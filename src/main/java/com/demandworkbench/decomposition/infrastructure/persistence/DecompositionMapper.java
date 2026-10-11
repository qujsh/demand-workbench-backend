package com.demandworkbench.decomposition.infrastructure.persistence;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DecompositionMapper {
    @Insert("""
        INSERT INTO decomposition_record
        (id, source_content, status, stage, model, prompt_version, created_at)
        VALUES (#{id}, #{sourceContent}, #{status}, #{stage}, #{model}, #{promptVersion}, #{createdAt})
        """)
    void insert(DecompositionRecord record);

    @Select("SELECT * FROM decomposition_record WHERE id = #{id}")
    DecompositionRecord findById(String id);

    @Update("""
        UPDATE decomposition_record
        SET status = #{status}, stage = #{stage}, started_at = COALESCE(started_at, CURRENT_TIMESTAMP(3))
        WHERE id = #{id}
        """)
    void updateStage(@Param("id") String id, @Param("status") String status, @Param("stage") String stage);

    @Update("""
        UPDATE decomposition_record
        SET status = 'SUCCESS', stage = 'COMPLETED', raw_response = #{rawResponse},
            prompt_tokens = #{promptTokens}, completion_tokens = #{completionTokens}, completed_at = CURRENT_TIMESTAMP(3)
        WHERE id = #{id}
        """)
    void markSuccess(@Param("id") String id, @Param("rawResponse") String rawResponse,
                     @Param("promptTokens") Integer promptTokens, @Param("completionTokens") Integer completionTokens);

    @Update("""
        UPDATE decomposition_record
        SET status = 'FAILED', stage = 'FAILED', error_code = #{errorCode}, error_message = #{errorMessage},
            completed_at = CURRENT_TIMESTAMP(3)
        WHERE id = #{id}
        """)
    void markFailed(@Param("id") String id, @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage);

    @Update("""
        UPDATE decomposition_record
        SET status = 'FAILED', stage = 'FAILED', error_code = 'SERVER_RESTARTED',
            error_message = '服务重启，任务未完成，请重新提交', completed_at = CURRENT_TIMESTAMP(3)
        WHERE status IN ('QUEUED', 'PROCESSING')
        """)
    int failInterruptedTasks();
}
