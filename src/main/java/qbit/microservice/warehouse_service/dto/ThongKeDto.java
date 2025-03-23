package qbit.microservice.warehouse_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ThongKeDto {
    private Long itemId;
    private int soLuong;
    private BigDecimal tongTien;
}
