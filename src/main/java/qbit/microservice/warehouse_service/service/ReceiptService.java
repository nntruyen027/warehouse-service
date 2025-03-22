package qbit.microservice.warehouse_service.service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import qbit.microservice.warehouse_service.client.ProductServiceClient;
import qbit.microservice.warehouse_service.dto.ThongKeDto;
import qbit.microservice.warehouse_service.entity.Receipt;
import qbit.microservice.warehouse_service.entity.Item;
import qbit.microservice.warehouse_service.repository.ReceiptRepository;
import qbit.microservice.warehouse_service.dto.ProductVersionDto;
import qbit.microservice.warehouse_service.util.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReceiptService {
    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private JasperReportService jasperReportService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<Receipt> getAllReceipts(Pageable pageable) {
        return receiptRepository.findAll(pageable);
    }

    public Receipt getReceiptById(String id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public Receipt createReceipt(Receipt receipt) {
        // Tính toán itemTotal cho từng mặt hàng và cập nhật kho
        for (Item item : receipt.getItems()) {
            item.setItemTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            ResponseEntity<ProductVersionDto> response = productServiceClient.addItem(
                    item.getId(),
                    item.getId(),
                    item.getQuantity(),
                    "Bearer " + jwtUtil.getJwtFromContext()
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Không thể cập nhật kho cho sản phẩm: " + item.getVersionName());
            }
        }
        // Tính tổng số tiền của phiếu nhập kho
        BigDecimal totalAmount = receipt.getItems().stream()
                .map(Item::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        receipt.setTotalAmount(totalAmount);

        return receiptRepository.save(receipt);
    }

    public Receipt updateReceipt(String id, Receipt updatedReceipt) {
        Optional<Receipt> existingReceiptOpt = receiptRepository.findById(id);
        if (existingReceiptOpt.isEmpty()) {
            throw new RuntimeException("Phiếu nhập kho không tồn tại!");
        }

        Receipt existingReceipt = existingReceiptOpt.get();

        // Cập nhật thông tin từng mặt hàng
        for (Item newItem : updatedReceipt.getItems()) {
            newItem.setItemTotal(newItem.getPrice().multiply(BigDecimal.valueOf(newItem.getQuantity())));
            Optional<Item> existingItemOpt = existingReceipt.getItems().stream()
                    .filter(i -> i.getId().equals(newItem.getId()))
                    .findFirst();

            int quantityDifference = newItem.getQuantity();
            if (existingItemOpt.isPresent()) {
                quantityDifference -= existingItemOpt.get().getQuantity();
            }

            if (quantityDifference > 0) {
                productServiceClient.addItem(newItem.getId(), newItem.getId(), quantityDifference, "Bearer " + jwtUtil.getJwtFromContext());
            } else if (quantityDifference < 0) {
                productServiceClient.removeItem(newItem.getId(), newItem.getId(), -quantityDifference, "Bearer " + jwtUtil.getJwtFromContext());
            }
        }

        BigDecimal totalAmount = updatedReceipt.getItems().stream()
                .map(Item::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updatedReceipt.setTotalAmount(totalAmount);

        updatedReceipt.setId(id);
        return receiptRepository.save(updatedReceipt);
    }

    public void deleteReceipt(String id) {
        receiptRepository.deleteById(id);
    }

    public byte[] generateReceiptReport(Receipt receipt) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("nguoiGiao", receipt.getNguoiGiao());
        parameters.put("ngayGiao", receipt.getNgayGiao().toString());
        parameters.put("bbSoHieu", receipt.getBbSoHieu());
        parameters.put("bbNgay", receipt.getBbNgay().toString());
        parameters.put("nhaCungCap", receipt.getNhaCungCap());
        parameters.put("khoTen", receipt.getKhoTen());
        parameters.put("khoDiaChi", receipt.getKhoDiaChi());
        parameters.put("totalAmount", receipt.getTotalAmount().toString());
        parameters.put("items", receipt.getItems());

        return jasperReportService.generateReport("receipt_report", parameters, new JREmptyDataSource(1));
    }

    public List<ThongKeDto> getThongKeNhap(List<Long> itemIds, LocalDate startDate, LocalDate endDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("items.id").in(itemIds));

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("ngayGiao")
                    .gte(startDate.atStartOfDay())
                    .lt(endDate.plusDays(1).atStartOfDay()));
        }

        List<Receipt> receipts = mongoTemplate.find(query, Receipt.class);

        Map<Long, ThongKeDto> thongKeMap = new HashMap<>();

        for (Receipt receipt : receipts) {
            for (Item item : receipt.getItems()) {
                if (itemIds.contains(item.getId())) {
                    thongKeMap.putIfAbsent(item.getId(), new ThongKeDto(item.getId(), 0, BigDecimal.ZERO));

                    ThongKeDto thongKeDto = thongKeMap.get(item.getId());
                    thongKeDto.setSoLuong(thongKeDto.getSoLuong() + item.getQuantity());
                    thongKeDto.setTongTien(thongKeDto.getTongTien().add(item.getItemTotal()));
                }
            }
        }

        return new ArrayList<>(thongKeMap.values());
    }

}
