package com.medical.service.impl;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.entity.dos.Report;
import com.medical.mapper.ReportMapper;
import com.medical.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 诊断报告业务层实现
 *
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final WebClient webClient = WebClient.create();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report generateReport(String doctorId, String patientId, MultipartFile imageFile) {
        try {
            // 1. 保存上传的影像文件
            String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath.toFile());
            log.info("影像文件保存成功：{}", filePath);

            // 2. 调用Python AI推理服务
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            }).contentType(MediaType.IMAGE_JPEG);

            Map result = webClient.post()
                    .uri(aiServiceUrl + "/predict")
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("AI推理完成，结果：{}", result);

            // 3. 组装报告
            Report report = new Report();
            report.setDoctorId(doctorId);
            report.setPatientId(patientId);
            report.setImagePath(filePath.toString());
            report.setReportContent(result != null ? (String) result.get("report") : "");
            report.setHeatmapPath(result != null ? (String) result.get("heatmap_path") : "");
            report.setStatus("DRAFT");
            report.setCreateBy(doctorId);
            reportMapper.save(report);
            log.info("诊断报告生成成功，ID：{}", report.getId());
            return report;

        } catch (IOException e) {
            log.error("文件处理失败", e);
            throw new ServiceException(ResultCode.REPORT_GENERATE_ERROR);
        }
    }

    @Override
    public Report getById(String id) {
        return reportMapper.findById(id).orElse(null);
    }

    @Override
    public List<Report> getByPatientId(String patientId) {
        return reportMapper.findByPatientIdOrderByCreateTimeDesc(patientId);
    }

    @Override
    public List<Report> getByDoctorId(String doctorId) {
        return reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId);
    }

    @Override
    public Report updateStatus(String id, String status) {
        Report report = reportMapper.findById(id).orElse(null);
        if (report == null) {
            throw new ServiceException(ResultCode.REPORT_NOT_EXIST);
        }
        report.setStatus(status);
        reportMapper.save(report);
        log.info("报告状态更新：{} -> {}", id, status);
        return report;
    }
}