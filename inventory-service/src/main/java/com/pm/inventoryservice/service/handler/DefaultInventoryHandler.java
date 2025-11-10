package com.pm.inventoryservice.service.handler;

import com.pm.inventoryservice.model.InventoryBatch;
import com.pm.inventoryservice.repository.InventoryBatchRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("defaultInventoryHandler")
public class DefaultInventoryHandler implements InventoryHandler {

    private final InventoryBatchRepository inventoryBatchRepository;

    public DefaultInventoryHandler(InventoryBatchRepository inventoryBatchRepository) {
        this.inventoryBatchRepository = inventoryBatchRepository;
    }

    @Override
    public List<InventoryBatch> listBatchesByProduct(Long productId) {
        return inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
    }

    @Override
    @Transactional
    public boolean reserve(String batchNumber, int requestedQuantity) {
        List<InventoryBatch> inventoryBatches = inventoryBatchRepository.findAll();

        InventoryBatch targetBatch = inventoryBatches.stream()
                .filter(batch -> batch.getBatchNumber().equals(batchNumber))
                .findFirst()
                .orElse(null);

        if (targetBatch == null || targetBatch.getQuantity() < requestedQuantity) {
            return false;
        }

        int updatedQuantity = targetBatch.getQuantity() - requestedQuantity;
        targetBatch.setQuantity(updatedQuantity);
        inventoryBatchRepository.save(targetBatch);

        return true;
    }

    @Override
    @Transactional
    public void updateAfterOrder(Long productId, int orderedQuantity) {
        List<InventoryBatch> productBatches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);

        int remainingQuantityToConsume = orderedQuantity;

        for (InventoryBatch batch : productBatches) {
            if (remainingQuantityToConsume <= 0) {
                break;
            }

            int availableQuantity = batch.getQuantity();
            int quantityToDeduct = Math.min(availableQuantity, remainingQuantityToConsume);

            batch.setQuantity(availableQuantity - quantityToDeduct);
            remainingQuantityToConsume -= quantityToDeduct;

            inventoryBatchRepository.save(batch);
        }

        if (remainingQuantityToConsume > 0) {
            throw new IllegalStateException("Insufficient stock for product ID: " + productId);
        }
    }
}
