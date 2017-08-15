package com.lilly.esb.iep.alsc.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lilly.esb.iep.alsc.common.ICamelStrings;
import com.lilly.esb.iep.alsc.common.util.IepGlobalConstants;

public class IepTrackerProcessor implements Processor {

	 /*
     * (non-Javadoc)
     * 
     * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
     */

	private final Logger LOGGER = LoggerFactory.getLogger(IepTrackerProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		LOGGER.info("$$$$$$$$$ Entering IepTrackerProcessor $$$$$$$$$");
				
		exchange.getOut().setHeader(ICamelStrings.MESSAGE_ID_HEADER, exchange.getIn().getHeader(ICamelStrings.MESSAGE_ID_HEADER));
		exchange.getOut().setHeader(IepGlobalConstants.IEP_HEADER_CALLBACK_URL, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_CALLBACK_URL));
		
		exchange.getOut().setHeader(Exchange.CONTENT_TYPE, exchange.getIn().getHeader(Exchange.CONTENT_TYPE));
		
		boolean hasTransId = false;
		if (null != exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_BUSINESS_TRANSACTION_ID)) {
			exchange.getOut().setHeader(IepGlobalConstants.IEP_HEADER_BUSINESS_TRANSACTION_ID, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_BUSINESS_TRANSACTION_ID));
			hasTransId = true;
		}
		
		boolean hasSourceName = false;
		if (null != exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_SOURCE_NAME)) {
			exchange.getOut().setHeader(IepGlobalConstants.IEP_HEADER_SOURCE_NAME, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_SOURCE_NAME));
			hasSourceName = true;
		}
		boolean hasClientId = false;
        if (null != exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_CLIENTID)) {
            exchange.getOut().setHeader(IepGlobalConstants.IEP_HEADER_CLIENTID, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_CLIENTID));
            hasClientId = true;
        }
		
		if (null != exchange.getIn().getHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_TRANSID)) {
			exchange.getOut().setHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_TRANSID, exchange.getIn().getHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_TRANSID));
		} else if (hasTransId) {
			exchange.getOut().setHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_TRANSID, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_BUSINESS_TRANSACTION_ID));
		}
		
		if (null != exchange.getIn().getHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_NAME)) {
			exchange.getOut().setHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_NAME, exchange.getIn().getHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_NAME));
		} else if (hasSourceName) {
            exchange.getOut().setHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_NAME, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_SOURCE_NAME));
        } else if (hasClientId) {
            exchange.getOut().setHeader(IepGlobalConstants.DISPATCH_HEADER_PARTNER_NAME, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_CLIENTID));
        }
		
		if (null != exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_MESSAGEID)) {
			exchange.getOut().setHeader(IepGlobalConstants.IEP_HEADER_MESSAGEID, exchange.getIn().getHeader(IepGlobalConstants.IEP_HEADER_MESSAGEID));
		}
			
		
		exchange.getOut().setBody(exchange.getIn().getBody());

	}

}

