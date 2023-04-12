package com.example.cscserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The root of the application.
 * @author Karl Clifford
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync
public class CscserverApplication {

    /**
     * Handles server logs.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(CscserverApplication.class);

    /**
     * The URL of the developer documentation.
     */
    private static final String DOCS_URI = "http://localhost:8080/docs.html";

    /**
     * The main method that starts the server.
     * @param args arguments passed in at runtime.
     */
    public static void main(String[] args) {
        // Check that this device has support for a web browser.
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            // This device supports web browsers, open the developer docs.
            try {
                Desktop.getDesktop().browse(new URI(DOCS_URI));
            } catch (IOException | URISyntaxException e) {
                // This device doesn't support a web browser, log the URL instead.
                LOG.info("Access the developer docs at " + DOCS_URI);
            }
        }
        // Start the server.
        SpringApplication.run(CscserverApplication.class, args);
    }

}
