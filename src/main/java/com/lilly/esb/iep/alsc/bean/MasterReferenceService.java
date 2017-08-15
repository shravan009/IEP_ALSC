package com.lilly.esb.iep.alsc.bean;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.xml.sax.SAXException;

import com.lilly.esb.iep.alsc.Constants;
import com.lilly.esb.iep.alsc.model.reference.ALSCMasterRefData;
import com.lilly.esb.iep.alsc.model.reference.ALSCMasterRefData.Study;
import com.lilly.esb.iep.alsc.model.reference.Failure;
import com.lilly.esb.iep.alsc.model.reference.PartnerResponse;
import com.lilly.esb.iep.alsc.model.reference.Status;
import com.lilly.esb.iep.alsc.model.reference.Success;
import com.lilly.esb.iep.alsc.common.exception.ValidationException;


public class MasterReferenceService {
	protected NamedParameterJdbcTemplate dataSourceTemplate;
	private String queryReference;

	private String partnerResponseSchemaFilePath;
	private String alscMasterRefDataSchemaFilePath;

	private Marshaller alscMasterRefDataMarshaller;
	private Unmarshaller partnerResponseUnmarshaller;

	private DatatypeFactory dataTypeFactory;
	private XmlValidator xmlValidator;

	public void setDataSourceTemplate(NamedParameterJdbcTemplate template) {
		this.dataSourceTemplate = template;
	}

	public void setQueryReference(String queryReference) {
		this.queryReference = queryReference;
	}

	public void setPartnerResponseSchemaFilePath(
			String partnerResponseSchemaFilePath) {
		this.partnerResponseSchemaFilePath = partnerResponseSchemaFilePath;
	}

	public void setAlscMasterRefDataSchemaFilePath(
			String alscMasterRefDataSchemaFilePath) {
		this.alscMasterRefDataSchemaFilePath = alscMasterRefDataSchemaFilePath;
	}	

	public void setXmlValidator(XmlValidator xmlValidator) {
		this.xmlValidator = xmlValidator;
	}

	public MasterReferenceService() throws SAXException, JAXBException, DatatypeConfigurationException {				
		final JAXBContext alscMasterRefDataJaxbContext = JAXBContext.newInstance(ALSCMasterRefData.class);
		this.alscMasterRefDataMarshaller = alscMasterRefDataJaxbContext.createMarshaller();
		this.alscMasterRefDataMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
		// Intentionally not setting schema so validation can be manually done to throw IEP ValidationException

		final JAXBContext partnerResponseJaxbContext = JAXBContext.newInstance(PartnerResponse.class);
		this.partnerResponseUnmarshaller = partnerResponseJaxbContext.createUnmarshaller();
		// Intentionally not setting schema so validation can be manually done to return 400 response code

		this.dataTypeFactory = DatatypeFactory.newInstance();
	}

	public void processMasterReferenceAlscFinalResponseMessage(final Exchange exchange) {
		final Message inMsg = exchange.getIn();
		final Message outMsg = exchange.getOut();
		outMsg.setHeader(Exchange.BREADCRUMB_ID, inMsg.getHeader(Exchange.BREADCRUMB_ID, String.class)); // Preserve the breadcrumbid
		final String inBody = inMsg.getBody(String.class);
		PartnerResponse partnerResponse = null;
		try {
			xmlValidator.validate(inBody, partnerResponseSchemaFilePath);
			partnerResponse = (PartnerResponse) partnerResponseUnmarshaller.unmarshal(new StringReader(inBody));
		} catch (Exception e) {
			final String outBody = "Failed to validate/deserialize PartnerResponse XML Payload: " + inBody;
			outMsg.setBody(outBody, String.class);
			outMsg.setHeader(Exchange.HTTP_RESPONSE_CODE, HttpURLConnection.HTTP_BAD_REQUEST);
			outMsg.setHeader(Exchange.CONTENT_TYPE, MediaType.TEXT_PLAIN);
			exchange.setProperty(Constants.ALSC_CREATE_SNOW_TICKET_PROPERTY, Boolean.TRUE);
			return;
		}
		final boolean doCreateServiceNowTicketFromFinalResponse = this.setHttpResponseMessageFromAlscFinalResponse(partnerResponse, outMsg);
		exchange.setProperty(Constants.ALSC_CREATE_SNOW_TICKET_PROPERTY, doCreateServiceNowTicketFromFinalResponse);
	}

	public void generateAndMarshallMasterReferenceAlscPayload(final Exchange exchange) throws JAXBException, ValidationException, SAXException, Exception {
		final Message inMsg = exchange.getIn();
		final Message outMsg = exchange.getOut();
		outMsg.setHeader(Exchange.BREADCRUMB_ID, inMsg.getHeader(Exchange.BREADCRUMB_ID, String.class)); // Preserve the breadcrumbid
		final String studyId = inMsg.getHeader(Constants.ALSC_STUDY_ID, String.class);
		final String defaultTherapeuticArea = inMsg.getHeader(Constants.ALSC_DEFAULT_THERAPEUTIC_AREA, String.class);

		final Map<String, Object> namedParams = new HashMap<>();		
		namedParams.put("study_als_cd", studyId);

		final ALSCMasterRefData data = new ALSCMasterRefData();
		data.setFileCreationDateTime(this.dataTypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
		data.setFileDesc("");
		data.setFileType("Cumulative");
		data.setModelVersion("01.00");

		RowCallbackHandler handler = new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				Study study = buildStudy(rs, defaultTherapeuticArea);
				data.getStudy().add(study);
			}
		};

		dataSourceTemplate.query(queryReference, namedParams, handler);

		final java.io.StringWriter sw = new StringWriter();
		alscMasterRefDataMarshaller.marshal(data, sw);
		final String alscMasterReferenceXmlData = sw.toString();
		xmlValidator.validate(alscMasterReferenceXmlData, this.alscMasterRefDataSchemaFilePath);

		// Intentionally not preserving breadcrumbid so a new unique one is generated to send to frm/alsc
		outMsg.setBody(alscMasterReferenceXmlData);
	}

	/*
	 * This method processes a PartnerResponse object by checking that it's populated with
	 * a status object and setting a camel http response message based on success/failure.
	 * In addition, this method returns a boolean indiciating whether a service now ticket
	 * should be generated for either an improperly populated XML message body or a failure
	 * response status.
	 */
	private boolean setHttpResponseMessageFromAlscFinalResponse(final PartnerResponse partnerResponse, final Message outMsg) {
		final String responseContentType = MediaType.TEXT_PLAIN;
		String responseBody = null;
		Integer responseCode = null;
		Boolean createServiceNowTix = null;

		if (partnerResponse != null) {
			final List<Status> statusList = partnerResponse.getStatus();
			if (statusList != null && statusList.size() == 1) {
				final Status status = statusList.get(0);
				final Success success = status.getSuccess();
				final Failure failure = status.getFailure();
				if (success != null) {
					responseBody = String.format("Processed SUCCESS final response message with OID: %s TargetOID: %s", success.getOID(), success.getTargetOID());
					responseCode = HttpURLConnection.HTTP_OK;
					createServiceNowTix = false;
				} else if (failure != null) {
					responseBody = String.format("Processed FAILURE final response message with OID: %s FailureType: %s Code: %s Description: %s", failure.getOID(), failure.getType().value(), failure.getCode(), failure.getDescription());
					responseCode = HttpURLConnection.HTTP_OK; // Even return 200 with failure since we still successfully received/processed the final response message
					createServiceNowTix = true;
				}
			} else if (statusList.size() > 1) {
				responseBody = "Multiple status elements found in PartnerResponse XML";
				responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
				createServiceNowTix = true;
			} else { // null or 0
				responseBody = "No status elements found in PartnerResponse XML";
				responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
				createServiceNowTix = true;
			}
		} else {
			responseBody = "PartnerResponse payload is null";
			responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
			createServiceNowTix = true;
		}
		outMsg.setHeader(Exchange.CONTENT_TYPE, responseContentType);
		outMsg.setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
		outMsg.setBody(responseBody, String.class);

		return createServiceNowTix;
	}

	private Study buildStudy (ResultSet rs, String defaultTherapeuticArea) throws SQLException {
		final Study study = new Study();

		study.setStudyID(rs.getString("StudyID"));
		study.setStudyTitle(rs.getString("StudyTitle"));
		study.setDevelopmentPhase(rs.getString("DevelopmentPhase"));
		study.setStudyStartDate(rs.getString("StudyStartDate"));
		study.setStudyEndDate(rs.getString("StudyEndDate"));
		study.setDrugProgramCd(rs.getString("DrugProgramCd"));
		study.setStudyDesignBlinding(rs.getString("StudyDesignBlinding"));

		study.setStudyTherapeuticArea(StringUtils.defaultIfBlank(rs.getString("StudyTherapeuticArea"), defaultTherapeuticArea));

		study.setSponsoringUnit(rs.getString("SponsoringUnit"));
		study.setStatisticalDesign(rs.getString("StatisticalDesign"));
		study.setStudyCurrentStatus(rs.getString("StudyStatusCurrent"));
		study.setStudyClosedFlag(rs.getString("StudyClosedFlag"));
		study.setIndication("");

		return study;
	}

}
