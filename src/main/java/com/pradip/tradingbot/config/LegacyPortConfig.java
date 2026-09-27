package com.pradip.tradingbot.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacyPortConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector legacyConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        legacyConnector.setPort(8080);
        legacyConnector.setProperty("redirectPort", "8081");
        factory.addAdditionalTomcatConnectors(legacyConnector);
    }
}
