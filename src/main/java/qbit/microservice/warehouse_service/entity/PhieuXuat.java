package qbit.microservice.warehouse_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "phieu_xuat")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PhieuXuat {
    @Id
    private String id;

    private String sdtCuaHang;
    private String diaChiCuaHang;
    private String tenKh;
    private String diaChiKh;
    private String sdtKh;
    private List<Item> items;
    private BigDecimal totalAmount;

    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();
}
