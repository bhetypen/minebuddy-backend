package com.minebuddy.service;

import com.minebuddy.dto.request.ItemRequestDTO;
import com.minebuddy.model.Item;
import com.minebuddy.repository.ItemRepository;
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
        return itemRepo.save(item);
    }

    @Transactional(readOnly = true)
    public Item findById(UUID itemId) {
        return itemRepo.findById(itemId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Item> listAll() {
        return itemRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Item> searchItems(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }
        return itemRepo.search(searchTerm.trim());
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID itemId) {
        return itemRepo.existsById(itemId);
    }
}