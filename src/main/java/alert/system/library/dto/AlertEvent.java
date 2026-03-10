package alert.system.library.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    private String userId;
    private String bookId;
    private String alertType; // BOOK_AVAILABLE, BOOK_DUE, etc.
    private String message;
    private LocalDateTime timestamp;
}
