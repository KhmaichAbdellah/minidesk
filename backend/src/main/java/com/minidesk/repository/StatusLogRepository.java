package com.minidesk.repository;

import com.minidesk.entity.StatutLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusLogRepository extends JpaRepository<StatutLog, Long> {

    List<StatutLog> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}
