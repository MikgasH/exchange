package com.example.cerpshashkin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseWireMockTest {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void setUpWireMockServer() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options().dynamicPort()
        );
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        System.out.println("WireMock started on port: " + wireMockServer.port());
    }

    @AfterAll
    static void tearDownWireMockServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("WireMock stopped");
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();

        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + wireMockServer.port();
        registry.add("api.fixer.url", () -> baseUrl);
        registry.add("api.exchangerates.url", () -> baseUrl);
        registry.add("api.currencyapi.url", () -> baseUrl);
    }


    protected String readJsonFile(final String fileName) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("test-data/" + fileName);

            if (resource == null) {
                throw new IllegalArgumentException("File not found: test-data/" + fileName);
            }

            return Files.readString(Paths.get(resource.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to read test data file: " + fileName, e);
        }
    }
}
