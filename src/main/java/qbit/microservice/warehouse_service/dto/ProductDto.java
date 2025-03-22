package qbit.microservice.warehouse_service.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private List<ProductVersionDto> versions;
    private String image;
    private Boolean isOpened;
}
