/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.bean;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Brett Meyer
 */
public abstract class AbstractCsvGenerator {

    protected NamedParameterJdbcTemplate dataSourceTemplate;
    
    public abstract void generateCsv(final Exchange exchange) throws Exception;
    
    public void setDataSourceTemplate(NamedParameterJdbcTemplate dsTemplate) {
        this.dataSourceTemplate = dsTemplate;
    }
    
    protected String createDataRow(ResultSet rs) throws SQLException {
    	StringBuilder data = new StringBuilder();
    	ResultSetMetaData meta = rs.getMetaData();
    	
    	int columnCount = meta.getColumnCount();
    	for (int col = 1; col <= columnCount; col++) {
			data.append(sanitize(rs.getString(col)));
			if (col != columnCount) {
				data.append(",");
			}
		}
    	return data.toString();
    }


    protected String sanitize(String dbVal) {
    	String val = StringUtils.defaultString(dbVal);
    	StringBuilder newVal = new StringBuilder();
    	boolean surround = false;
    	
    	int length = val.length();
    	for (int i = 0; i < length; i++) {
    		char c = val.charAt(i);
    		
			switch (c) {
			case ('"'):
				surround = true;
				newVal.append("\"\""); // change " to ""
				break;
			
			case (','):
				surround = true;
				newVal.append(c);
				break;
				
			default:
				newVal.append(c);
				break;
			}
    	}
    	
    	if (surround) {
    		newVal.insert(0, '"');
    		newVal.append('"');
    	}
    	
    	return newVal.toString();
    }
    
}
