package com.lilly.esb.iep.alsc.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthHeaderProcessor implements Processor{

    private String soagUser;
    private String soagPwd;

    private final Logger LOGGER = LoggerFactory.getLogger(BasicAuthHeaderProcessor.class);

    /**
     * Sets a BasicAuth Authorization header based on the instances soagUser and soagPwd.
     */
    @Override
    public void process(Exchange exchange) throws Exception {

      /*  if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">>>> Entering BasicAuthHeaderProcessor.process(Exchange exchange)");
        }*/

    	LOGGER.info("$$$$$$$$$ Entering BasicAuthHeaderProcessor $$$$$$$$$");
    	
        String authorizationHeader = "Basic " + Base64.encodeBase64String(String.format("%s:%s", soagUser, soagPwd).getBytes("UTF-8"));
        exchange.getIn().setHeader("Authorization", authorizationHeader);

       /* if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">>>> Leaving BasicAuthHeaderProcessor.process(Exchange exchange)");
        }*/

    }

    public String getSoagUser() {
        return soagUser;
    }

    public void setSoagUser(String soagUser) {
        this.soagUser = soagUser;
    }

    public String getSoagPwd() {
        return soagPwd;
    }

    public void setSoagPwd(String soagPwd) {
        this.soagPwd = soagPwd;
    }
}
