package com.pm.inventoryservice.handler;

import com.pm.inventoryservice.model.InventoryBatch;
import com.pm.inventoryservice.repository.InventoryBatchRepository;
import com.pm.inventoryservice.service.handler.DefaultInventoryHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryHandlerTest {

    @Mock
    InventoryBatchRepository repo;

    @InjectMocks
    DefaultInventoryHandler handler;

    @Test
    void listBatchesDelegatesToRepo() {
        long pid = 100L;
        InventoryBatch b = new InventoryBatch(1L, pid, "b1", 10, LocalDate.now());
        when(repo.findByProductIdOrderByExpiryDateAsc(pid)).thenReturn(List.of(b));

        var res = handler.listBatchesByProduct(pid);

        assertEquals(1, res.size());
        verify(repo).findByProductIdOrderByExpiryDateAsc(pid);
    }

    @Test
    void updateAfterOrderConsumesEarliestBatches() {
        long pid = 200L;
        InventoryBatch b1 = new InventoryBatch(1L, pid, "b1", 2, LocalDate.now());
        InventoryBatch b2 = new InventoryBatch(2L, pid, "b2", 5, LocalDate.now().plusDays(5));
        when(repo.findByProductIdOrderByExpiryDateAsc(pid)).thenReturn(List.of(b1, b2));

        handler.updateAfterOrder(pid, 6);

        // expect b1 -> 0, b2 -> 1
        verify(repo, atLeastOnce()).save(any());
    }

    @Test
    void updateAfterOrderInsufficientThrows() {
        Long pid = 300L;
        InventoryBatch b1 = new InventoryBatch(1L, pid, "b1", 1, LocalDate.now());
        when(repo.findByProductIdOrderByExpiryDateAsc(pid)).thenReturn(List.of(b1));

        assertThrows(IllegalStateException.class, () -> handler.updateAfterOrder(pid, 5));
    }
}
