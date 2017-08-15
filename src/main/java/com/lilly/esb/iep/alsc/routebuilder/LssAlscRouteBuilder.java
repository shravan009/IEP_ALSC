/**
 * Copyright (c) 2016 Eli Lilly and Company.  All rights reserved.
 */
package com.lilly.esb.iep.alsc.routebuilder;

/**
 * @author Brett Meyer
 */
public class LssAlscRouteBuilder extends AbstractAlscRouteBuilder {
	
	@Override
    public void configure() throws Exception {
        super.configure();
        
        rest("/alsc/lss").get("").route().id("restroute.get.alsc.lss")
        .to("direct:alsc-route:"+ "{{com.lilly.esb.iep.alsc.lss.scheduler.queue}}")
        .convertBodyTo(String.class).endRest();
        
        addSchedulerSharepointRoute("{{com.lilly.esb.iep.alsc.lss.scheduler.queue}}",
        		"{{com.lilly.esb.iep.alsc.lss.studylist.query}}");
        addCsvBuilderRoute("lssCsvGenerator");
    }

    @Override
    protected String datasourceName() {
        return "LSS";
    }

}
