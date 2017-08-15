/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc;

/**
 * @author Brett Meyer
 */
public class Constants {
	
	public static enum DmwInterfaceType {
		GENERAL,
		SECURE
	}
	
    public static final String ALSC_STUDY_ID = "ALSC_STUDY_ID";
    public static final String ALSC_DEFAULT_THERAPEUTIC_AREA  = "ALSC_DEFAULT_THERAPEUTIC_AREA";
    public static final String ALSC_DMW_INTERFACE_TYPE_PROPERTY = "ALSC_DMW_INTERFACE_TYPE";
    public static final String ALSC_CREATE_SNOW_TICKET_PROPERTY = "ALSC_CREATE_SNOW_TICKET";
}
