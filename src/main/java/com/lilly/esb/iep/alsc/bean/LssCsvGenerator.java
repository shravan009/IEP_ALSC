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
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Queries IIP and transforms results into CSV
 * 
 * @author Brett Meyer
 */
public class LssCsvGenerator extends AbstractCsvGenerator {

	/*
	 * Have to include the domain (table name) for each of
	 * the queries in the onfiguration since the Oracle
	 * driver does not support the JDBC ResultSetMetaData 
	 * getTableName() method.  Likewise, need to include
	 * header (column names) since we are not selecting 
	 * individual column names in the query and
	 * ResultSetMetaData.getColumnName() method does not
	 * give proper column names.
	 */
	private String queryCaseGeneralStg;
	private String headerCaseGeneralStg;
	private String domainCaseGeneralStg;

	private String queryEventStg;
	private String headerEventStg;
	private String domainEventStg;

	private String queryEventAssessStg;
	private String headerEventAssessStg;
	private String domainEventAssessStg;

	private static final String STUDY_ID_PLACEHOLDER = "<study_id>";
	private static final String DOMAIN_PLACEHOLDER = "<domain>";
	private static final String TIMESTAMP_PLACEHOLDER = "<timestamp>";

	private String fullPath;

	public void setQueryCaseGeneralStg(String queryCaseGeneralStg) {
		this.queryCaseGeneralStg = queryCaseGeneralStg;
	}

	public void setHeaderCaseGeneralStg(String headerCaseGeneralStg) {
		this.headerCaseGeneralStg = headerCaseGeneralStg;
	}

	public void setDomainCaseGeneralStg(String domainCaseGeneralStg) {
		this.domainCaseGeneralStg = domainCaseGeneralStg;
	}

	public void setQueryEventStg(String queryEventStg) {
		this.queryEventStg = queryEventStg;
	}

	public void setHeaderEventStg(String headerEventStg) {
		this.headerEventStg = headerEventStg;
	}

	public void setDomainEventStg(String domainEventStg) {
		this.domainEventStg = domainEventStg;
	}

	public void setQueryEventAssessStg(String queryEventAssessStg) {
		this.queryEventAssessStg = queryEventAssessStg;
	}

	public void setHeaderEventAssessStg(String headerEventAssessStg) {
		this.headerEventAssessStg = headerEventAssessStg;
	}

	public void setDomainEventAssessStg(String domainEventAssessStg) {
		this.domainEventAssessStg = domainEventAssessStg;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public void generateCsv(final Exchange exchange) throws Exception {
		final String study_als_cd = exchange.getIn().getBody(String.class);
		final StringBuilder stringBuilder = new StringBuilder();
		// Need new SDF for each request as it's not thread safe
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		final String currentTimestamp = simpleDateFormat.format(new java.util.Date());

		Map<String, Object> namedParams = new HashMap<>();
		namedParams.put("study_als_cd", study_als_cd);

		/*
		 *  TODO: DAH - It may be more efficient to one day do one single query 
		 *  (since the same table is hit each time) and then store off the results
		 *  to be used to build the .csvs instead of doing three separate queries
		 *  that build three csvs.
		 */
		stringBuilder.append(writeCsv(queryCaseGeneralStg, headerCaseGeneralStg, namedParams, study_als_cd, domainCaseGeneralStg, currentTimestamp));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(queryEventStg, headerEventStg, namedParams, study_als_cd, domainEventStg, currentTimestamp));
		stringBuilder.append("\n");
		stringBuilder.append(writeCsv(queryEventAssessStg, headerEventAssessStg, namedParams, study_als_cd, domainEventAssessStg, currentTimestamp));

		// Set the body to newline-separated String of absolute paths
		exchange.getOut().setBody(stringBuilder.toString(), String.class);
	}

	private String writeCsv(String query, String header, Map<String, Object> namedParams, 
			String study_als_cd, String domain, String timestamp) throws IOException {
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
