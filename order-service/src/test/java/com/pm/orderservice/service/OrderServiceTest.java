package com.pm.orderservice.service;

import com.pm.orderservice.client.InventoryBatchDTO;
import com.pm.orderservice.model.Order;
import com.pm.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService service;

    @Test
    void placeOrder_successfulInventoryUpdate() {
        // Arrange
        Order order = new Order(null, 100L, 5, null);
        InventoryBatchDTO[] batches = {
                new InventoryBatchDTO(1L, 100L, "b1", 10, LocalDate.now().plusDays(10))
        };

        when(restTemplate.getForEntity(anyString(), eq(InventoryBatchDTO[].class)))
                .thenReturn(new ResponseEntity<>(batches, HttpStatus.OK));

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK));

        when(repo.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        // Act
        Order result = service.placeOrder(order);

        // Assert
        assertNotNull(result.getId());
        assertEquals("COMPLETED", result.getStatus());
        verify(repo, atLeastOnce()).save(any());
    }

    @Test
    void placeOrder_insufficientStockShouldFail() {
        // Arrange
        Order order = new Order(null, 200L, 50, null);
        InventoryBatchDTO[] batches = {
                new InventoryBatchDTO(1L, 200L, "b1", 10, LocalDate.now().plusDays(10))
        };

        when(restTemplate.getForEntity(anyString(), eq(InventoryBatchDTO[].class)))
                .thenReturn(new ResponseEntity<>(batches, HttpStatus.OK));

        when(repo.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Order result = service.placeOrder(order);

        // Assert
        assertEquals("FAILED", result.getStatus());
    }

    @Test
    void placeOrder_inventoryServiceDownShouldFail() {
        // Arrange
        Order order = new Order(null, 300L, 10, null);
        when(restTemplate.getForEntity(anyString(), eq(InventoryBatchDTO[].class)))
                .thenThrow(new RuntimeException("Inventory service unreachable"));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Order result = service.placeOrder(order);

        // Assert
        assertEquals("FAILED", result.getStatus());
    }
}
