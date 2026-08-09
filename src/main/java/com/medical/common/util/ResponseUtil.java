package com.medical.common.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Response输出工具类
 *
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
public class ResponseUtil {

    static final String ENCODING = "UTF-8";
    static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    /**
     * 输出JSON
     *
     * @param response  响应
     * @param resultMap 结果
     */
    public static void output(HttpServletResponse response, Map<String, Object> resultMap) {
        ServletOutputStream servletOutputStream = null;
        try {
            response.setCharacterEncoding(ENCODING);
            response.setContentType(CONTENT_TYPE);
            servletOutputStream = response.getOutputStream();
            servletOutputStream.write(new Gson().toJson(resultMap).getBytes());
        } catch (Exception e) {
            log.error("response output error:", e);
        } finally {
            if (servletOutputStream != null) {
                try {
                    servletOutputStream.flush();
                    servletOutputStream.close();
                } catch (IOException e) {
                    log.error("response output IO close error:", e);
                }
            }
        }
    }

    /**
     * 输出JSON（带状态码）
     *
     * @param response  响应
     * @param status    状态码
     * @param resultMap 结果
     */
    public static void output(HttpServletResponse response, Integer status, Map<String, Object> resultMap) {
        response.setStatus(status);
        output(response, resultMap);
    }

    /**
     * 构造响应Map
     *
     * @param flag 是否成功
     * @param code 状态码
     * @param msg  消息
     * @return 响应Map
     */
    public static Map<String, Object> resultMap(boolean flag, Integer code, String msg) {
        Map<String, Object> resultMap = new HashMap<>(16);
        resultMap.put("success", flag);
        resultMap.put("message", msg);
        resultMap.put("code", code);
        resultMap.put("timestamp", System.currentTimeMillis());
        return resultMap;
    }
}