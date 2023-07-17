package sk.stuba.fei.uim.vsa.pr2;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr2.ApplicationConfiguration.BASE_URI;

public class SanityCheckTest {

    private static final Logger log = LoggerFactory.getLogger(SanityCheckTest.class);
    private static HttpServer server;
    private static WebTarget client;

    @BeforeAll
    static void setup() {
        log.info("Starting HTTP server for testing");
        server = Project2Application.startServer();
        log.info("Creating client for HTTP server");
        client = ClientBuilder.newClient().target(BASE_URI);
        log.info("Client created " + client.toString());
    }

    @AfterAll
    static void cleaning() {
        log.info("Cleaning after the test");
        client = null;
        server.shutdownNow();
    }

    @Test
    void testForResourcesAvailability() {
        try {
            assertNotNull(server);
            assertNotNull(client);
            log.info("Checking if server started correctly");
            assertTrue(server.isStarted());
            log.info("Requesting for generated WADL definition on " + BASE_URI + "/application.wadl");
            String content = client.path("application.wadl").request().get(String.class);
            assertNotNull(content);
            assertTrue(content.contains("<application xmlns=\"http://wadl.dev.java.net/2009/02\">"));
            assertTrue(content.contains("<resources base=\"" + BASE_URI + (BASE_URI.endsWith("/") ? "" : "/") + "\">"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testValidityOfPOMFile() {
        log.info("Checking if student is set as developer in pom.xml");
        File pom = new File("pom.xml");
        assertNotNull(pom);
        try (Stream<String> lineStream = Files.lines(pom.toPath())) {
            List<String> lines = lineStream
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .collect(Collectors.toList());
            boolean developerTag = false;
            for (String line : lines) {
                if (line.contains("</developers>")) {
                    break;
                }
                if (line.contains("<developer>")) {
                    developerTag = true;
                    continue;
                }
                if (developerTag) {
                    if (line.contains("<id>")) {
                        assertFalse(line.contains("999999"));
                        log.info("Found student id " + line);
                    }
                    if (line.contains("<name>")) {
                        assertFalse(line.contains("Meno Študenta"));
                        log.info("Found student name " + line);
                    }
                    if (line.contains("<email>")) {
                        assertFalse(line.contains("xstudent@stuba.sk"));
                        log.info("Found student email " + line);
                    }
                }
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testPersistentUnit() {
        log.info("Checking for definition of persistence.xml");
        File pom = new File(String.join(File.separator, "src", "main", "resources", "META-INF", "persistence.xml"));
        assertNotNull(pom);
        try (Stream<String> lineStream = Files.lines(pom.toPath())) {
            AtomicBoolean containsClass = new AtomicBoolean(false);
            AtomicBoolean hasPUName = new AtomicBoolean(false);
            lineStream.filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .forEach(l -> {
                        if (l.contains("<class>")) {
                            containsClass.set(true);
                        }
                        if (l.contains("<persistence-unit")) {
                            int i = l.indexOf("name=\"");
                            if (i != -1) {
                                int j = l.indexOf("\"", i + 6);
                                if (j != -1) {
                                    hasPUName.set(l.substring(i + 6, j).equals("vsa-project-2"));
                                }
                            }
                        }
                    });
            log.info("Entity classes were detected: " + containsClass);
            log.info("Persistent unit has correct name: " + hasPUName);
            assertTrue(containsClass.get() && hasPUName.get());
        } catch (Exception e) {
            fail(e);
        }
    }
}
