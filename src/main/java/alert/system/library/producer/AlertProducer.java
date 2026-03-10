package alert.system.library.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import alert.system.library.dto.AlertEvent;

@Service
public class AlertProducer {

    @Autowired
    private KafkaTemplate<String, AlertEvent> kafkaTemplate;

    private static final String TOPIC = "library_alerts";
    
    public void sendAlertEvent(AlertEvent alertEvent) {
        kafkaTemplate.send(TOPIC, alertEvent.getUserId(), alertEvent)
        .whenComplete(
            (result, ex) -> {
                if (ex == null) {
                    System.out.println("Alert event sent successfully for userId: " + alertEvent.getUserId());
                } else {
                    System.err.println("Failed to send alert event for userId: " + alertEvent.getUserId() + ", error: " + ex.getMessage());
                }
            }
        );
    }

}
