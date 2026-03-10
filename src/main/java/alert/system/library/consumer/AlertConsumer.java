package alert.system.library.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import alert.system.library.dto.AlertEvent;
import alert.system.library.service.AlertService;

@Service
public class AlertConsumer {

    @Autowired
    private AlertService alertService;

    @KafkaListener(topics = "library_alerts", groupId = "library_group")
    public void consumeAlertEvent(AlertEvent alertEvent) {
        alertService.processAlertEvent(alertEvent);
    }

}
