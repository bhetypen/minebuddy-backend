package com.minebuddy.repository;

import com.minebuddy.model.Order;
import com.minebuddy.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByItemIdAndStatus(UUID itemId, OrderStatus status);

    @Query("""
        SELECT o FROM Order o
        WHERE LOWER(CAST(o.orderId AS string)) LIKE LOWER(CONCAT('%', :term, '%'))
           OR EXISTS (
               SELECT 1 FROM Customer c
               WHERE c.customerId = o.customerId
                 AND LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :term, '%'))
           )
           OR EXISTS (
               SELECT 1 FROM Item i
               WHERE i.itemId = o.itemId
                 AND LOWER(i.name) LIKE LOWER(CONCAT('%', :term, '%'))
           )
        """)
    List<Order> search(@Param("term") String term);
}
