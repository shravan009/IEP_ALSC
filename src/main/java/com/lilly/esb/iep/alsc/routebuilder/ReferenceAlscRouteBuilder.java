/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.routebuilder;

import java.net.HttpURLConnection;

import javax.ws.rs.core.MediaType;

import org.apache.camel.Exchange;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.component.http4.HttpMethods;

import com.lilly.esb.iep.alsc.Constants;
import com.lilly.esb.iep.alsc.model.reference.ALSCMasterRefData;
import com.lilly.esb.iep.alsc.common.util.IepGlobalConstants;


public class ReferenceAlscRouteBuilder extends AbstractAlscRouteBuilder {
    
    @Override
    public void configure() throws Exception {
        super.configure();

        rest("/alsc/reference").get("").route().id("restroute.get.alsc.reference")
        .to("direct:alsc-route:"+ "{{com.lilly.esb.iep.alsc.reference.scheduler.queue}}")
        .convertBodyTo(String.class).endRest();
        
        rest("{{com.lilly.esb.iep.alsc.ref.final.resp.address}}/masterref/response/v3")
        	.consumes(MediaType.APPLICATION_XML)
        	.produces(MediaType.TEXT_PLAIN)
        	.post("finalresp")
        		.route().id("restroute.get.alsc.reference.final.resp")
        		.to("direct:alsc-route:referenceResponseRsEndpoint")
        		.endRest();   
        
        //
        // Retrieve the studyIds from Sharepoint
        //
        addSchedulerSharepointRouteWithDefaultTherapeuticArea("{{com.lilly.esb.iep.alsc.reference.scheduler.queue}}",
                "{{com.lilly.esb.iep.alsc.reference.studylist.query}}");

        //
        // For Each studyId: query database, build xml, send to ALSC
        //

        from("direct:alsc-" + datasourceName() + ".split").routeId("alsc-" + datasourceName() + "-integration.extract-study-route").errorHandler(noErrorHandler())
            // Jira 2000 - breadcrumbId header is sent to ALSC should be unique for each study id for FRM tracking
        	.choice()
            	.when(header(Exchange.SPLIT_INDEX).isNotNull())
					.setHeader(Exchange.BREADCRUMB_ID, header(Exchange.BREADCRUMB_ID).append("_").append(property(Exchange.SPLIT_INDEX)))
				.endChoice()
			.end()
			
            // To be safe and remove the IBM MQ noise, clean up headers using TrackerProcessor
            .processRef("trackerProcessor")
           // .wireTap(dispatchUri("SPLIT", true))
            .log("$$$$$$$$$$ SPLIT $$$$$$$$$$")
            
            // Save off SharePoint results so it can be parsed twice (below).. since parsing results are set as body
            .setProperty(IepGlobalConstants.IEP_PROPERTY_ORIGINAL_MESSAGE, body())

            // Query IIP and build the payload.
            .setHeader(Constants.ALSC_STUDY_ID, 
            		xpath("//d:StudyAlias/text()").saxon()
            		.namespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices"))
            		
            // Restore the original body to be parsed again below
            .setBody(property(IepGlobalConstants.IEP_PROPERTY_ORIGINAL_MESSAGE))
            		
            .setHeader(Constants.ALSC_DEFAULT_THERAPEUTIC_AREA, 
            		xpath("//d:DefaultTherapeuticArea/text()").saxon()
            		.namespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices"))
            		
            //.wireTap(dispatchUri("GENERATING_ALSC_PAYLOAD", true))
            .log("$$$$$$$$$$ SPLIT $$$$$$$$$$")
            .beanRef("masterReferenceService", "generateAndMarshallMasterReferenceAlscPayload")
            //.wireTap(dispatchUri("ALSC_PAYLOAD_GENERATED", true))
            .log("$$$$$$$$$$ ALSC_PAYLOAD_GENERATED $$$$$$$$$$")
            
            //.wireTap(dispatchUri("PREPARING_ALSC_MSTRREF_WEBSVC_CALL", false))
            .log("$$$$$$$$$$ PREPARING_ALSC_MSTRREF_WEBSVC_CALL $$$$$$$$$$")
            .processRef("basicAuthHeaderProcessor")
            .setHeader(Exchange.CONTENT_TYPE, simple(MediaType.APPLICATION_XML + ";charset=UTF-8"))
            .setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
            //.setHeader(IepGlobalConstants.IEP_HEADER_CALLBACK_URL, simple("{{com.lilly.esb.iep.alsc.reference.endpoint.uri}}"))
            //.wireTap(dispatchUri("ALSC_MSTRREF_WEBSVC_CALL_PREPARED", true))
            .log("$$$$$$$$$$ ALSC_MSTRREF_WEBSVC_CALL_PREPARED $$$$$$$$$$")
            
            // Send the XML payload to ALSC
            //.wireTap(dispatchUri("POSTING_MSTRREF_DATA_TO_ALSC", true))
            .log("$$$$$$$$$$ POSTING_MSTRREF_DATA_TO_ALSC $$$$$$$$$$")
            .convertBodyTo(ALSCMasterRefData.class, "UTF-8")
            // Call the web service and don't throw exception regardless of response code
            .inOut("https4://{{com.lilly.esb.iep.alsc.reference.soag.proxy.endpoint.uri}}?throwExceptionOnFailure=false").id("post-masterref-data-to-alsc")
            //.wireTap(dispatchUri("PROCESSING_MSTRREF_DATA_POST_RESPONSE", true))
            .log("$$$$$$$$$$ PROCESSING_MSTRREF_DATA_POST_RESPONSE $$$$$$$$$$")
            .to("direct:process-alsc-initial-response-status-code")

            // Notify FinalResponseMonitor only on 200 response
            .choice()
            	.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_OK)))
					.wireTap("direct:frm-notify-command")
				.endChoice()
				.otherwise()
					//.wireTap(dispatchUri("NON_200_HTTP_CODE_NOT_NOTIFYING_FRM", true)).id("non-200-status-code-wiretap")
					.log("$$$$$$$$$$ NON_200_HTTP_CODE_NOT_NOTIFYING_FRM $$$$$$$$$$")
				.endChoice()
			.end()
            ;

       // from("cxfrs:bean:referenceResponseRsEndpoint?bindingStyle=SimpleConsumer").routeId("referenceResponseWS")
          from("direct:alsc-route:referenceResponseRsEndpoint").routeId("referenceResponseWS")
           	//.wireTap(dispatchUri("FINAL_RESPONSE_RECEIVED", true))
        	.log("$$$$$$$$$$ FINAL_RESPONSE_RECEIVED $$$$$$$$$$")
            .removeHeader(Exchange.CONTENT_LENGTH) // Need to remove this header this to avoid Camel bug that uses this as response content length
            .choice()
                .when(header(Exchange.BREADCRUMB_ID).isNotNull())
                	//.wireTap(dispatchUri("FINAL_RESPONSE_NOTIFYING_FRM", false))
                	.log("$$$$$$$$$$ FINAL_RESPONSE_NOTIFYING_FRM $$$$$$$$$$")
                	//.log("FINAL_RESPONSE_NOTIFYING_FRM") // Camel won't let you do two wiretaps in a row - need to get past one with this useless logging
                	.wireTap("direct:frm-notify-response")
                	//.log("PROCESSING_FINAL_RESP_MSG") // Camel won't let you do two wiretaps in a row - need to get past one with this useless logging
                	//.wireTap(dispatchUri("PROCESSING_FINAL_RESP_MSG", false))
                	.log("$$$$$$$$$$ PROCESSING_FINAL_RESP_MSG $$$$$$$$$$")
                	.beanRef("masterReferenceService", "processMasterReferenceAlscFinalResponseMessage").id("master-ref-svc-process-master-ref-alsc-final-resp-msg")
                	// Have to split nested if statements into direct routes to get it to work (camel bug)
                	.to("direct:process-final-response-processing-results")
                .endChoice()
                .otherwise()
                	//.wireTap(dispatchError("ALSC_REFERENCE_NO_BREADCRUMBID_HEADER_MILESTONE", true))
                	.log("$$$$$$$$$$ ALSC_REFERENCE_NO_BREADCRUMBID_HEADER_MILESTONE $$$$$$$$$$")
                	.setBody(constant("Error: " + Exchange.BREADCRUMB_ID + " header not found in final response http request"))
                	.setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN))
                	.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(HttpURLConnection.HTTP_BAD_REQUEST)).id("set-bad-request-http-resp-code")
                .endChoice()
            .end()
            ;
        
        from("direct:process-alsc-initial-response-status-code").errorHandler(noErrorHandler()).routeId("direct-process-alsc-initial-response-status-code")
        	.choice()
        		.when(header(Exchange.HTTP_RESPONSE_CODE).isNull())
        			//.wireTap(dispatchError("ALSC_REFERENCE_REC_NULL_HTTP_RESP_CODE_MILESTONE", true)).id("null-response-code-wiretap")
        			.log("$$$$$$$$$$ ALSC_REFERENCE_REC_NULL_HTTP_RESP_CODE_MILESTONE $$$$$$$$$$")
        		.endChoice()
        		.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_OK)))
					//.wireTap(dispatchUri("REC_200_HTTP_CODE_FROM_ALSC", true)).id("200-response-code-wiretap")
        			.log("$$$$$$$$$$ REC_200_HTTP_CODE_FROM_ALSC $$$$$$$$$$")	
				.endChoice()
				.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_BAD_REQUEST)))
					//.wireTap(dispatchError("ALSC_REFERENCE_REC_400_HTTP_CODE_FROM_ALSC_MILESTONE", true)).id("400-response-code-wiretap")
					.log("$$$$$$$$$$ ALSC_REFERENCE_REC_400_HTTP_CODE_FROM_ALSC_MILESTONE $$$$$$$$$$")	
				.endChoice()
				.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_UNAUTHORIZED)))
					//.wireTap(dispatchError("ALSC_REFERENCE_REC_401_HTTP_CODE_FROM_ALSC_MILESTONE", true)).id("401-response-code-wiretap")
					.log("$$$$$$$$$$ ALSC_REFERENCE_REC_401_HTTP_CODE_FROM_ALSC_MILESTONE $$$$$$$$$$")
				.endChoice()
				.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_BAD_METHOD)))
					//.wireTap(dispatchError("ALSC_REFERENCE_REC_405_HTTP_CODE_FROM_ALSC_MILESTONE", true)).id("405-response-code-wiretap")
					.log("$$$$$$$$$$ ALSC_REFERENCE_REC_405_HTTP_CODE_FROM_ALSC_MILESTONE $$$$$$$$$$")
				.endChoice()
				.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(constant(HttpURLConnection.HTTP_INTERNAL_ERROR)))
					//.wireTap(dispatchError("ALSC_REFERENCE_REC_500_HTTP_CODE_FROM_ALSC_MILESTONE", true)).id("500-response-code-wiretap")
					.log("$$$$$$$$$$ ALSC_REFERENCE_REC_500_HTTP_CODE_FROM_ALSC_MILESTONE $$$$$$$$$$")
				.endChoice()
        		.otherwise()
        			//.wireTap(dispatchError("ALSC_REFERENCE_REC_BAD_HTTP_CODE_FROM_ALSC_MILESTONE", true)).id("unknown-response-code-wiretap")
        			.log("$$$$$$$$$$ ALSC_REFERENCE_REC_BAD_HTTP_CODE_FROM_ALSC_MILESTONE $$$$$$$$$$")
				.endChoice()
			.end()
			;
        
        // Have to split nested if statements into direct routes to get it to work (camel bug)
        from("direct:process-final-response-processing-results").errorHandler(noErrorHandler()).routeId("direct-process-final-response-processing-results")
        	.choice()
        		.when(PredicateBuilder.and(property(Constants.ALSC_CREATE_SNOW_TICKET_PROPERTY).isNotNull(), property(Constants.ALSC_CREATE_SNOW_TICKET_PROPERTY).isEqualTo(Boolean.TRUE)))
        			//.wireTap(dispatchError("ALSC_REFERENCE_INVLD_OR_FAILURE_FINAL_RESP_MILESTONE", true)).id("alsc-invlid-or-failure-final-response-message-received")
        			.log("$$$$$$$$$$ ALSC_REFERENCE_INVLD_OR_FAILURE_FINAL_RESP_MILESTONE $$$$$$$$$$")
        		.endChoice()
				.otherwise()
					//.wireTap(dispatchUri("SUCCESS_FINAL_RESP_RECEIVED", true)).id("alsc-success-final-response-message-received")
					.log("$$$$$$$$$$ SUCCESS_FINAL_RESP_RECEIVED $$$$$$$$$$")
				.endChoice()
			.end()
			;
        
        from("direct:frm-notify-command").errorHandler(noErrorHandler()).routeId("direct-frm-notify-command")
        	.beanRef("finalResponseService", "prepareNotify")
            .setHeader(IepGlobalConstants.IEP_HEADER_CALLBACK_URL, simple("{{com.lilly.esb.iep.alsc.final.response.monitor.commandUrl}}"))
            //.wireTap(dispatchUri("FRM_COMMAND_NOTIFY", true))
            .log("$$$$$$$$$$ FRM_COMMAND_NOTIFY $$$$$$$$$$")
            //.recipientList(simple("wmqOut:queue:{{com.lilly.esb.iep.alsc.endpoint.queue.tracker}}?disableReplyTo=true")).id("tracker-frm-notify")
            //.wireTap(dispatchUri("FRM_COMMAND_NOTIFIED", false))
            .log("$$$$$$$$$$ FRM_COMMAND_NOTIFIED $$$$$$$$$$")
        	;
        
        from("direct:frm-notify-response").errorHandler(noErrorHandler()).routeId("direct-frm-notify-response")
	        // Respond to FinalResponseMonitor
	        .beanRef("finalResponseService", "prepareResponse")
	        .setHeader(IepGlobalConstants.IEP_HEADER_CALLBACK_URL, simple("{{com.lilly.esb.iep.alsc.final.response.monitor.responseUrl}}"))
	        //.wireTap(dispatchUri("FRM_RESPONSE_NOTIFY", true))
	        .log("$$$$$$$$$$ FRM_RESPONSE_NOTIFY $$$$$$$$$$")
	        //.recipientList(simple("wmqOut:queue:{{com.lilly.esb.iep.alsc.endpoint.queue.tracker}}?disableReplyTo=true")).id("tracker-frm-response")
	        //.wireTap(dispatchUri("FRM_RESPONSE_NOTIFIED", false))
	        .log("$$$$$$$$$$ FRM_RESPONSE_NOTIFIED $$$$$$$$$$")
            ;
    }
    
    @Override
    protected String datasourceName() {
        return "REFERENCE";
    }

}
