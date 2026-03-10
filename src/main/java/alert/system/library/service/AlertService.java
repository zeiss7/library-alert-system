package alert.system.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import alert.system.library.dto.AlertEvent;
import alert.system.library.model.Alert;
import alert.system.library.model.User;
import alert.system.library.repository.AlertRepository;
import alert.system.library.repository.UserRepository;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public void processAlertEvent(AlertEvent alertEvent) {
        try{
            User user = userRepository.findByUserId(alertEvent.getUserId());

            if (user != null && user.getEmailNotifications()) {
                emailService.sendEmail(user.getEmail(), 
                    "Library Alert" + alertEvent.getAlertType(), alertEvent.getMessage());
                    
                    saveAlert(alertEvent, "SENT");
                }    
            } catch (Exception e) {
                saveAlert(alertEvent, "FAILED");
                e.printStackTrace();
            }
        }

    private void saveAlert(AlertEvent alertEvent, String status) {
        Alert alert = new Alert();
        alert.setUserId(alertEvent.getUserId());
        alert.setBookId(alertEvent.getBookId());
        alert.setAlertType(alertEvent.getAlertType());
        alert.setMessage(alertEvent.getMessage());
        alert.setStatus(status);
        alert.setCreatedAt(java.time.LocalDateTime.now());
        if ("SENT".equals(status)) {
            alert.setSentAt(java.time.LocalDateTime.now());
            
        }
        alertRepository.save(alert);
    }


}