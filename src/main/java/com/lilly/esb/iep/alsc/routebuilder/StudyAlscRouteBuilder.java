/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.routebuilder;

import javax.ws.rs.core.MediaType;

/**
 * @author Brett Meyer
 */
public class StudyAlscRouteBuilder extends AbstractAlscRouteBuilder {
	
	@Override
    public void configure() throws Exception {
        super.configure();
        
        rest("/alsc/study").get("").produces(MediaType.TEXT_PLAIN)
        .route().id("restroute.get.alsc.study")
        .to("direct:alsc-route:"+ "{{com.lilly.esb.iep.alsc.study.scheduler.queue}}")
        .convertBodyTo(String.class).endRest();
        
        addSchedulerSharepointRoute("{{com.lilly.esb.iep.alsc.study.scheduler.queue}}","{{com.lilly.esb.iep.alsc.study.studylist.query}}");
        addCsvBuilderRoute("studyCsvGenerator");
    }

    @Override
    protected String datasourceName() {
        return "STUDY";
    }

}
