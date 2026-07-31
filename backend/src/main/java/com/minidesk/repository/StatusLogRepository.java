package com.minidesk.repository;

import com.minidesk.entity.StatutLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusLogRepository extends JpaRepository<StatutLog, Long> {
}
