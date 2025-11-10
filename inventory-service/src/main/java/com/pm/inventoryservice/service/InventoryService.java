package com.pm.inventoryservice.service;
import com.pm.inventoryservice.model.InventoryBatch;
import com.pm.inventoryservice.service.factory.InventoryHandlerFactory;
import com.pm.inventoryservice.service.handler.InventoryHandler;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryHandlerFactory factory;

    public InventoryService(InventoryHandlerFactory factory)
    {
        this.factory=factory;
    }

    public List<InventoryBatch> getBatches(Long productId) {
        InventoryHandler handler = factory.getHandler("default");
        return handler.listBatchesByProduct(productId);
    }

    public void updateAfterOrder(Long productId, int quantity) {
        InventoryHandler handler = factory.getHandler("default");
        handler.updateAfterOrder(productId, quantity);
    }
}
