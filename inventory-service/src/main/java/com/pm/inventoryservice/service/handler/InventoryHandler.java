package com.pm.inventoryservice.service.handler;

import com.pm.inventoryservice.model.InventoryBatch;
import jakarta.transaction.Transactional;

import java.util.List;

public interface InventoryHandler {
    List<InventoryBatch> listBatchesByProduct(Long productId);
    boolean reserve(String batchNumber, int quantity);
    void updateAfterOrder(Long productId, int quantity);
}
