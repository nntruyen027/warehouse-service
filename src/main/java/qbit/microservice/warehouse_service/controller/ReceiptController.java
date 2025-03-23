package qbit.microservice.warehouse_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qbit.microservice.warehouse_service.dto.StatisticsInputDto;
import qbit.microservice.warehouse_service.entity.Receipt;
import qbit.microservice.warehouse_service.service.ReceiptService;


@RestController
@RequestMapping("/receipts")
@PreAuthorize("hasRole('admin')")
public class ReceiptController {
    @Autowired
    private ReceiptService receiptService;

    @GetMapping("")
    public ResponseEntity<?> findAll(Pageable pageable) {
        return ResponseEntity.ok(receiptService.getAllReceipts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        return ResponseEntity.ok(receiptService.getReceiptById(id));
    }

    @PostMapping("")
    public ResponseEntity<?> createOne(@RequestBody Receipt receipt) {
        return ResponseEntity.ok(receiptService.createReceipt(receipt));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOne(@PathVariable String id, @RequestBody Receipt receipt) {
        return ResponseEntity.ok(receiptService.updateReceipt(id, receipt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOne(@PathVariable String id) {
        receiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<byte[]> generateReport(@PathVariable String id) throws Exception {
        byte[] pdfBytes = receiptService.generateReceiptReport(receiptService.getReceiptById(id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);

    }

    @GetMapping("/statistics")
    public ResponseEntity<?> doStatistics(@RequestBody StatisticsInputDto input) throws Exception {
        return ResponseEntity.ok(receiptService.getThongKeNhap(input.getIds(), input.getStartDate(), input.getEndDate()));
    }

    @GetMapping("/statistics/year/{nam}/quarters")
    public ResponseEntity<?> doQuarterlyStatistics(@PathVariable int nam) throws Exception {
        return ResponseEntity.ok(receiptService.getQuarterlyImportStatistics(nam));
    }

    @GetMapping("/statistics/year/{nam}/months")
    public ResponseEntity<?> doMonthlyStatistics(@PathVariable int nam) throws Exception {
        return ResponseEntity.ok(receiptService.getMonthlyImportStatistics(nam));
    }
}
