package com.pm.orderservice.controller;

import com.pm.orderservice.client.InventoryBatchDTO;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestTemplate realRestTemplate;

    @Autowired
    private OrderRepository repo;

    @LocalServerPort
    private int port;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        repo.deleteAll();
        mockServer = MockRestServiceServer.createServer(realRestTemplate);
    }

    @Test
    void placeOrder_shouldSucceed_whenInventoryAvailable() throws Exception {
        // Mock Inventory GET
        InventoryBatchDTO[] available = { new InventoryBatchDTO(1L, 100L, "B1", 10, null) };
        mockServer.expect(requestTo("http://localhost:8081/inventory/100"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(available),
                        MediaType.APPLICATION_JSON));

        // Mock Inventory POST update
        mockServer.expect(requestTo("http://localhost:8081/inventory/update"))
                .andRespond(withSuccess("{\"status\":\"success\"}", MediaType.APPLICATION_JSON));

        // Request to order endpoint
        Order req = new Order(null, 100L, 5, null);
        ResponseEntity<Order> res = restTemplate.postForEntity("http://localhost:" + port + "/order", req, Order.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo("COMPLETED");

        mockServer.verify();
    }

    @Test
    void placeOrder_shouldFail_whenInventoryUnavailable() throws Exception {
        // Mock Inventory GET (low stock)
        InventoryBatchDTO[] available = { new InventoryBatchDTO(1L, 200L, "B1", 3, null) };
        mockServer.expect(requestTo("http://localhost:8081/inventory/200"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(available),
                        MediaType.APPLICATION_JSON));

        // Request with more quantity than available
        Order req = new Order(null, 200L, 10, null);
        ResponseEntity<Order> res = restTemplate.postForEntity("http://localhost:" + port + "/order", req, Order.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo("FAILED");

        mockServer.verify();
    }

    @Test
    void placeOrder_shouldFail_whenInventoryServiceDown() {
        mockServer.expect(requestTo("http://localhost:8081/inventory/300"))
                .andRespond(withServerError());

        Order req = new Order(null, 300L, 5, null);
        ResponseEntity<Order> res = restTemplate.postForEntity("http://localhost:" + port + "/order", req, Order.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getStatus()).isEqualTo("FAILED");

        mockServer.verify();
    }
}
