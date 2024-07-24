package com.example.PaymentOptimizationAlgorithm.IntegrationTests;

import com.example.PaymentOptimizationAlgorithm.Service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentService paymentService;

    @Test
    public void testAddBranchAndEdgeAndProcessPayment() {
        String baseUrl = "http://localhost:" + port + "/payment";

        restTemplate.postForObject(baseUrl + "/branch?branch=A&cost=5", null, Void.class);
        restTemplate.postForObject(baseUrl + "/branch?branch=B&cost=50", null, Void.class);
        restTemplate.postForObject(baseUrl + "/branch?branch=C&cost=10", null, Void.class);
        restTemplate.postForObject(baseUrl + "/branch?branch=D&cost=10", null, Void.class);
        restTemplate.postForObject(baseUrl + "/branch?branch=E&cost=20", null, Void.class);
        restTemplate.postForObject(baseUrl + "/branch?branch=F&cost=5", null, Void.class);

        restTemplate.postForObject(baseUrl + "/edge?from=A&to=B", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=A&to=C", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=C&to=B", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=B&to=D", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=C&to=E", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=D&to=E", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=E&to=D", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=D&to=F", null, Void.class);
        restTemplate.postForObject(baseUrl + "/edge?from=E&to=F", null, Void.class);

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/process?originBranch=A&destinationBranch=D", String.class);
        assertThat(response.getBody()).isEqualTo("A,C,E,D");
    }
}
