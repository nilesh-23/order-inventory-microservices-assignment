package com.pm.inventoryservice.controller;

import com.pm.inventoryservice.model.InventoryBatch;
import com.pm.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryBatch>> getBatches(@PathVariable Long productId) {
        List<InventoryBatch> productBatches = inventoryService.getBatches(productId);
        return ResponseEntity.ok(productBatches);
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateAfterOrder(@RequestBody Map<String, Object> requestBody) {
        try {
            Long productId = Long.parseLong(requestBody.get("productId").toString());
            int orderedQuantity = Integer.parseInt(requestBody.get("quantity").toString());

            inventoryService.updateAfterOrder(productId, orderedQuantity);

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (IllegalStateException exception) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", exception.getMessage()
            ));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", exception.getMessage()
            ));
        }
    }
}
