package com.medical.common;

/**
 * 返回状态码
 *
 * @author wangda
 * @since 2026/08/08
 */
public enum ResultCode {

    SUCCESS(200, "成功"),

    ERROR(400, "服务器繁忙，请稍后重试"),

    PARAMS_ERROR(4002, "参数异常"),

    USER_NOT_EXIST(20002, "用户不存在"),
    USER_NOT_LOGIN(20003, "用户未登录"),
    USER_AUTH_EXPIRED(20004, "用户已退出，请重新登录"),
    USER_AUTHORITY_ERROR(20005, "权限不足"),
    USER_EXIST(20008, "该用户名已被注册"),
    USER_PASSWORD_ERROR(20010, "密码不正确"),
    USER_STATUS_ERROR(20028, "用户已禁用"),

    PATIENT_NOT_EXIST(30001, "患者不存在"),
    PATIENT_NO_EXIST(30002, "该患者编号已存在"),

    REPORT_NOT_EXIST(40001, "报告不存在"),
    REPORT_GENERATE_ERROR(40002, "报告生成失败"),
    REPORT_STATUS_ERROR(40003, "报告状态异常"),
    REPORT_NOT_OWN(40004, "无权操作他人的报告"),

    FILE_NOT_EXIST_ERROR(50001, "上传文件不能为空"),
    FILE_TYPE_NOT_SUPPORT(50002, "不支持的文件类型"),
    FILE_SIZE_EXCEED(50003, "文件大小超出限制"),
    FILE_EXTENSION_NOT_ALLOWED(50004, "不允许的文件扩展名"),
    FILE_UPLOAD_ERROR(50005, "文件上传失败"),
    FILE_LIST_EMPTY(50006, "请至少上传一张影像文件"),
    FILE_COUNT_EXCEED(50007, "最多支持上传2张影像文件（正位+侧位）"),

    AI_SERVICE_ERROR(60001, "AI推理服务异常，请稍后重试"),
    AI_SERVICE_TIMEOUT(60002, "AI推理服务超时");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}