package com.minebuddy.controller;

import com.minebuddy.dto.request.ItemRequestDTO;
import com.minebuddy.dto.response.ItemResponseDTO;
import com.minebuddy.model.Item;
import com.minebuddy.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemResponseDTO> getAll() {
        return service.listAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @GetMapping("/search")
    public List<ItemResponseDTO> search(@RequestParam("q") String query) {
        return service.searchItems(query).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> findById(@PathVariable UUID id) {
        Item item = service.findById(id);
        return (item != null)
                ? ResponseEntity.ok(toResponseDTO(item))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDTO create(@Valid @RequestBody ItemRequestDTO dto) {
        Item item = service.createItem(dto);
        return toResponseDTO(item);
    }

    private ItemResponseDTO toResponseDTO(Item item) {
        return new ItemResponseDTO(
                item.getItemId().toString(),
                item.getName(),
                item.getLiveName(),
                item.getCategory(),
                item.getSaleType(),
                item.getPrice(),
                item.getCost(),
                item.getStock(),
                item.isActive(),
                item.getCreatedAt()
        );
    }
}