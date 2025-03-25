package qbit.microservice.warehouse_service.entity;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String donViTinh;
    private BigDecimal itemTotal;
    private Long type;
}
