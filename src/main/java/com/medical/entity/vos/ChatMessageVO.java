package com.medical.entity.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 对话消息VO
 * @author wangda
 * @since 2026/09/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {

    /** 消息ID */
    private String id;

    /** 角色：user/assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}