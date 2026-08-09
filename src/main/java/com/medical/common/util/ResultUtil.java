package com.medical.common.util;

import com.medical.common.ResultMessage;

/**
 * 返回结果工具类
 * @author wangda
 * @since 2026/08/08
 */
public class ResultUtil<T> {

    private final ResultMessage<T> resultMessage;

    private static final Integer SUCCESS = 200;

    public ResultUtil() {
        resultMessage = new ResultMessage<>();
        resultMessage.setSuccess(true);
        resultMessage.setMessage("success");
        resultMessage.setCode(SUCCESS);
    }

    public ResultMessage<T> setData(T t) {
        this.resultMessage.setResult(t);
        return this.resultMessage;
    }

    public static <T> ResultMessage<T> data(T t) {
        return new ResultUtil<T>().setData(t);
    }

    public static <T> ResultMessage<T> success() {
        return new ResultUtil<T>().setData(null);
    }

    public static <T> ResultMessage<T> error(Integer code, String msg) {
        ResultUtil<T> util = new ResultUtil<>();
        util.resultMessage.setSuccess(false);
        util.resultMessage.setMessage(msg);
        util.resultMessage.setCode(code);
        return util.resultMessage;
    }

    public static <T> ResultMessage<T> error(String msg) {
        return error(500, msg);
    }
}
