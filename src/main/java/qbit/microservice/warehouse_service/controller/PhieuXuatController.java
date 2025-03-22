package qbit.microservice.warehouse_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qbit.microservice.warehouse_service.dto.StatisticsInputDto;
import qbit.microservice.warehouse_service.entity.PhieuXuat;
import qbit.microservice.warehouse_service.service.PhieuXuatService;

@RestController
@RequestMapping("/phieu-xuat")
@PreAuthorize("hasRole('admin')")
public class PhieuXuatController {

    @Autowired
    private PhieuXuatService phieuXuatService;

    @GetMapping("")
    public ResponseEntity<?> findAll(Pageable pageable) {
        return ResponseEntity.ok(phieuXuatService.getAllPhieuXuat(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        return ResponseEntity.ok(phieuXuatService.getPhieuXuatById(id));
    }

    @PostMapping("")
    public ResponseEntity<?> createOne(@RequestBody PhieuXuat phieuXuat) {
        return ResponseEntity.ok(phieuXuatService.createPhieuXuat(phieuXuat));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOne(@PathVariable String id, @RequestBody PhieuXuat phieuXuat) {
        return ResponseEntity.ok(phieuXuatService.updatePhieuXuat(id, phieuXuat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOne(@PathVariable String id) {
        phieuXuatService.deletePhieuXuat(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<byte[]> generateReport(@PathVariable String id) throws Exception {
        PhieuXuat phieuXuat = phieuXuatService.getPhieuXuatById(id);
        byte[] pdfBytes = phieuXuatService.generatePhieuXuatReport(phieuXuat);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=phieu_xuat.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> doStatistics(@RequestBody StatisticsInputDto input) throws Exception {
        return ResponseEntity.ok(phieuXuatService.getThongKe(input.getIds(), input.getStartDate(), input.getEndDate()));
    }


}
