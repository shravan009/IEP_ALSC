/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.routebuilder;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lilly.esb.iep.alsc.Constants;

/**
 * Abstract RouteBuilder to be extended by all ALSC integrations.
 * 
 * @author Brett Meyer
 */
public abstract class AbstractAlscRouteBuilder extends RouteBuilder {
	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractAlscRouteBuilder.class);
	protected static final String PROTOCOL = "PROTOCOL";

	protected abstract String datasourceName();

	public void configure() throws Exception {
		restConfiguration().component("jetty").host("0.0.0.0")
		.port("{{rest.port}}")
		//.endpointProperty("handlers", "securityHandler")
		.bindingMode(RestBindingMode.off)
		.dataFormatProperty("prettyPrint", "true")
		.apiContextPath("/api-doc")
		.apiProperty("api.title", "User API")
		.apiProperty("api.version", "1.2.3")
		.apiProperty("cors", "true"); 
		
		onException(Throwable.class)
		.handled(true)
		.processRef("defaultExceptionProcessor");
	}

	/*
	 * protected String dispatchUri(String milestone, boolean includeBody) {
	 * return "audit:dispatch?dispatch.milestone=ALSC_" + datasourceName() + "_"
	 * + milestone +
	 * "_MILESTONE&dispatch.encrypt={{com.lilly.esb.iep.alsc.encrypt.password}}&dispatch.includeBody="
	 * + includeBody; }
	 * 
	 * protected String dispatchError(String milestone, boolean includeBody) {
	 * return "error:dispatch?dispatch.milestone=" + milestone +
	 * "&dispatch.encrypt={{com.lilly.esb.iep.alsc.encrypt.password}}&dispatch.includeBody="
	 * + includeBody; }
	 */

	protected void addSchedulerSharepointRouteWithDefaultTherapeuticArea(String schedulerQueue,
			String sharepointQuery) {

		// TODO: printXpathResults needs to be false since Camel can't handle
		// printing xml body. One day we should fix this the right way.
		addSchedulerSharepointRoute(schedulerQueue, sharepointQuery, "//m:properties", false);
	}

	protected void addSchedulerSharepointRoute(String schedulerQueue, String sharepointQuery) {
		addSchedulerSharepointRoute(schedulerQueue, sharepointQuery,
				"//d:StudyAlias[not(. = preceding::d:StudyAlias)]/text()", true);
	}

	protected void addSchedulerSharepointRoute(String schedulerQueue, String sharepointQuery, String xpath,
			boolean printXpathResults) {
		//from("wmqIn:queue:" + schedulerQueue).routeId("alsc-" + datasourceName() + "-integration.extract-route")
		from("direct:alsc-route:"+ schedulerQueue).routeId("alsc-" + datasourceName() + "-integration.extract-route")
				.log("$$$$$$$$$$ Entering AbstractAlscRouteBuilder: addSchedulerSharepointRoute $$$$$$$$$$")
				
				// To be safe and remove the IBM MQ noise, clean up headers
				// using TrackerProcessor
				.log("$$$$$$$$$$ Route to TrackerProcessor $$$$$$$$$$")
				.processRef("trackerProcessor")
				// .wireTap(dispatchUri("START", true))

				// clear the body, preventing issues with the proceeding https
				// call to SharePoint
				.setBody(constant(""))

				.log("$$$$$$$$$$ Route to BasicAuthHeaderProcessor  $$$$$$$$$$")
				.processRef("basicAuthHeaderProcessor")
				
				// .wireTap(dispatchUri("SHAREPOINT_QUERY", true))
				.log("$$$$$$$$$$ SHAREPOINT_QUERY $$$$$$$$$$")
				.inOut("https4://{{com.lilly.esb.iep.alsc.studylist.site}}?" + sharepointQuery)
				.convertBodyTo(String.class)
				.log("$$$$$$$$$$ SHAREPOINT_RESULTS $$$$$$$$$$")
				// .wireTap(dispatchUri("SHAREPOINT_RESULTS", true))
				
				// For an example of the SharePoint Atom feed and how this XPath
				// line plays into it, see
				// src/test/resources/SharepointList.xml
				.setBody(xpath(xpath).saxon()
						.namespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata")
						.namespace("d",	"http://schemas.microsoft.com/ado/2007/08/dataservices"))
				// .wireTap(dispatchUri("SHAREPOINT_RESULTS_XPATH",
				// printXpathResults))
				.log("$$$$$$$$$$ SHAREPOINT_RESULTS_XPATH $$$$$$$$$$")

				// The above XPath takes the SharePoint Atom feed and reduces it
				// to a simple list of study aliases.
				// Split that list of IDs, sending each to the alsc.split route
				.split(simple("${body}"))
				// we do NOT want to stop on exception - Jira 2180 -DAH
				.convertBodyTo(String.class)
				.inOut("direct:alsc-" + datasourceName() + ".split").end();
	}

	protected void addCsvBuilderRoute(String csvGeneratorRef) {
		from("direct:alsc-" + datasourceName() + ".split")
				.log("$$$$$$$$$$ In "+"alsc-" + datasourceName() + "-integration.extract-study-route $$$$$$$$$$")
				.routeId("alsc-" + datasourceName() + "-integration.extract-study-route")
				// .wireTap(dispatchUri("SPLIT", true))
				.log("$$$$$$$$$$ SPLIT $$$$$$$$$$")
				.setHeader(Constants.ALSC_STUDY_ID).body(String.class)

				// Query IIP and build the payloads.
				.log("$$$$$$$$$$ To csvGeneratorRef $$$$$$$$$$" + csvGeneratorRef)
				.beanRef(csvGeneratorRef, "generateCsv")

				.to("direct:sendFESLoad-" + datasourceName());

		from("direct:sendFESLoad-" + datasourceName()).routeId("sendFESLoad-" + datasourceName())
				// .wireTap(dispatchUri("FES", true))
				// Trigger FES through its queue.
				// NOTE: fesProcessor writes out the file. However, we had
				// issues in FES where Java 7's Files
				// class would return even though technically the file hadn't
				// completely replicated across
				// the NFS. Currently, FES has a 5 sec sleep built in when it
				// pulls these from the queue,
				// leaving "enough" time for full replication. But if that ever
				// changes, this
				// might need re-thought!
				// .inOnly("wmqOut:queue:{{com.lilly.esb.iep.alsc.fes.queue}}?disableReplyTo=true")
				.log("End Processing sendFESLoad-" + datasourceName())
				;

	}

}
