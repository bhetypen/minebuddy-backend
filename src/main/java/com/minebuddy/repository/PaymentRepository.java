package com.minebuddy.repository;

import com.minebuddy.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByStoreId(UUID storeId);

    List<Payment> findByOrderIdAndStoreId(UUID orderId, UUID storeId);

    Optional<Payment> findByPaymentIdAndStoreId(UUID paymentId, UUID storeId);
}
