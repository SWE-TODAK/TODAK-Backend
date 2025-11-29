package com.todak.api.hospital.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteToggleRequestDto {
    private Long hospitalId;
    private boolean favorite;   // true = 찜 등록, false = 찜 해제
}
