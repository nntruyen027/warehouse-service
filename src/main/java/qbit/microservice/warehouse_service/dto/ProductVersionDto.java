package qbit.microservice.warehouse_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductVersionDto {
    private Long id;
    private ProductDto product;
    private String versionName;
    private BigDecimal price;
    private Integer stockQuantity;
    private String images;
}
