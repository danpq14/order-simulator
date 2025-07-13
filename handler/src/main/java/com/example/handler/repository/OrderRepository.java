package com.example.handler.repository;

import com.example.handler.model.entity.Order;
import com.example.common.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findBySymbol(String symbol, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY RAND() LIMIT :limit")
    List<Order> findRandomOrdersByStatus(@Param("status") OrderStatus status, @Param("limit") int limit);
}
