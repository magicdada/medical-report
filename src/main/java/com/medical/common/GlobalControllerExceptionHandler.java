package com.medical.common;

import com.medical.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * @author wangda
 * @since 2026/08/08
 */
@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    static Integer MAX_LENGTH = 200;

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResultMessage<Object> handleServiceException(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        if (e instanceof ServiceException) {
            ServiceException serviceException = ((ServiceException) e);
            ResultCode resultCode = serviceException.getResultCode();
            Integer code = null;
            String message = null;
            if (resultCode != null) {
                code = resultCode.code();
                message = resultCode.message();
            }
            if (!serviceException.getMsg().equals(ServiceException.DEFAULT_MESSAGE)) {
                message += ":" + serviceException.getMsg();
            }
            log.error("全局异常[ServiceException]:{}-{}", code, message, e);
            return ResultUtil.error(code, message);
        }
        String errorMsg = "服务器异常，请稍后重试";
        if (e != null && e.getMessage() != null && e.getMessage().length() < MAX_LENGTH) {
            errorMsg = e.getMessage();
        }
        return ResultUtil.error(ResultCode.ERROR.code(), errorMsg);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResultMessage<Object> runtimeExceptionHandler(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        log.error("全局异常[RuntimeException]:", e);
        return ResultUtil.error(ResultCode.ERROR.code(), ResultCode.ERROR.message());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResultMessage<Object> validExceptionHandler(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        BindException exception = (BindException) e;
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        try {
            if (!fieldErrors.isEmpty()) {
                return ResultUtil.error(ResultCode.PARAMS_ERROR.code(),
                        fieldErrors.stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", ")));
            }
            return ResultUtil.error(ResultCode.PARAMS_ERROR.code(), ResultCode.PARAMS_ERROR.message());
        } catch (Exception ex) {
            return ResultUtil.error(ResultCode.PARAMS_ERROR.code(), ResultCode.PARAMS_ERROR.message());
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResultMessage<Object> constraintViolationExceptionHandler(HttpServletRequest request, final Exception e, HttpServletResponse response) {
        ConstraintViolationException exception = (ConstraintViolationException) e;
        return ResultUtil.error(ResultCode.PARAMS_ERROR.code(), exception.getMessage());
    }
}