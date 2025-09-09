package com.loopers.collector.repository;

import com.loopers.collector.entity.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledRepository extends JpaRepository<EventHandled, String> {
}
