package qbit.microservice.warehouse_service.service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import qbit.microservice.warehouse_service.client.ProductServiceClient;
import qbit.microservice.warehouse_service.dto.ThongKeDto;
import qbit.microservice.warehouse_service.entity.PhieuXuat;
import qbit.microservice.warehouse_service.entity.Item;
import qbit.microservice.warehouse_service.dto.ProductVersionDto;
import qbit.microservice.warehouse_service.entity.Receipt;
import qbit.microservice.warehouse_service.repository.PhieuXuatRepository;
import qbit.microservice.warehouse_service.util.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PhieuXuatService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PhieuXuatRepository phieuXuatRepository;

    @Autowired
    private JasperReportService jasperReportService;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private JwtUtil jwtUtil;

    public Page<PhieuXuat> getAllPhieuXuat(Pageable pageable) {
        return phieuXuatRepository.findAll(pageable);
    }

    public PhieuXuat getPhieuXuatById(String id) {
        return phieuXuatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phieu xuat not found"));
    }

    public PhieuXuat createPhieuXuat(PhieuXuat phieuXuat) {
        // Cập nhật kho: giảm số lượng sản phẩm theo số lượng xuất
        for (Item item : phieuXuat.getItems()) {
            item.setItemTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            ResponseEntity<ProductVersionDto> response = productServiceClient.removeItem(
                    item.getId(),
                    item.getId(),
                    item.getQuantity(),
                    "Bearer " + jwtUtil.getJwtFromContext()
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Không thể cập nhật kho cho sản phẩm: " + item.getVersionName());
            }
        }
        // Tính tổng số tiền của phiếu xuất
        BigDecimal totalAmount = phieuXuat.getItems().stream()
                .map(Item::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        phieuXuat.setTotalAmount(totalAmount);

        // Thiết lập thời gian tạo nếu chưa có
        if(phieuXuat.getThoiGianTao() == null) {
            phieuXuat.setThoiGianTao(LocalDateTime.now());
        }
        return phieuXuatRepository.save(phieuXuat);
    }

    public PhieuXuat updatePhieuXuat(String id, PhieuXuat updatedPhieuXuat) {
        Optional<PhieuXuat> existingOpt = phieuXuatRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Phieu xuat not found");
        }
        PhieuXuat existing = existingOpt.get();

        // Cập nhật các trường cơ bản
        existing.setTenCuaHang(updatedPhieuXuat.getTenCuaHang());
        existing.setDiaChiCuaHang(updatedPhieuXuat.getDiaChiCuaHang());
        existing.setTenKh(updatedPhieuXuat.getTenKh());
        existing.setDiaChiKh(updatedPhieuXuat.getDiaChiKh());
        existing.setSdtKh(updatedPhieuXuat.getSdtKh());
        existing.setThoiGianTao(updatedPhieuXuat.getThoiGianTao());

        // Xử lý cập nhật mặt hàng:
        // So sánh số lượng của từng mặt hàng để xác định thay đổi cần cập nhật kho:
        for (Item newItem : updatedPhieuXuat.getItems()) {
            newItem.setItemTotal(newItem.getPrice().multiply(BigDecimal.valueOf(newItem.getQuantity())));
            Optional<Item> existingItemOpt = existing.getItems().stream()
                    .filter(i -> i.getId().equals(newItem.getId()))
                    .findFirst();
            int quantityDifference = newItem.getQuantity();
            if (existingItemOpt.isPresent()) {
                quantityDifference -= existingItemOpt.get().getQuantity();
            }
            if (quantityDifference > 0) {
                // Xuất thêm: giảm thêm số lượng trong kho
                ResponseEntity<ProductVersionDto> response = productServiceClient.removeItem(
                        newItem.getId(), newItem.getId(), quantityDifference, "Bearer " + jwtUtil.getJwtFromContext()
                );
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Không thể cập nhật kho cho sản phẩm: " + newItem.getVersionName());
                }
            } else if (quantityDifference < 0) {
                // Hủy xuất bớt: trả lại số lượng cho kho
                ResponseEntity<ProductVersionDto> response = productServiceClient.addItem(
                        newItem.getId(), newItem.getId(), -quantityDifference, "Bearer " + jwtUtil.getJwtFromContext()
                );
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Không thể cập nhật kho cho sản phẩm: " + newItem.getVersionName());
                }
            }
        }

        // Cập nhật danh sách mặt hàng và tính lại tổng tiền
        existing.setItems(updatedPhieuXuat.getItems());
        BigDecimal totalAmount = existing.getItems().stream()
                .map(Item::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        existing.setTotalAmount(totalAmount);

        return phieuXuatRepository.save(existing);
    }

    public void deletePhieuXuat(String id) {
        phieuXuatRepository.deleteById(id);
    }

    public byte[] generatePhieuXuatReport(PhieuXuat phieuXuat) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        // Các thông tin của phiếu xuất
        parameters.put("tenCuaHang", phieuXuat.getTenCuaHang());
        parameters.put("diaChiCuaHang", phieuXuat.getDiaChiCuaHang());
        parameters.put("tenKh", phieuXuat.getTenKh());
        parameters.put("diaChiKh", phieuXuat.getDiaChiKh());
        parameters.put("sdtKh", phieuXuat.getSdtKh());
        parameters.put("thoiGianTao", phieuXuat.getThoiGianTao().toString());
        parameters.put("totalAmount", phieuXuat.getTotalAmount().toString());
        // Đưa danh sách items vào parameter để table component sử dụng
        parameters.put("items", phieuXuat.getItems());

        // Sử dụng JREmptyDataSource(1) để main report in đúng 1 lần
        return jasperReportService.generateReport("phieu_xuat_report", parameters, new JREmptyDataSource(1));
    }

    public List<ThongKeDto> getThongKe(List<Long> itemIds, LocalDate startDate, LocalDate endDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("items.id").in(itemIds));

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("thoiGianTao")
                    .gte(startDate.atStartOfDay())
                    .lt(endDate.plusDays(1).atStartOfDay()));
        }

        List<PhieuXuat> phieuXuats = mongoTemplate.find(query, PhieuXuat.class);

        Map<Long, ThongKeDto> thongKeMap = new HashMap<>();

        for (PhieuXuat phieuXuat : phieuXuats) {
            for (Item item : phieuXuat.getItems()) {
                if (itemIds.contains(item.getId())) {
                    thongKeMap.putIfAbsent(item.getId(), new ThongKeDto(item.getId(), 0, BigDecimal.ZERO));

                    ThongKeDto thongKeDto = thongKeMap.get(item.getId());
                    thongKeDto.setSoLuong(thongKeDto.getSoLuong() + item.getQuantity());
                    thongKeDto.setTongTien(thongKeDto.getTongTien().add(item.getItemTotal()) );
                }
            }
        }

        return new ArrayList<>(thongKeMap.values());
    }

    public Map<Integer, Map<Long, ThongKeDto>> getQuarterlyImportStatistics(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        Query query = new Query();
        query.addCriteria(Criteria.where("thoiGianTao").gte(startDate.atStartOfDay()).lt(endDate.plusDays(1).atStartOfDay()));

        List<PhieuXuat> receipts = mongoTemplate.find(query, PhieuXuat.class);
        return processQuarterlyStatistics(receipts);
    }

    public Map<Integer, Map<Long, ThongKeDto>> getMonthlyImportStatistics(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        Query query = new Query();
        query.addCriteria(Criteria.where("thoiGianTao").gte(startDate.atStartOfDay()).lt(endDate.plusDays(1).atStartOfDay()));

        List<PhieuXuat> phieuXuats = mongoTemplate.find(query, PhieuXuat.class);
        return processMonthlyStatistics(phieuXuats);
    }

    private Map<Integer, Map<Long, ThongKeDto>> processQuarterlyStatistics(List<PhieuXuat> phieuXuats) {
        Map<Integer, Map<Long, ThongKeDto>> quarterlyStats = new HashMap<>();

        for (int quarter = 1; quarter <= 4; quarter++) {
            quarterlyStats.put(quarter, new HashMap<>());
        }


        for (PhieuXuat phieuXuat : phieuXuats) {
            LocalDateTime thoiGianTao = phieuXuat.getThoiGianTao();
            int quarter = (getQuarter(thoiGianTao.getMonthValue()));

            for (Item item : phieuXuat.getItems()) {
                Long itemId = item.getId();
                quarterlyStats.putIfAbsent(quarter, new HashMap<>());
                quarterlyStats.get(quarter).putIfAbsent(itemId, new ThongKeDto(itemId,0, BigDecimal.ZERO));

                ThongKeDto stats = quarterlyStats.get(quarter).get(itemId);
                stats.setSoLuong(stats.getSoLuong() + item.getQuantity());
                stats.setTongTien(stats.getTongTien().add(item.getItemTotal()));
            }
        }

        return quarterlyStats;
    }

    private Map<Integer, Map<Long, ThongKeDto>> processMonthlyStatistics(List<PhieuXuat> phieuXuats) {
        Map<Integer, Map<Long, ThongKeDto>> result = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            result.put(month, new HashMap<>());
        }

        for (PhieuXuat phieuXuat : phieuXuats) {
            Integer monthKey = phieuXuat.getThoiGianTao().getMonthValue();

            for (Item item : phieuXuat.getItems()) {
                result.computeIfAbsent(monthKey, k -> new HashMap<>())
                        .compute(item.getId(), (id, stat) -> {
                            if (stat == null) stat = new ThongKeDto(item.getId(), 0, BigDecimal.ZERO);
                            stat.setSoLuong(stat.getSoLuong() + item.getQuantity());
                            stat.setTongTien(stat.getTongTien().add(item.getItemTotal()));
                            return stat;
                        });
            }
        }

        return result;
    }


    private int getQuarter(int month) {
        if (month >= 1 && month <= 3) return 1;
        if (month >= 4 && month <= 6) return 2;
        if (month >= 7 && month <= 9) return 3;
        return 4;
    }

}
