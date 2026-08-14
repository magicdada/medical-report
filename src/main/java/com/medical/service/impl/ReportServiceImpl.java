package com.medical.service.impl;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.enums.ReportStatusEnum;
import com.medical.entity.dos.Report;
import com.medical.entity.vos.ReportVO;
import com.medical.mapper.ReportMapper;
import com.medical.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * 允许上传的图片类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".dcm"};
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "application/dicom"};
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    /**
     * 允许上传的影像数量限制
     */
    private static final int MAX_IMAGE_COUNT = 2;

    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report generateReport(String doctorId, String patientId, List<MultipartFile> imageFiles) {
        // 1. 文件数量校验
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new ServiceException(ResultCode.FILE_LIST_EMPTY);
        }
        if (imageFiles.size() > MAX_IMAGE_COUNT) {
            throw new ServiceException(ResultCode.FILE_COUNT_EXCEED);
        }

        // 2. 文件格式校验
        List<Path> savedPaths = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            validateImageFile(imageFile);
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile imageFile : imageFiles) {
                String fileName = UUID.randomUUID().toString().replace("-", "")
                        + "_" + imageFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
//                imageFile.transferTo(filePath.toFile());
                imageFile.transferTo(filePath.toAbsolutePath().toFile());
                savedPaths.add(filePath);
                log.info("影像文件保存成功：{}", filePath);
            }

            // 3. 调用Python AI推理服务（使用FileSystemResource避免大文件进内存）
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            for (Path savedPath : savedPaths) {
                builder.part("files", new FileSystemResource(savedPath.toFile()))
                        .contentType(MediaType.IMAGE_JPEG);
            }

            Map result = webClient.post()
                    .uri(aiServiceUrl + "/predict")
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            log.info("AI推理完成，结果：{}", result);

            // 4. 组装报告（多个路径用逗号分隔存储）
            String imagePaths = savedPaths.stream()
                    .map(Path::toString)
                    .collect(Collectors.joining(","));

            String reportContent = result != null ? (String) result.get("report") : "";
            String heatmapPath = result != null ? (String) result.get("heatmap_path") : "";

            Report report = new Report();
            report.setDoctorId(doctorId);
            report.setPatientId(patientId);
            report.setImagePath(imagePaths);
            report.setReportContent(reportContent);
            report.setAiDraft(reportContent);
            report.setHeatmapPath(heatmapPath);
            report.setStatus(ReportStatusEnum.DRAFT.name());
            report.setCreateBy(doctorId);
            reportMapper.save(report);
            log.info("诊断报告生成成功，ID：{}", report.getId());

            return report;

        } catch (IOException e) {
            log.error("文件处理失败", e);
            throw new ServiceException(ResultCode.REPORT_GENERATE_ERROR);
        } catch (Exception e) {
            log.error("AI推理服务异常", e);
            throw new ServiceException(ResultCode.AI_SERVICE_ERROR);
        }
    }

    /**
     * 校验上传的影像文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException(ResultCode.FILE_NOT_EXIST_ERROR);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException(ResultCode.FILE_SIZE_EXCEED);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedExtension(originalFilename)) {
            throw new ServiceException(ResultCode.FILE_EXTENSION_NOT_ALLOWED);
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new ServiceException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }
    }

    /**
     * 判断文件扩展名是否允许
     */
    private boolean isAllowedExtension(String filename) {
        String lowerName = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断MIME类型是否允许
     */
    private boolean isAllowedContentType(String contentType) {
        for (String type : ALLOWED_CONTENT_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
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
    public List<ReportVO> getByDoctorId(String doctorId) {
        return reportMapper.findVOByDoctorId(doctorId);
    }

    @Override
    public Report updateStatus(String id, String status, String doctorId) {
        Report report = reportMapper.findById(id).orElse(null);
        if (report == null) {
            throw new ServiceException(ResultCode.REPORT_NOT_EXIST);
        }
        if (!report.getDoctorId().equals(doctorId)) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
        report.setStatus(status);
        reportMapper.save(report);
        log.info("报告状态更新：{} -> {}", id, status);
        return report;
    }
}