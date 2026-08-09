package com.medical.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 前后端交互VO
 * @author wangda
 * @since 2026/08/08
 */
@Data
public class ResultMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private String message;

    private Integer code;

    private long timestamp = System.currentTimeMillis();

    private T result;
}
