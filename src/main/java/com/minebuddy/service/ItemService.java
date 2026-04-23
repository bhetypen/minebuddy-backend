package com.minebuddy.service;

import com.minebuddy.dto.request.ItemRequestDTO;
import com.minebuddy.model.Item;
import com.minebuddy.repository.ItemRepository;
import com.minebuddy.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepo;

    public ItemService(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    @Transactional
    public Item createItem(ItemRequestDTO req) {
        Item item = new Item(
                req.name(),
                req.category(),
                req.price(),
                req.stock(),
                req.saleType()
        );
        item.setLiveName(req.liveName());
        // storeId is set automatically via @PrePersist in the Item entity
        return itemRepo.save(item);
    }

    @Transactional(readOnly = true)
    public Item findById(UUID itemId) {
        UUID storeId = TenantContext.getStoreId();
        return itemRepo.findByItemIdAndStoreId(itemId, storeId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Item> listAll() {
        UUID storeId = TenantContext.getStoreId();
        return itemRepo.findAllByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public List<Item> searchItems(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }
        UUID storeId = TenantContext.getStoreId();
        return itemRepo.search(storeId, searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID itemId) {
        UUID storeId = TenantContext.getStoreId();
        return itemRepo.existsByItemIdAndStoreId(itemId, storeId);
    }
}