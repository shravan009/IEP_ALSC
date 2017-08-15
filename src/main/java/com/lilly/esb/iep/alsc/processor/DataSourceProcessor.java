package com.lilly.esb.iep.alsc.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DataSourceProcessor implements Processor {
	private static final Logger LOGGER = LoggerFactory
            .getLogger(DataSourceProcessor.class);
	
    protected NamedParameterJdbcTemplate dataSourceTemplate;
    private String studyQuery;
        
    public String getStudyQuery() {
		return studyQuery;
	}

	public void setStudyQuery(String studyQuery) {
		this.studyQuery = studyQuery;
	}

	public void setDataSourceTemplate(NamedParameterJdbcTemplate dsTemplate) {
        this.dataSourceTemplate = dsTemplate;
    }
	
	@Override
	public void process(Exchange exchange) throws Exception {
		final StringBuilder data = new StringBuilder();
		Map<String, Object> namedParams = null;
		
		RowCallbackHandler handler = new RowCallbackHandler() {
			public void processRow(ResultSet resultSet) throws SQLException {
				LOGGER.info("callback");
				data.append(resultSet.getString(1) + "\n");				
			}
		};

		LOGGER.info("$$$$$$$$$ In DataSourceProcessor $$$$$$$$$" + studyQuery);
		//dataSourceTemplate.query("select name from artist_name where ROWNUM <= 100", namedParams, handler);
		//exchange.getIn().setBody(data.toString());
	}

}
