package com.todak.api.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** 내부 고유 식별자: UUID */
    @Id
    @Column(name = "user_uuid", columnDefinition = "UUID")
    private UUID userUuid;

    /** 카카오 식별자 */
    @Column(nullable = false, unique = true)
    private Long kakaoId;
    private String role;

    /** 사용자 이름 (선택) */
    private String name;

    /** 최초 가입시 UUID 자동 생성 */
    @PrePersist
    public void generateUuid() {
        if (this.userUuid == null) {
            this.userUuid = UUID.randomUUID();
        }
    }
}
