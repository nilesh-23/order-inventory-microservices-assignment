package com.pm.orderservice.client;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InventoryBatchDTO {
    private Long id;
    private Long productId;
    private String batchNumber;
    private int quantity;
    private LocalDate expiryDate;
}
