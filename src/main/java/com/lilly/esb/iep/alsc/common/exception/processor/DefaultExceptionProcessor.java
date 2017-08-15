package com.lilly.esb.iep.alsc.common.exception.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

import org.apache.camel.Exchange;
import org.apache.camel.component.http4.HttpOperationFailedException;
import org.apache.camel.processor.ErrorHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lilly.esb.iep.alsc.common.exception.CommandValidationException;
import com.lilly.esb.iep.alsc.common.exception.MqException;
import com.lilly.esb.iep.alsc.common.exception.ResponseValidationException;
import com.lilly.esb.iep.alsc.common.exception.UnknownOperationException;
import com.lilly.esb.iep.alsc.common.exception.ValidationException;
import com.lilly.esb.iep.alsc.common.exception.processor.DefaultExceptionProcessor;
import com.lilly.esb.iep.alsc.common.model.Request;
import com.lilly.esb.iep.alsc.common.model.Response;
import com.lilly.esb.iep.alsc.common.model.ResponseMessage;
import com.lilly.esb.iep.alsc.common.util.IepGlobalConstants;

@SuppressWarnings("deprecation")
public class DefaultExceptionProcessor implements ErrorHandler {
    
	private final static int RESP_MSG_CODE_400 = 400;
	private final static int RESP_MSG_CODE_404 = 404;
	private final static int RESP_MSG_CODE_500 = 500;
	private final static int RESP_MSG_CODE_503 = 503;
	
    //private final static int HTTP_RESPONSE_CODE_400 = 400;
    //private final static int HTTP_RESPONSE_CODE_404 = 404;
    //private final static int HTTP_RESPONSE_CODE_500 = 500;
    
	/** All Exceptions that extend GeneralException use this Milestone **/
    protected final static String GENERAL_EXCEPTION_MILESTONE = "GEN_EXCEP_MILESTONE&dispatch.includeBody=true";
    
    /** All Exceptions that extend ValidationException use this Milestone **/
    protected final static String VALIDATION_EXCEPTION_MILESTONE = "VALIDATION_FAILED_MILESTONE&dispatch.includeBody=true";
    
    /** Needs to be removed at some point since these are http only and based on deprecated Exceptions **/
    protected final static String UNKNOWN_EXCEPTION_MILESTONE = "UNKNOWN_OPER_MILESTONE&dispatch.includeBody=true";
    protected final static String HTTP_EXCEPTION_MILESTONE = "HTTP_OPER_EXECP_OCCURRED&dispatch.includeBody=true";
    
    private String logName;
    private String encryptedPassword;
    
    @SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultExceptionProcessor.class);

    @Override
    public final void process(Exchange exchange) throws Exception {
        if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
            Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            doProcess(exception, exchange);
        }
    }
    
    /**
     * Process the specific exception.  This will be overridden by subclasses!  Note that we call #log and #dispatch
     * here, rather than in #process itself.  Some overridden uses (ex: SoapExceptionProcessor's creation of
     * SoapFaults) need to keep logging and Dispatch from happening!
     * 
     * @param exception
     * @param exchange
     * @throws Exception
     */
    protected void doProcess(Exception exception, Exchange exchange) throws Exception {
        log(exchange);
        
        // ordered most-to-least specific
        // NOTE: This could theoretically be broken out into a polymorphic or enum-based "action" pattern.  However,
        // allowing DefaultExceptionProcessor to be extended makes that pattern more complicated.
        // Instead, keep it simple and easily overridden.
        if (exception instanceof org.apache.camel.ValidationException) {
        	dispatch(VALIDATION_EXCEPTION_MILESTONE, exchange);
        } else if (exception instanceof ValidationException) {
            dispatch(VALIDATION_EXCEPTION_MILESTONE, exchange);
        } else if (exception instanceof CommandValidationException) {
        	//Do not need a ServiceNow Ticket for this exception
            dispatch(VALIDATION_EXCEPTION_MILESTONE, exchange);
        } else if (exception instanceof ResponseValidationException) {
            dispatch(VALIDATION_EXCEPTION_MILESTONE, exchange);
        } else if (exception instanceof UnknownOperationException) {
            dispatch(UNKNOWN_EXCEPTION_MILESTONE, exchange);
        } else if (exception instanceof HttpOperationFailedException) {
            dispatch(HTTP_EXCEPTION_MILESTONE, exchange);
        } else {
            dispatch(GENERAL_EXCEPTION_MILESTONE, exchange);
        }
    }
    
    protected void log(Exchange exchange) {
        // com.lilly.esb.iep.log.name is set by the bundle's propertyPlaceholder
        exchange.getContext().createProducerTemplate().send(
                "log:" + logName + "?level=ERROR&showAll=true&showStackTrace=true&multiline=true", exchange);
    }
    
    protected void dispatch(String milestone, Exchange exchange) {
        
    	if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null) {
        	
            Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            
            // In Denodo, it's really helpful to have a clear picture of the Exception message and stacktrace.
            // Add as headers.
            exchange.getIn().setHeader(IepGlobalConstants.IEP_HEADER_EXCEPTION_MESSAGE, exception.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            exchange.getIn().setHeader(IepGlobalConstants.IEP_HEADER_EXCEPTION_STACKTRACE, sw.toString());
        }
        
        processServiceNowTicket(milestone, exchange);
    }
    
    private void processServiceNowTicket(final String milestone, Exchange exchange) {
    	
        Boolean sendServiceNowTicket = exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_EXCEPTION_SERVICENOW, Boolean.class);
        
        //By default, generate a serviceNow ticket unless told otherwise
        if(sendServiceNowTicket == null){
        	sendServiceNowTicket = true;
        }
        
        if (sendServiceNowTicket){
    		exchange.getContext().createProducerTemplate().send(
                "error:dispatch?dispatch.milestone=" + milestone + "&dispatch.encrypt=" + encryptedPassword, exchange);
    	} else {
    		exchange.getContext().createProducerTemplate().send(
                    "error:dispatch?dispatch.milestone=" + milestone + "&dispatch.suppressInc=true&dispatch.encrypt=" + encryptedPassword, exchange);
    	}
		
	}
    
    /**
     * This method will be used to generate an IEP ResponseMessage
     * 
     * @param exception
     * @param exchange
     * @return
     * @throws Exception
     */
    protected ResponseMessage generateResponseMessage(Exception exception, Exchange exchange) throws Exception {
    	
        ResponseMessage responseMessage = new ResponseMessage();
        
        Request request = new Request();
        responseMessage.setRequest(request);
        
        Response response = new Response();
        responseMessage.getResponse().add(response);
        
        /* Set the Received Date to the Request */
        Date created = exchange.getProperty(IepGlobalConstants.IEP_REQUEST_TIMESTAMP, Date.class);
        if (created == null) {
            // If our property wasn't set, default to the current route's timestamp.
            created = exchange.getProperty(Exchange.CREATED_TIMESTAMP, Date.class);
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(created);
        request.setReceivedDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
        
        /* Set the IEP Business Transaction Id to the Request */
        String transactionId = exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_BUSINESS_TRANSACTION_ID, String.class);
        request.setBusinessTransactionId(transactionId);
        
        /* Set the IEP Source Name to the Request */
        String source = exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_SOURCE_NAME, String.class);
        request.setSourceName(source);
        
        /* The Response should allows be in error */
        response.setError(true);
        
        /* Set the Iep Target Name to the Response */
        String targetName = exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_TARGET_NAME, String.class);
        response.setTargetName(targetName);
        
        /* Set the statusCode from the doProcess method or default */
        response.setStatus(getStatusCode(exception));
        
        /* Set the Exception Message to the Response */
        if (!StringUtils.isBlank(exception.getMessage())) {
        	response.setPayload(exception.getMessage());
        }
        
        return responseMessage;
    }
    
    protected int getStatusCode(Exception exception) {
        if (exception instanceof ValidationException
                || exception instanceof CommandValidationException
                || exception instanceof ResponseValidationException) {
            return RESP_MSG_CODE_400;
            //statusCode = HTTP_RESPONSE_CODE_400;
        } else if (exception instanceof UnknownOperationException) {
            return RESP_MSG_CODE_404;
            //statusCode = HTTP_RESPONSE_CODE_404;
        } else if (exception instanceof MqException) {
            return RESP_MSG_CODE_503;
        } else {
            return RESP_MSG_CODE_500;
        }
    }
    
    public void setLogName(String logName) {
        this.logName = logName;
    }
    
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

}