package qbit.microservice.warehouse_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "warehouse_receipts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Receipt {
    @Id
    private String id;
    private String nguoiGiao; // Họ tên người giao
    private LocalDate ngayGiao; // Ngày giao
    private String bbSoHieu; //Số hiệu biên bản
    private LocalDate bbNgay; //Số hiệu biên bản
    private String nhaCungCap; // Nhà cung cấp
    private String khoTen; // Tên kho nhận
    private String khoDiaChi; // Địa chỉ kho
    private List<Item> items;
    private BigDecimal totalAmount;
}
