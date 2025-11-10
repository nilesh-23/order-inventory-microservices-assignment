package com.pm.orderservice.controller;

import com.pm.orderservice.model.Order;
import com.pm.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order orderRequest) {
        Order processedOrder = orderService.placeOrder(orderRequest);

        if ("COMPLETED".equals(processedOrder.getStatus())) {
            return ResponseEntity.ok(processedOrder);
        } else {
            return ResponseEntity.badRequest().body(processedOrder);
        }
    }
}
