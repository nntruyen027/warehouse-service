package qbit.microservice.warehouse_service.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String username;
    private String email;
    private String googleId;
    private String facebookId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<RoleDto> roles;

}
