/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */

package com.lilly.esb.iep.alsc.bean;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.apache.camel.Exchange;
import org.apache.camel.Message;


public class FinalResponseService {
    private int timeoutMinutes;
    private String notes;
    private String sourceName;
    private String targetName;
    
    public FinalResponseService() {
        super();
    }
    
    public int getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(int timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }
    
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public void prepareNotify(final Exchange exchange) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Message inMsg = exchange.getIn();
        final Message outMsg = exchange.getOut();
        
        Date now = Calendar.getInstance().getTime();
        Date timeout = getTimeoutDateTime(now);

        final String createDateTimeString = simpleDateFormat.format(now);
        final String timeoutDateTimeString = simpleDateFormat.format(timeout);
        final String breadCrumbId = inMsg.getHeader(Exchange.BREADCRUMB_ID, String.class);
        final String businessTransactionId = breadCrumbId;

        outMsg.setHeader("IepSourceName", sourceName);
        outMsg.setHeader("IepTargetName", targetName);
        outMsg.setHeader("IepBusinessTransactionId", businessTransactionId);
        outMsg.setHeader("IepFrmCreateDateTime", createDateTimeString);
        outMsg.setHeader("IepFrmTimeoutDateTime", timeoutDateTimeString);
        outMsg.setHeader(Exchange.BREADCRUMB_ID, breadCrumbId); // For Denodo logging
        
        if  ((this.notes != null) && (this.notes.length() > 0)) {
            outMsg.setHeader("IepFrmNotes", this.notes);
        }
        outMsg.setHeader(Exchange.CONTENT_TYPE, MediaType.TEXT_PLAIN); // soag requires this
    }
    
    public void prepareResponse(final Exchange exchange) {
    	final Message inMsg = exchange.getIn();
        final Message outMsg = exchange.getOut();
        final String breadCrumbId = inMsg.getHeader(Exchange.BREADCRUMB_ID, String.class);
        final String businessTransactionId = breadCrumbId;    

        outMsg.setHeader("IepSourceName", sourceName);
        outMsg.setHeader("IepTargetName", targetName);
        outMsg.setHeader("IepBusinessTransactionId", businessTransactionId);
        outMsg.setHeader(Exchange.CONTENT_TYPE, MediaType.TEXT_PLAIN); // soag requires this
        outMsg.setHeader(Exchange.BREADCRUMB_ID, breadCrumbId); // For Denodo logging
    }

    private Date getTimeoutDateTime(final java.util.Date date) {
        final Calendar timeout = Calendar.getInstance();
        timeout.setTime(date);
        timeout.add(Calendar.MINUTE, this.timeoutMinutes);
        return timeout.getTime();
    }

}
