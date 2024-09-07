package com.auditflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.auditflow.entity.AuditLog;
import com.auditflow.repository.AuditLogRepository;

@Service
public class AuditService {
    
	@Autowired
    private AuditLogRepository auditLogRepository;

	@KafkaListener(topics = "product-topic", groupId = "audit-group")
    public void listen(String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(message);
        auditLog.setEntity("Product");
        auditLogRepository.save(auditLog);
    }
}
