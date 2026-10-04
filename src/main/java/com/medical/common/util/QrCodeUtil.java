package com.medical.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * QR码生成工具类
 * @author wangda
 * @since 2026/10/01
 */
@Slf4j
public class QrCodeUtil {

    /** 默认图片格式 */
    private static final String DEFAULT_FORMAT = "PNG";

    /** 字符编码 */
    private static final String CHARSET = "UTF-8";

    private QrCodeUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 生成QR码图片的字节数组（PNG格式）
     *
     * @param content QR码内容
     * @param size    图片宽高（像素）
     * @return PNG图片字节数组
     */
    public static byte[] generatePng(String content, int size) {
        return generatePng(content, size, 1);
    }

    /**
     * 生成QR码图片的字节数组（可指定边距）
     *
     * @param content QR码内容
     * @param size    图片宽高（像素）
     * @param margin  边距（0-4）
     * @return PNG图片字节数组
     */
    public static byte[] generatePng(String content, int size, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>(3);
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, margin);

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, DEFAULT_FORMAT, out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("QR码生成失败，content：{}", content, e);
            throw new RuntimeException("QR码生成失败", e);
        }
    }
}