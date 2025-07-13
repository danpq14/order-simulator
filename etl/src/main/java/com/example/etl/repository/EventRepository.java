package com.example.etl.repository;

import com.example.etl.model.entity.Event;
import com.example.common.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    Page<Event> findByEventTypeOrderByCreatedAtDesc(EventType eventType, Pageable pageable);

    Page<Event> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
