package com.example.cerpshashkin;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseWireMockTest {

    protected WireMockServer wireMockServer;

    @BeforeEach
    void setUpWireMock() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterEach
    void tearDownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    protected String readJsonFile(String fileName) {
        try {
            return Files.readString(Paths.get("src/test/resources/test-data/" + fileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read test data file: " + fileName, e);
        }
    }
}
