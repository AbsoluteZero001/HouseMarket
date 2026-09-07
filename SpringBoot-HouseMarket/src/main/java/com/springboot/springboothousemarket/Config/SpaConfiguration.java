package com.springboot.springboothousemarket.Config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * SPA fallback configuration for Vue Router (createWebHistory / HTML5 History mode).
 * <p>
 * Without this, navigating directly to a frontend route (e.g., /login, /tenant)
 * or refreshing the page would result in a 404 because Spring Boot has no
 * controller mapping for those paths.
 * <p>
 * This config registers a custom 404 error page that serves index.html,
 * allowing Vue Router to take over and render the correct view.
 */
@Configuration
public class SpaConfiguration {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> spaErrorPageCustomizer() {
        return factory -> factory.addErrorPages(
                new ErrorPage(HttpStatus.NOT_FOUND, "/index.html")
        );
    }
}
