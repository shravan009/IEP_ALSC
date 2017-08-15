/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.bean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.lilly.esb.iep.alsc.Constants;
import com.lilly.esb.iep.alsc.Constants.DmwInterfaceType;

/**
 * Queries IIP and transforms results into 2 CSVs - one for General 
 * (blinded data) and one for Secure (unblinded data).
 * 
 * @author Brett Meyer
 * @author David Heitzer
 */
public class EctsCsvGenerator extends AbstractCsvGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(EctsCsvGenerator.class);

	private static final String STUDY_ID_PLACEHOLDER = "<study_id>";
	private static final String DOMAIN_PLACEHOLDER = "<domain>";
	private static final String TIMESTAMP_PLACEHOLDER = "<timestamp>";

	private String ectsSecureFesStagingDirFullPath;
	private String ectsGeneralFesStagingDirFullPath;

	/* MDF-SS_OWNER subscription schema  tables */
	private String trtmnt_grp_desc_query;
	private String trtmnt_grp_desc_header;
	private String trtmnt_grp_desc_domain;

	private String package_desc_query;
	private String package_desc_header;
	private String package_desc_domain;

	private String mdf_ss_owner_dtp_sms_treatment_dispense_query;
	private String mdf_ss_owner_dtp_sms_treatment_dispense_header;
	private String mdf_ss_owner_dtp_sms_treatment_dispense_domain;

	private String mdf_ss_owner_dtp_sms_treatment_dispense_f_query;
	private String mdf_ss_owner_dtp_sms_treatment_dispense_f_header;
	private String mdf_ss_owner_dtp_sms_treatment_dispense_f_domain;

	private String mdf_ss_owner_dtp_sms_randomization_query;
	private String mdf_ss_owner_dtp_sms_randomization_header;
	private String mdf_ss_owner_dtp_sms_randomization_domain;

	private String mdf_ss_owner_dtp_sms_randomization_f_query;
	private String mdf_ss_owner_dtp_sms_randomization_f_header;
	private String mdf_ss_owner_dtp_sms_randomization_f_domain;

	/* Data lake tables */
	private String ects_ss_owner_ivrs_sbjct_sts_query;
	private String ects_ss_owner_ivrs_sbjct_sts_header;
	private String ects_ss_owner_ivrs_sbjct_sts_domain;

	private String ects_ss_owner_ivrs_sbjct_data_query;
	private String ects_ss_owner_ivrs_sbjct_data_header;
	private String ects_ss_owner_ivrs_sbjct_data_domain;

	private String ects_ss_owner_ivrs_site_enroll_query;
	private String ects_ss_owner_ivrs_site_enroll_header;
	private String ects_ss_owner_ivrs_site_enroll_domain;

	public void setEctsSecureFesStagingDirFullPath(
			String ectsSecureFesStagingDirFullPath) {
		this.ectsSecureFesStagingDirFullPath = ectsSecureFesStagingDirFullPath;
	}

	public void setEctsGeneralFesStagingDirFullPath(
			String ectsGeneralFesStagingDirFullPath) {
		this.ectsGeneralFesStagingDirFullPath = ectsGeneralFesStagingDirFullPath;
	}

	public void setTrtmnt_grp_desc_query(String trtmnt_grp_desc_query) {
		this.trtmnt_grp_desc_query = trtmnt_grp_desc_query;
	}

	public void setTrtmnt_grp_desc_header(String trtmnt_grp_desc_header) {
		this.trtmnt_grp_desc_header = trtmnt_grp_desc_header;
	}

	public void setTrtmnt_grp_desc_domain(String trtmnt_grp_desc_domain) {
		this.trtmnt_grp_desc_domain = trtmnt_grp_desc_domain;
	}

	public void setPackage_desc_query(String package_desc_query) {
		this.package_desc_query = package_desc_query;
	}

	public void setPackage_desc_header(String package_desc_header) {
		this.package_desc_header = package_desc_header;
	}

	public void setPackage_desc_domain(String package_desc_domain) {
		this.package_desc_domain = package_desc_domain;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_query(
			String mdf_ss_owner_dtp_sms_treatment_dispense_query) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_query = mdf_ss_owner_dtp_sms_treatment_dispense_query;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_header(
			String mdf_ss_owner_dtp_sms_treatment_dispense_header) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_header = mdf_ss_owner_dtp_sms_treatment_dispense_header;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_domain(
			String mdf_ss_owner_dtp_sms_treatment_dispense_domain) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_domain = mdf_ss_owner_dtp_sms_treatment_dispense_domain;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_f_query(
			String mdf_ss_owner_dtp_sms_treatment_dispense_f_query) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_f_query = mdf_ss_owner_dtp_sms_treatment_dispense_f_query;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_f_header(
			String mdf_ss_owner_dtp_sms_treatment_dispense_f_header) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_f_header = mdf_ss_owner_dtp_sms_treatment_dispense_f_header;
	}

	public void setMdf_ss_owner_dtp_sms_treatment_dispense_f_domain(
			String mdf_ss_owner_dtp_sms_treatment_dispense_f_domain) {
		this.mdf_ss_owner_dtp_sms_treatment_dispense_f_domain = mdf_ss_owner_dtp_sms_treatment_dispense_f_domain;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_query(
			String mdf_ss_owner_dtp_sms_randomization_query) {
		this.mdf_ss_owner_dtp_sms_randomization_query = mdf_ss_owner_dtp_sms_randomization_query;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_header(
			String mdf_ss_owner_dtp_sms_randomization_header) {
		this.mdf_ss_owner_dtp_sms_randomization_header = mdf_ss_owner_dtp_sms_randomization_header;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_domain(
			String mdf_ss_owner_dtp_sms_randomization_domain) {
		this.mdf_ss_owner_dtp_sms_randomization_domain = mdf_ss_owner_dtp_sms_randomization_domain;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_f_query(
			String mdf_ss_owner_dtp_sms_randomization_f_query) {
		this.mdf_ss_owner_dtp_sms_randomization_f_query = mdf_ss_owner_dtp_sms_randomization_f_query;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_f_header(
			String mdf_ss_owner_dtp_sms_randomization_f_header) {
		this.mdf_ss_owner_dtp_sms_randomization_f_header = mdf_ss_owner_dtp_sms_randomization_f_header;
	}

	public void setMdf_ss_owner_dtp_sms_randomization_f_domain(
			String mdf_ss_owner_dtp_sms_randomization_f_domain) {
		this.mdf_ss_owner_dtp_sms_randomization_f_domain = mdf_ss_owner_dtp_sms_randomization_f_domain;
	}

	public void setEcts_ss_owner_ivrs_sbjct_sts_query(
			String ects_ss_owner_ivrs_sbjct_sts_query) {
		this.ects_ss_owner_ivrs_sbjct_sts_query = ects_ss_owner_ivrs_sbjct_sts_query;
	}

	public void setEcts_ss_owner_ivrs_sbjct_sts_header(
			String ects_ss_owner_ivrs_sbjct_sts_header) {
		this.ects_ss_owner_ivrs_sbjct_sts_header = ects_ss_owner_ivrs_sbjct_sts_header;
	}

	public void setEcts_ss_owner_ivrs_sbjct_sts_domain(
			String ects_ss_owner_ivrs_sbjct_sts_domain) {
		this.ects_ss_owner_ivrs_sbjct_sts_domain = ects_ss_owner_ivrs_sbjct_sts_domain;
	}

	public void setEcts_ss_owner_ivrs_sbjct_data_query(
			String ects_ss_owner_ivrs_sbjct_data_query) {
		this.ects_ss_owner_ivrs_sbjct_data_query = ects_ss_owner_ivrs_sbjct_data_query;
	}

	public void setEcts_ss_owner_ivrs_sbjct_data_header(
			String ects_ss_owner_ivrs_sbjct_data_header) {
		this.ects_ss_owner_ivrs_sbjct_data_header = ects_ss_owner_ivrs_sbjct_data_header;
	}

	public void setEcts_ss_owner_ivrs_sbjct_data_domain(
			String ects_ss_owner_ivrs_sbjct_data_domain) {
		this.ects_ss_owner_ivrs_sbjct_data_domain = ects_ss_owner_ivrs_sbjct_data_domain;
	}

	public void setEcts_ss_owner_ivrs_site_enroll_query(
			String ects_ss_owner_ivrs_site_enroll_query) {
		this.ects_ss_owner_ivrs_site_enroll_query = ects_ss_owner_ivrs_site_enroll_query;
	}

	public void setEcts_ss_owner_ivrs_site_enroll_header(
			String ects_ss_owner_ivrs_site_enroll_header) {
		this.ects_ss_owner_ivrs_site_enroll_header = ects_ss_owner_ivrs_site_enroll_header;
	}

	public void setEcts_ss_owner_ivrs_site_enroll_domain(
			String ects_ss_owner_ivrs_site_enroll_domain) {
		this.ects_ss_owner_ivrs_site_enroll_domain = ects_ss_owner_ivrs_site_enroll_domain;
	}

	@Override
	public void generateCsv(final Exchange exchange) throws Exception {
		final String studyAlsCd = exchange.getIn().getBody(String.class);
		// Need new SDF for each request as it's not thread safe
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		final String currentTimestamp = simpleDateFormat.format(new java.util.Date());

		// For eCTS, we generate and send two csv mq msgs - one for general and one for secure
		final DmwInterfaceType dmwInterfaceType = exchange.getProperty(Constants.ALSC_DMW_INTERFACE_TYPE_PROPERTY, DmwInterfaceType.class);
		if (dmwInterfaceType == null) {
			throw new Exception("ALSC_DMW_INTERFACE_TYPE is null when processing eCTS generateCsv() call");
		}
		switch(dmwInterfaceType) {
		case SECURE:
			exchange.getOut().setBody(this.generateSecureCsv(studyAlsCd, currentTimestamp));
			break;
		case GENERAL:
			exchange.getOut().setBody(this.generateGeneralCsv(studyAlsCd, currentTimestamp));
			break;
		default:
			throw new Exception("invalid ALSC_DMW_INTERFACE_TYPE: " + dmwInterfaceType + " when processing eCTS generateCsv() call");

		}
	}

	/*
	 * Secure files with unblinded data (sent with additional required reference tables)
	 */
	private String generateSecureCsv(final String studyAlsCd, final String currentTimestamp) throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		final Map<String, Object> namedParams = new HashMap<>();
		namedParams.put("study_als_cd", studyAlsCd);

		stringBuilder.append(writeCsv(mdf_ss_owner_dtp_sms_randomization_query, mdf_ss_owner_dtp_sms_randomization_header, namedParams, studyAlsCd, mdf_ss_owner_dtp_sms_randomization_domain, currentTimestamp, ectsSecureFesStagingDirFullPath));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(mdf_ss_owner_dtp_sms_treatment_dispense_query, mdf_ss_owner_dtp_sms_treatment_dispense_header, namedParams, studyAlsCd, mdf_ss_owner_dtp_sms_treatment_dispense_domain, currentTimestamp, ectsSecureFesStagingDirFullPath));

		return stringBuilder.toString();
	}

	/*
	 * General files with blinded data (_f fake tables) (sent with additional required reference tables)
	 */
	private String generateGeneralCsv(final String studyAlsCd, final String currentTimestamp) throws Exception {
		final StringBuilder stringBuilder = new StringBuilder();
		final Map<String, Object> namedParams = new HashMap<>();

		namedParams.put("study_als_cd", studyAlsCd);		
		/* Fake blinded data table */
		stringBuilder.append(writeCsv(mdf_ss_owner_dtp_sms_randomization_f_query, mdf_ss_owner_dtp_sms_randomization_f_header, namedParams, studyAlsCd, mdf_ss_owner_dtp_sms_randomization_f_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		/* Fake blinded data table */
		stringBuilder.append(writeCsv(mdf_ss_owner_dtp_sms_treatment_dispense_f_query, mdf_ss_owner_dtp_sms_treatment_dispense_f_header, namedParams, studyAlsCd, mdf_ss_owner_dtp_sms_treatment_dispense_f_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(trtmnt_grp_desc_query, trtmnt_grp_desc_header, namedParams, studyAlsCd, trtmnt_grp_desc_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(package_desc_query, package_desc_header, namedParams, studyAlsCd, package_desc_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		/* Data lake queries */
		stringBuilder.append(writeCsv(ects_ss_owner_ivrs_sbjct_sts_query, ects_ss_owner_ivrs_sbjct_sts_header, namedParams, studyAlsCd, ects_ss_owner_ivrs_sbjct_sts_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(ects_ss_owner_ivrs_sbjct_data_query, ects_ss_owner_ivrs_sbjct_data_header, namedParams, studyAlsCd, ects_ss_owner_ivrs_sbjct_data_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(ects_ss_owner_ivrs_site_enroll_query, ects_ss_owner_ivrs_site_enroll_header, namedParams, studyAlsCd, ects_ss_owner_ivrs_site_enroll_domain, currentTimestamp, ectsGeneralFesStagingDirFullPath));

		return stringBuilder.toString();
	}

	private String writeCsv(String query, String header, Map<String, Object> namedParams, 
			String study_als_cd, String domain, String timestamp, String csvFullPathWithPlaceholders) throws IOException {
		final StringBuilder csv = new StringBuilder();
		csv.append(header.trim().toUpperCase()); // Add the header as the first row in the csv (ALSC requires all upper case and no trailing spaces)

		RowCallbackHandler handler = new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet resultSet) throws SQLException {
				csv.append("\n"); // Avoid the trailing line feed at the end of the data per ALSC
				csv.append(createDataRow(resultSet));
			}
		};

		LOGGER.info("Running Query: " + query + "for study alias: " + study_als_cd + "Using map: " + namedParams);

		dataSourceTemplate.query(query, namedParams, handler);

		csvFullPathWithPlaceholders = csvFullPathWithPlaceholders.replace(STUDY_ID_PLACEHOLDER, study_als_cd);
		csvFullPathWithPlaceholders = csvFullPathWithPlaceholders.replace(DOMAIN_PLACEHOLDER, domain);
		csvFullPathWithPlaceholders = csvFullPathWithPlaceholders.replace(TIMESTAMP_PLACEHOLDER, timestamp);

		// Write the CSV file to FES' staging area.
		Path path = Paths.get(csvFullPathWithPlaceholders);
		Files.write(path, csv.toString().getBytes(StandardCharsets.UTF_8));

		return csvFullPathWithPlaceholders;
	}

}
