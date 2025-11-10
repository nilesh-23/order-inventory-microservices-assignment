package com.pm.orderservice.service;

import com.pm.orderservice.client.InventoryBatchDTO;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl = "http://localhost:8081/inventory";

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    public Order placeOrder(Order order) {
        order.setStatus("CREATED");
        orderRepository.save(order);

        String fetchInventoryUrl = inventoryServiceUrl + "/" + order.getProductId();
        ResponseEntity<InventoryBatchDTO[]> inventoryResponse;

        try {
            inventoryResponse = restTemplate.getForEntity(fetchInventoryUrl, InventoryBatchDTO[].class);
        } catch (Exception exception) {
            order.setStatus("FAILED");
            return orderRepository.save(order);
        }

        InventoryBatchDTO[] inventoryBatches = inventoryResponse.getBody();
        int totalAvailableQuantity = 0;

        if (inventoryBatches != null) {
            for (InventoryBatchDTO batch : inventoryBatches) {
                totalAvailableQuantity += batch.getQuantity();
            }
        }

        if (totalAvailableQuantity < order.getQuantity()) {
            order.setStatus("FAILED");
            return orderRepository.save(order);
        }

        Map<String, Object> updateRequest = Map.of(
                "productId", order.getProductId(),
                "quantity", order.getQuantity()
        );

        try {
            ResponseEntity<Map> updateResponse = restTemplate.postForEntity(
                    inventoryServiceUrl + "/update",
                    updateRequest,
                    Map.class
            );

            boolean updateSucceeded = updateResponse.getStatusCode().is2xxSuccessful()
                    && updateResponse.getBody() != null
                    && Objects.equals(updateResponse.getBody().get("status"), "success");

            order.setStatus(updateSucceeded ? "COMPLETED" : "FAILED");
        } catch (Exception exception) {
            order.setStatus("FAILED");
        }

        return orderRepository.save(order);
    }
}
