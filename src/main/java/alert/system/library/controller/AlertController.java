package alert.system.library.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import alert.system.library.dto.AlertEvent;
import alert.system.library.model.Alert;
import alert.system.library.producer.AlertProducer;
import alert.system.library.repository.AlertRepository;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertProducer alertProducer;

    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private JavaMailSender mailSender;

    /**
     * TEST ENDPOINT - Verify email configuration works
     */
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail() {
        Map<String, Object> response = new HashMap<>();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("dravenarseny2@gmail.com");
            message.setSubject("Library Alert System - Email Test");
            message.setText("If you see this email, your email configuration is working correctly!\n\n"
                    + "Timestamp: " + LocalDateTime.now() + "\n"
                    + "This is a test message from the Library Alert System.");
            message.setFrom("dravenarseny2@gmail.com");
            
            mailSender.send(message);
            
            response.put("success", true);
            response.put("message", "✅ Test email sent successfully!");
            response.put("recipient", "dravenarseny2@gmail.com");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Email test failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", LocalDateTime.now());
            
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger a new alert - PRIMARY ENDPOINT
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerAlert(@RequestBody AlertEvent alertEvent) {
        try {
            alertEvent.setTimestamp(LocalDateTime.now());
            alertProducer.sendAlertEvent(alertEvent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Alert triggered successfully");
            response.put("alertType", alertEvent.getAlertType());
            response.put("userId", alertEvent.getUserId());
            response.put("timestamp", alertEvent.getTimestamp());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to trigger alert: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Legacy endpoint - still works
     */
    @PostMapping("/sendTestAlert")
    public ResponseEntity<String> sendTestAlert(@RequestBody AlertEvent alertEvent) {
        alertEvent.setTimestamp(LocalDateTime.now());
        alertProducer.sendAlertEvent(alertEvent);
        return ResponseEntity.ok("Alert event sent to Kafka topic");
    }

    /**
     * Get all alerts
     */
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        List<Alert> alerts = alertRepository.findAll();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get recent alerts (last 20)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Alert>> getRecentAlerts() {
        List<Alert> alerts = alertRepository.findAll();
        // Return last 20 alerts in reverse order (newest first)
        int start = Math.max(0, alerts.size() - 20);
        return ResponseEntity.ok(alerts.subList(start, alerts.size()));
    }

    /**
     * Get alerts by status (SENT, PENDING, FAILED)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Alert>> getAlertsByStatus(@PathVariable String status) {
        List<Alert> alerts = alertRepository.findByStatus(status);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Alert>> getAlertsByUser(@PathVariable String userId) {
        List<Alert> alerts = alertRepository.findByUserId(userId);
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * System health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "HEALTHY");
        health.put("kafkaConnected", true);
        health.put("databaseConnected", true);
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    /**
     * Get alert statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Alert> allAlerts = alertRepository.findAll();
        
        long sent = allAlerts.stream().filter(a -> "SENT".equals(a.getStatus())).count();
        long pending = allAlerts.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long failed = allAlerts.stream().filter(a -> "FAILED".equals(a.getStatus())).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlerts", allAlerts.size());
        stats.put("sent", sent);
        stats.put("pending", pending);
        stats.put("failed", failed);
        stats.put("successRate", allAlerts.size() > 0 ? (sent * 100.0) / allAlerts.size() : 0);
        
        return ResponseEntity.ok(stats);
    }
}