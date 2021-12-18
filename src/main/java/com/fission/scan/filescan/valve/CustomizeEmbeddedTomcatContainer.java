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
	
	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		logger.info("------------ CustomizeEmbeddedTomcatContainer ----------------");
		
		
		TomcatSlf4jAccessValve accessLogValve = new TomcatSlf4jAccessValve();
        accessLogValve.setEnabled(true);

        /**
         * for pattern format see https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/catalina/valves/AccessLogValve.html
         */
        accessLogValve.setPattern("request: method=%m uri=\"%U\" response: statuscode=%s bytes=%b duration=%D(ms) client: remoteip=%a user=%u useragent=\"%{User-Agent}i\"");
        factory.addContextValves(accessLogValve);
        
        CustomAgentErrorHandlerValve agentHandler = new CustomAgentErrorHandlerValve();
        factory.addContextValves(agentHandler);
    }


    public static class TomcatSlf4jAccessValve extends AbstractAccessLogValve {

        Logger httpAccessLogLogger = LoggerFactory.getLogger("http_access_log");

        @Override
        protected void log(CharArrayWriter message) {
            httpAccessLogLogger.info(message.toString());
        }
        
    }
    
    
    public static class CustomAgentErrorHandlerValve extends ValveBase{

    	@Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
        	logger.info("*********************");
    		logger.info("Valve invoke method().....");
    		Valve next = getNext();
    		
    		logger.info("Request : " + request.getRequestURI());
    		logger.info("Response: " + response.getMessage());
    		logger.info("next: " + next);
    		
    		if(null == next) {
    			return;
    		}
    		
    		try {
    			logger.info("try block ... " );
    			next.invoke(request, response);
    		}catch (ServletException ae) {
    			logger.info("catch block ..ServletException. " );
    			logger.error("error: " + ae.getMessage());
    			throw new AgentError("Error in file uploading....");
    		}catch (MultipartException ae) {
    			logger.info("catch block ..MultipartException. " );
    			logger.error("error: " + ae.getMessage());
    			throw new AgentError("Error in file uploading....");
    		}catch (AgentError ae) {
    			logger.info("catch block ... " );
    			logger.error("error: " + ae.getMessage());
    			throw new AgentError("Error in file uploading....");
    		}catch (OverrideResponseError oe) {
    			logger.info("catch block ... " );
    			logger.error("error: " + oe.getMessage());
    			throw new AgentError("Virus detected....");
    		}catch (Exception e) {
    			logger.error("Exception... error: " + e.getMessage());
    		}
        }
    	
    }

}
