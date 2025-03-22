package qbit.microservice.warehouse_service.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class JasperReportService {

    public byte[] generateReport(String reportName, Map<String, Object> parameters, JREmptyDataSource data) throws Exception {

        ClassPathResource jasperResource = new ClassPathResource("reports/" + reportName + ".jasper");

        try (InputStream jasperStream = jasperResource.getInputStream()) {
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, data);

            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }
}
