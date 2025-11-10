package com.pm.inventoryservice.controller;

import com.pm.inventoryservice.model.InventoryBatch;
import com.pm.inventoryservice.repository.InventoryBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryBatchRepository repo;

    @BeforeEach
    void setup() {
        repo.deleteAll();
        repo.saveAll(List.of(
                new InventoryBatch(null, 100L, "B1", 10, LocalDate.now().plusDays(5)),
                new InventoryBatch(null, 100L, "B2", 20, LocalDate.now().plusDays(1)),
                new InventoryBatch(null, 101L, "B3", 15, LocalDate.now().plusDays(10))
        ));
    }

    @Test
    void getBatches_returnsSortedByExpiry() {
        ResponseEntity<InventoryBatch[]> response =
                restTemplate.getForEntity("/inventory/100", InventoryBatch[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        InventoryBatch[] batches = response.getBody();
        assertThat(batches).isNotNull();
        assertThat(batches.length).isEqualTo(2);

        // check that batches are sorted by expiry date ascending
        LocalDate first = batches[0].getExpiryDate();
        LocalDate second = batches[1].getExpiryDate();
        assertThat(first.isBefore(second) || first.isEqual(second)).isTrue();
    }

    @Test
    void updateAfterOrder_reducesQuantity_whenEnoughStock() {
        // before update
        List<InventoryBatch> before = repo.findByProductIdOrderByExpiryDateAsc(100L);
        int totalBefore = before.stream().mapToInt(InventoryBatch::getQuantity).sum();

        Map<String, Object> payload = Map.of("productId", 100L, "quantity", 5);

        ResponseEntity<Map> res = restTemplate.postForEntity("/inventory/update", payload, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("status", "success");

        // after update
        List<InventoryBatch> after = repo.findByProductIdOrderByExpiryDateAsc(100L);
        int totalAfter = after.stream().mapToInt(InventoryBatch::getQuantity).sum();
        assertThat(totalAfter).isEqualTo(totalBefore - 5);
    }

    @Test
    void updateAfterOrder_returnsError_whenInsufficientStock() {
        Map<String, Object> payload = Map.of("productId", 100L, "quantity", 999);
        ResponseEntity<Map> res = restTemplate.postForEntity("/inventory/update", payload, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).containsEntry("status", "error");
        assertThat(res.getBody().get("message").toString())
                .contains("Insufficient stock");
    }

    @Test
    void updateAfterOrder_returnsError_whenProductIdMissing() {
        // simulate missing productId key — expect BAD_REQUEST or parsing error
        Map<String, Object> payload = Map.of("quantity", 5);

        ResponseEntity<Map> res = restTemplate.postForEntity("/inventory/update", payload, Map.class);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
        assertThat(res.getBody()).containsEntry("status", "error");
    }
}
