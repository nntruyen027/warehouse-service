package qbit.microservice.warehouse_service.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleDto {
    private Long id;
    private String roleName;

    public RoleDto(Object[] result) {
        this.id = (Long) result[0];  // Mapping from result array
        this.roleName = (String) result[1];
    }

}
