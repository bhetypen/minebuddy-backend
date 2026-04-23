package com.minebuddy.repository;

import com.minebuddy.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    List<Item> findAllByStoreId(UUID storeId);

    @Query("""
        SELECT i FROM Item i
        WHERE i.storeId = :storeId
          AND (LOWER(CAST(i.itemId AS string)) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(i.name)     LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(i.liveName) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(i.category) LIKE LOWER(CONCAT('%', :term, '%'))
           OR CAST(i.createdAt AS string) LIKE CONCAT('%', :term, '%'))
        """)
    List<Item> search(@Param("storeId") UUID storeId, @Param("term") String term);

    java.util.Optional<Item> findByItemIdAndStoreId(UUID itemId, UUID storeId);
    
    boolean existsByItemIdAndStoreId(UUID itemId, UUID storeId);
}