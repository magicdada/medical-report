package com.medical.mapper;

import com.medical.entity.dos.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * 对话消息数据处理层
 * @author wangda
 * @since 2026/09/30
 */
public interface ChatMessageMapper extends JpaRepository<ChatMessage, String> {

    /**
     * 按报告ID和会话类型查询对话历史，按创建时间升序
     *
     * @param reportId 报告ID
     * @param sessionType 会话类型（doctor_chat / patient_chat）
     * @return 对话消息列表
     */
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.reportId = :reportId AND m.sessionType = :sessionType AND m.deleteFlag = false " +
            "ORDER BY m.createTime ASC")
    List<ChatMessage> findHistory(@Param("reportId") String reportId,
                                  @Param("sessionType") String sessionType);

    /**
     * 清空指定报告下某类会话的所有历史（软删除）
     *
     * @param reportId 报告ID
     * @param sessionType 会话类型
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.deleteFlag = true " +
            "WHERE m.reportId = :reportId AND m.sessionType = :sessionType")
    int softDeleteBySession(@Param("reportId") String reportId,
                            @Param("sessionType") String sessionType);
}