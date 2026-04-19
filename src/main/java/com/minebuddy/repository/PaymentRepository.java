package com.minebuddy.repository;

import com.minebuddy.model.Payment;
import com.minebuddy.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository  extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
}
