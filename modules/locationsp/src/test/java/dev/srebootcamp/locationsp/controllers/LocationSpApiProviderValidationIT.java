package dev.srebootcamp.locationsp.controllers;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import dev.srebootcamp.providerValidation.AbstractApiProviderValidationIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static dev.srebootcamp.locationsp.LocationSpFixture.testTransicsClient;

@ExtendWith(SpringExtension.class)
@Provider("edriverapp")
//@PactBroker(url = "https://validoc.pactflow.io")
@PactFolder("../../pacts/accepted")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySources({@PropertySource("classpath:application.properties"), @PropertySource("classpath:testing.properties")})
@EnableConfigurationProperties
public class LocationSpApiProviderValidationIT  {


    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext provider) {
        provider.verifyInteraction();
    }

    @Value("${server.port}")
    int serverPort;

    @Autowired
    LocationspController controller;

    @BeforeEach
    void before(PactVerificationContext context) {
        System.out.println("Port: " + serverPort);
        context.setTarget(new HttpTestTarget("localhost", serverPort));
        controller.transicsClient = testTransicsClient();
    }

    @State({"test state"})
    public void toDefaultState() {
        //doesnt do anything as the state is already set up
    }

}
