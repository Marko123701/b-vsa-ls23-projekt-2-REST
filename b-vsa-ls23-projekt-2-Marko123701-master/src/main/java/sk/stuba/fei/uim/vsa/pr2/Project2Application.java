package sk.stuba.fei.uim.vsa.pr2;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static sk.stuba.fei.uim.vsa.pr2.ApplicationConfiguration.BASE_URI;

public class Project2Application {

    private static final Logger log = LoggerFactory.getLogger(Project2Application.class);

    public static HttpServer startServer() {
        final ResourceConfig rc = ResourceConfig.forApplicationClass(JAXRSApplicationConfiguration.class);
        log.info("Starting Grizzly2 HTTP server...");
        log.info("Server listening on " + BASE_URI);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Shutting down the application...");
                    server.shutdownNow();
                    log.info("Exiting");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }));
            log.info("Last steps of setting up the application...");
            postStart();
            log.info(String.format("Application started.%nStop the application using CRL+C"));
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }


    public static void postStart() {
        // TODO sem napíš akékoľvek nastavenia, či volania, ktoré sa majú udiať ihneď po štarte servera

    }


}
