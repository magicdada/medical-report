package com.medical.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全局业务异常类
 *
 * @author wangda
 * @since 2026/08/08
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 3447728300174142127L;

    public static final String DEFAULT_MESSAGE = "网络错误，请稍后重试！";

    private String msg = DEFAULT_MESSAGE;

    private ResultCode resultCode;

    public ServiceException(String msg) {
        this.resultCode = ResultCode.ERROR;
        this.msg = msg;
    }

    public ServiceException() {
        super();
    }

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public ServiceException(ResultCode resultCode, String message) {
        this.resultCode = resultCode;
        this.msg = message;
    }
}