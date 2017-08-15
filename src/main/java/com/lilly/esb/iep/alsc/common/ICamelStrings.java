package com.lilly.esb.iep.alsc.common;

import java.io.Serializable;

/**
 * The Interface ICamelStrings.
 */
public interface ICamelStrings extends Serializable {

    /** The Constant MESSAGE_ID_HEADER. */
    String MESSAGE_ID_HEADER = "breadcrumbId";
    
    /** The Rest call Operation Name */
    String REST_OPERATION_NAME = "operationName";
    
    /** The Rest call content-type */
    String CAMEL_ACCEPT_CONTENT_TYPE = "CamelAcceptContentType";
    
    /** the Camel JMS replyTo header */
    String JMS_REPLY_TO = "JMSReplyTo";
}
