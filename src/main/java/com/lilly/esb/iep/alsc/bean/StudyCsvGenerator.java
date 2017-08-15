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


/**
 * Queries IIP and transforms results into CSV
 * 
 * @author Brett Meyer
 */
public class StudyCsvGenerator extends AbstractCsvGenerator {

	public static final Logger LOGGER = LoggerFactory.getLogger(StudyCsvGenerator.class);
	
	private String masterStudyQuery;
	private String masterStudyHeader;
	private String masterStudyDomain;

	private static final String STUDY_ID_PLACEHOLDER = "<study_id>";
	private static final String DOMAIN_PLACEHOLDER = "<domain>";
	private static final String TIMESTAMP_PLACEHOLDER = "<timestamp>";

	private String fullPath;

	public void setMasterStudyQuery(String masterStudyQuery) {
		this.masterStudyQuery = masterStudyQuery;
	}

	public void setMasterStudyHeader(String masterStudyHeader) {
		this.masterStudyHeader = masterStudyHeader;
	}

	public void setMasterStudyDomain(String masterStudyDomain) {
		this.masterStudyDomain = masterStudyDomain;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public void generateCsv(final Exchange exchange) throws Exception {
		LOGGER.info("$$$$$$$$$$ In StudyCsvGenerator:generateCsv() $$$$$$$$$$");
		final String study_als_cd = exchange.getIn().getBody(String.class);
		final StringBuilder stringBuilder = new StringBuilder();
		// Need new SDF for each request as it's not thread safe
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		final String currentTimestamp = simpleDateFormat.format(new java.util.Date());

		Map<String, Object> namedParams = new HashMap<>();
		namedParams.put("study_als_cd", study_als_cd);

		stringBuilder.append(writeCsv(masterStudyQuery, masterStudyHeader, namedParams, study_als_cd, masterStudyDomain, currentTimestamp));

		// Set the body to newline-separated String of absolute paths
		exchange.getOut().setBody(stringBuilder.toString(), String.class);
	}

	private String writeCsv(String query, String header, Map<String, Object> namedParams, 
			String study_als_cd, String domain, String timestamp) throws IOException {
		LOGGER.info("$$$$$$$$$$ In StudyCsvGenerator:writeCsv() $$$$$$$$$$");
		final StringBuilder csv = new StringBuilder();
		csv.append(header.trim().toUpperCase()); // Add the header as the first row in the csv (ALSC requires all upper case and no trailing spaces)

		RowCallbackHandler handler = new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet resultSet) throws SQLException {
				csv.append("\n"); // Avoid the trailing line feed at the end of the data per ALSC
				csv.append(createDataRow(resultSet));
			}
		};

		dataSourceTemplate.query(query, namedParams, handler);

		String fullPathWithParams = fullPath.replace(STUDY_ID_PLACEHOLDER, study_als_cd);
		fullPathWithParams = fullPathWithParams.replace(DOMAIN_PLACEHOLDER, domain);
		fullPathWithParams = fullPathWithParams.replace(TIMESTAMP_PLACEHOLDER, timestamp);

		// Write the CSV file to FES' staging area.
		Path path = Paths.get(fullPathWithParams);
		Files.write(path, csv.toString().getBytes(StandardCharsets.UTF_8));

		return fullPathWithParams;
	}

}
