package com.todak.api.appointment.dto.request;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCancelRequest {
    private Long appointmentId;
}
