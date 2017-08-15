/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.routebuilder;

import com.lilly.esb.iep.alsc.Constants;
import com.lilly.esb.iep.alsc.Constants.DmwInterfaceType;

/**
 * @author Brett Meyer
 * @author David Heitzer
 */
public class EctsAlscRouteBuilder extends AbstractAlscRouteBuilder {
	
	private static final String ECTS_CSV_GENERATOR_BEAN_REF = "ectsCsvGenerator";
	
	@Override
    public void configure() throws Exception {
        super.configure();
        
        rest("/alsc/ects").get("").route().id("restroute.get.alsc.ects")
        .to("direct:alsc-route:"+ "{{com.lilly.esb.iep.alsc.ects.scheduler.queue}}")
        .convertBodyTo(String.class).endRest();
        
        addSchedulerSharepointRoute("{{com.lilly.esb.iep.alsc.ects.scheduler.queue}}",
        		"{{com.lilly.esb.iep.alsc.ects.studylist.query}}");
        
        /* Need to create custom eCTS route here (rather than using addCsvBuilderRoute())
         * since for eCTS we generate and send two .csvs per study id (one for general 
         * (blinded) and one for secure (unblinded)).
         */
        from("direct:alsc-" + datasourceName() + ".split").routeId("alsc-" + datasourceName() + "-integration.extract-study-route")
        	//.wireTap(dispatchUri("SPLIT", true))
        	.log("$$$$$$$$$$ SPLIT $$$$$$$$$$")
        	.setHeader(Constants.ALSC_STUDY_ID).body(String.class)
        	.wireTap("direct:generateAndSendFesGeneralCsvs")
        	.wireTap("direct:generateAndSendFesSecureCsvs")
        ;
        
        from("direct:generateAndSendFesSecureCsvs").routeId("direct-generate-and-send-fes-secure-csvs")
        	//.wireTap(dispatchUri("GENERATING_ECTS_SECURE_CSV", true))
        	.log("$$$$$$$$$$ GENERATING_ECTS_SECURE_CSV $$$$$$$$$$")
        	.setProperty(Constants.ALSC_DMW_INTERFACE_TYPE_PROPERTY, constant(DmwInterfaceType.SECURE))
        	.beanRef(ECTS_CSV_GENERATOR_BEAN_REF, "generateCsv")
        	//.wireTap(dispatchUri("SENDING_ECTS_SECURE_CSV_TO_FES", true))
        	.log("$$$$$$$$$$ SENDING_ECTS_SECURE_CSV_TO_FES $$$$$$$$$$")
        	//.inOnly("wmqOut:queue:{{com.lilly.esb.iep.alsc.fes.queue}}?disableReplyTo=true")
        ;
        
        from("direct:generateAndSendFesGeneralCsvs").routeId("direct-generate-and-send-fes-general-csvs")
        	//.wireTap(dispatchUri("GENERATING_ECTS_GENERAL_CSV", true))
        	.log("$$$$$$$$$$ GENERATING_ECTS_GENERAL_CSV $$$$$$$$$$")
        	.setProperty(Constants.ALSC_DMW_INTERFACE_TYPE_PROPERTY, constant(DmwInterfaceType.GENERAL))
        	.beanRef(ECTS_CSV_GENERATOR_BEAN_REF, "generateCsv")
        	//.wireTap(dispatchUri("SENDING_ECTS_GENERAL_CSV_TO_FES", true))
        	.log("$$$$$$$$$$ SENDING_ECTS_GENERAL_CSV_TO_FES $$$$$$$$$$")
        	//.inOnly("wmqOut:queue:{{com.lilly.esb.iep.alsc.fes.queue}}?disableReplyTo=true")
        ;
        
    }

    @Override
    protected String datasourceName() {
        return "ECTS";
    }

}
