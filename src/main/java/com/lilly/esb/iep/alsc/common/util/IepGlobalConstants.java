package com.lilly.esb.iep.alsc.common.util;


import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

/**
 * A simple class containing all the constants used within the projects.
 * 
 */
public final class IepGlobalConstants {

	/**
	 * Private parameterless constructor used to prevent direct instantiation since this is a utility class.
	 */
	private IepGlobalConstants() {
	}

	public enum ValidateServiceType{
		VALIDATE,
		CONVERT
	}
	
	public static final EnumSet<ValidateServiceType> VALIDATE_SERVICE_TYPES = EnumSet.of(ValidateServiceType.CONVERT, ValidateServiceType.VALIDATE);
	
	public static final Set<MediaType> VALIDATE_SUPPORTED_MEDIA_TYPES = Collections.unmodifiableSet(new HashSet<MediaType>
																					(Arrays.asList(
																							MediaType.APPLICATION_JSON_TYPE, 
																							MediaType.APPLICATION_XML_TYPE)));
	
	/** The Constant SOURCE_SYSTEM_NAME. */
	public static final String SOURCE_SYSTEM_NAME = "InformationExchangePlatform";

	/** The Constant EIP_DEFAULT_DATE_FORMAT. */
	public static final String EIP_DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/** The Constant EIP_DEFAULT_DATETIME_FORMAT. */
	public static final String EIP_DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	/** The Constant FIRST_ITEM_INDEX. */
	public static final int FIRST_ITEM_INDEX = 0;

	/** The Constant SINGLE_ITEM. */
	public static final int SINGLE_ITEM = 1;

	/** The Constant SINGLE_HEADER. */
	public static final int SINGLE_HEADER = 1;

	/** The Constant EMPTY_STRING. */
	public static final String EMPTY_STRING = "";

	/** The Constant ZERO. */
	public static final int ZERO = 0;

	/** The Constant DOUBLE_ZERO. */
	public static final double DOUBLE_ZERO = 0.0;

	/** The Constant MAX_ROWS_RETURNED_BY_GETALL. */
	public static final int MAX_ROWS_RETURNED_BY_GETALL = 5000;

	/** The Constant MANDATORY_FIELD_MISSING_MESSAGE_ERROR_MSG. */
	public static final String MANDATORY_FIELD_MISSING_MESSAGE_ERROR_MSG = "Mandatory field from the message is missing";

	/** The Constant EXTRANEOUS_MESSAGES_ERROR_MSG. */
	public static final String EXTRANEOUS_MESSAGES_ERROR_MSG = "More than one item was received when there should only be one so only the first will be used. The exchange id for the message was: ";

	/** The Constant EXTRANEOUS_HEADERS_ERROR_MSG. */
	public static final String EXTRANEOUS_HEADERS_ERROR_MSG = "More than one message header was received when there should only be one so only the first will be used. The exchange id for the message was: ";

	/** The Constant INCOMPLETE_SOURCE_MSG_ERROR. */
	public static final String INCOMPLETE_SOURCE_MSG_ERROR = "The message received from the source was properly formatted but incomplete so it could not be processed. The exchange id for the message was: ";

	/** The Constant DATE_PARSING_ERROR_MSG. */
	public static final String DATE_PARSING_ERROR_MSG = "Unable to parse the date string: ";
	
	/** Constants for IEP properties. */
	public static final String IEP_PROPERTY_DOMAIN = "IepDomain";
	public static final String IEP_PROPERTY_ORIGINAL_MESSAGE = "IepOriginalMessage";
	
	/** Constants for SharePoint Study ID Filtering */
	public static final String IEP_PROPERTY_SHAREPOINT_STUDY_ID_LIST = "IepSharePointStudyIdList";
	public static final String IEP_PROPERTY_SHAREPOINT_STUDY_ID_XPATH_EXP = "IepSharePointStudyIdXpathExp";
	public static final String IEP_PROPERTY_SHAREPOINT_LIFECYCLE_XPATH_EXP = "IepSharePointLifecycleXpathExp";
	public static final String IEP_PROPERTY_SP_RESULTS_ELEMENT_NS_PREFIX = "IepSpResultsElementNsPrefix";
	public static final String IEP_PROPERTY_SP_RESULTS_ELEMENT_NS_URI = "IepSpResultsElementNsUri";
	public static final String IEP_PROPERTY_SHAREPOINT_QUERY_FILTER_EXP = "IepSharePointQueryFilterExp";
	public static final String IEP_PROPERTY_DO_FILTER_MESSAGE = "IepDoFilterMessage";
	public static final String IEP_PROPERTY_SOURCE_MESSAGE_STUDY_ID_LIST = "IepSourceMessageStudyIdList";
	public static final String IEP_PROPERTY_STUDY_REPLAY = "IepStudyReplay";
	
	/** Constants for IEP service http headers/query params. */
	public static final String IEP_HEADER_STATUS = "Iep_Status";
	public static final String IEP_HEADER_DOMAIN = "IepDomain";
	public static final String IEP_HEADER_COMMAND = "IepCommand";
	public static final String IEP_HEADER_PARAMETER_MAP = "IepParameterMap";
	public static final String IEP_HEADER_ERROR_TEXT = "IepErrorText";
	public static final String IEP_HEADER_ERROR_OCCURRED = "IepErrorOccurred";
	public static final String IEP_HEADER_CLIENTID = "IepClientId";
	public static final String IEP_HEADER_MESSAGEID = "IepMessageId";
	public static final String IEP_HEADER_BUSINESS_TRANSACTION_ID = "IepBusinessTransactionId";
    public static final String IEP_HEADER_SOURCE_NAME = "IepSourceName";
    public static final String IEP_HEADER_TARGET_NAME = "IepTargetName";
	public static final String IEP_HEADER_CALLBACK_URL = "IepCallbackUrl";
	public static final String IEP_HEADER_TYPE = "IepType";
	public static final String IEP_HEADER_SERVICE = "IepService";
	public static final String IEP_HEADER_INBOUND_PROTOCOL = "IepInboundProtocol";
	public static final String IEP_HEADER_RESULT_SIZE = "IepResultsize";
	public static final String IEP_HEADER_RESULT_PAGE = "IepResultpage";
	public static final String IEP_HEADER_FILTER_BY = "IepIdFilter";
	public static final String IEP_HEADER_METHOD_ROUTE_URI = "IepMethodRouteUri";
	public static final String IEP_HEADER_SUBDOMAINS = "IepSubDomains";
	
	public static final String IEP_HEADER_DO_FILTER_MESSAGE = "IepDoFilterMessage";
	
	public static final String IEP_HEADER_FORWARD_URL = "IepForwardUrl";
	
	public static final String IEP_HEADER_SOURCE_CREATION_DATE = "SourceCreationDate";
	
	public static final String IEP_HEADER_SOURCE_RECEIPT_DATE = "SourceReceiptDate";
	
    public static final String IEP_HEADER_EXCEPTION_HANDLER_DISPATCH_MILESTONE = "IepExceptionHandlerDispatchMilestone";
    
    public static final String IEP_HEADER_EXCEPTION_MESSAGE = "IepExceptionMessage";
    public static final String IEP_HEADER_EXCEPTION_STACKTRACE = "IepExceptionStacktrace";
    
    public static final String IEP_HEADER_EXCEPTION_SERVICENOW = "IepExceptionServiceNow";
    
    public static final String IEP_HEADER_EXCEPTION_DLQ = "IepExceptionDlq";
    
    public static final String IEP_HEADER_MAP_400_500 = "IepMapHttp400To500";
	
	public static final String DISPATCH_HEADER_PARTNER_NAME = "partnerName";
	public static final String DISPATCH_HEADER_PARTNER_TRANSID = "partnerTransId";
	
	public static final String SOAP_BUSINESS_TRANSACTION_ID = "BusinessTransactionId";
	public static final String SUMMARY_ONLY = "SummaryOnly";
	
	public static final String IEP_REQUEST_TIMESTAMP = "IepRequestTimestamp";
	
	/** Constants for IEP domain names */
	public static final String IEP_DOMAIN_STUDY = "study";
	public static final String IEP_DOMAIN_REFERENCE = "reference";
	public static final String IEP_DOMAIN_STUDYDESIGN = "studydesign";
	public static final String IEP_DOMAIN_CLINPLAN = "clinicalplan";
	public static final String IEP_DOMAIN_LAB = "lab";
	public static final String IEP_DOMAIN_SAMPLE = "sample";
	public static final String IEP_DOMAIN_TRIALFORCE = "trialforce";
	public static final String IEP_DOMAIN_IMPACT = "impact";
	public static final String IEP_DOMAIN_SIP = "sip";
	
	public static final String SOAP_PROTOCOL = "soap";
	public static final String REST_PROTOCOL = "rest";
	
	public static final String DYNAMICENDPOINT_DOMAIN_CONSUMERS = "domainConsumers";
	public static final String DYNAMICENDPOINT_PARTNER_CONFIG = "partnerConfiguration";
	public static final String DYNAMICENDPOINT_SYSTEM_CONFIG = "systemConfiguration";
	
	public static final String DYNAMICENDPOINT_PARTNER_SUBMISSION_SETTINGS = "partnerSubmissionSettings";
	public static final String DYNAMICENDPOINT_SYSTEM_SUBMISSION_SETTINGS = "systemSubmissionSettings";

	public static final String DYNAMICENDPOINT_PARTNER_FILE_DELIVERY_URI = "partnerFileDeliveryUri";
	public static final String DYNAMICENDPOINT_SYSTEM_FILE_DELIVERY_URI = "systemFileDeliveryUri";
	
	public static final String ODM_HEADER_EXTENSION_VERSION = "odmExtensionVersion";
	
	public static final String FTE_HEADER_SUBDIR = "LLYfteSubDir";
	public static final String FTE_HEADER_FILENAME = "LLYfteFileName";
	public static final String FTE_HEADER_LAST = "JMS_IBM_Last_Msg_In_Group";
	
	public static final String JMS_HEADER_GROUP_ID = "JMSXGroupID";
	public static final String JMS_HEADER_GROUP_SEQ = "JMSXGroupSeq";
	
	public static final String IEP_HEADER_MODEL_VERSION = "IepModelVersion";
	
	public static final String IEP_HEADER_VALIDATE_SERVICE_TYPE = "IepValidateServiceType";
	public static final String IEP_HEADER_VALIDATE_CURR_MEDIA_TYPE = "IepValidateCurrMediaType";
	public static final String IEP_HEADER_VALIDATE_EXPECT_MEDIA_TYPE = "IepValidateExpectMediaType";

    public static final String IEP_HEADER_TRACKER_REDELIVERY_ATTEMPTS = "IepTrackerRedeliveryAttempts";
    public static final String IEP_HEADER_TRACKER_BACKOFF_MULTIPLIER = "IepTrackerBackoffMultiplier";
    public static final String IEP_HEADER_TRACKER_DELAY_MILLISECOND = "IepTrackerDelayMillisecond";

    public static final String IEP_HEADER_FRM_CREATE_DATETIME = "IepFrmCreateDateTime";
    public static final String IEP_HEADER_FRM_TIMEOUT_DATETIME = "IepFrmTimeoutDateTime";
    public static final String IEP_HEADER_FRM_NOTES = "IepFrmNotes";
    public static final String IEP_HEADER_FRM_CALLBACK_URI = "IepFrmCallbackUri";
    public static final String IEP_HEADER_FRM_CALLBACK_PROTOCOL = "IepFrmCallbackProtocol";
    public static final String IEP_HEADER_FRM_BREADCRUMBID = "IepFrmBreadCrumbId"; 
    
    public static final String IEP_HEADER_VALIDATE_RESPONSE_VALUATE = "IepValidateResponseValuate";
    public static final String IEP_HEADER_FINAL_RESPONSE_MONITOR_NOTIFY = "IepFinalResponseMonitorNotify";
    
    public static final String IEP_HEADER_STUDY_REPLAY = "studyReplay";
    
	public static final String IEP_HEADER_STUDY_ID_LIST = "IepStudyIdList";
    
}
