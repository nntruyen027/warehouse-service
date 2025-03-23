package qbit.microservice.warehouse_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
