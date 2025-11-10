package com.pm.inventoryservice.service.factory;

import com.pm.inventoryservice.service.handler.InventoryHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InventoryHandlerFactory {

    private final ApplicationContext applicationContext;

    public InventoryHandlerFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public InventoryHandler getHandler(String handlerKey) {
        boolean isDefaultHandler = (handlerKey == null || handlerKey.isBlank() || "default".equalsIgnoreCase(handlerKey));

        if (isDefaultHandler) {
            return applicationContext.getBean("defaultInventoryHandler", InventoryHandler.class);
        }

        // Future extension point for custom handlers
        return applicationContext.getBean("defaultInventoryHandler", InventoryHandler.class);
    }
}
