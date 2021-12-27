package com.fission.scan.filescan.valve;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AbstractAccessLogValve;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartException;

import com.trend.app_protect.AgentError;
import com.trend.app_protect.OverrideResponseError;

@Component
public class CustomizeEmbeddedTomcatContainer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

	private static final Logger logger = LoggerFactory.getLogger(CustomizeEmbeddedTomcatContainer.class);
	
    private static final String SUCCESS_CODE = "statuscode=200";
    private static final String URI = "uri=/services/mydocs";
    
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        logger.info("------------ CustomizeEmbeddedTomcatContainer ----------------");
        
        
        TomcatSlf4jAccessValve accessLogValve = new TomcatSlf4jAccessValve();
        accessLogValve.setEnabled(true);

        /**
         * for pattern format see https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/catalina/valves/AccessLogValve.html
         */
        //accessLogValve.setPattern("request: method=%m uri=\"%U\" response: statuscode=%s bytes=%b duration=%D(ms) client: remoteip=%a user=%u useragent=\"%{User-Agent}i\"");
        accessLogValve.setPattern("uri=\"%U\" statuscode=%s");
        factory.addContextValves(accessLogValve);
    }


    public static class TomcatSlf4jAccessValve extends AbstractAccessLogValve {

        Logger httpAccessLogLogger = LoggerFactory.getLogger("http_access_log");

        @Override
        protected void log(CharArrayWriter message) {
            httpAccessLogLogger.info(message.toString());
            
            String[] respLogArray = message.toString().split(" ");
            if((respLogArray[0].trim()).equalsIgnoreCase(URI))
            if(!(respLogArray[1].trim()).equalsIgnoreCase(SUCCESS_CODE)) {
                logger.warn("Unusual Activity, malicious file uploading: {}", message.toString());
                throw new RuntimeException("Virus detected. .. ... .....");
            }
        }
    }

}
