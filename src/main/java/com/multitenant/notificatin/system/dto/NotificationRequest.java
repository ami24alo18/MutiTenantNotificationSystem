package com.multitenant.notificatin.system.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Recipient is mandatory")
    private String recipient;

    @NotBlank(message = "Content is mandatory")
    private String content;

    @NotBlank(message = "Channel is mandatory")
    private String channel;

    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;
}
