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
        (decomposition_no, source_content, status, stage, model, prompt_version, created_at)
        VALUES (#{decompositionNo}, #{sourceContent}, #{status}, #{stage}, #{model}, #{promptVersion}, #{createdAt})
        """)
    void insert(DecompositionRecord record);

    @Select("SELECT * FROM decomposition_record WHERE decomposition_no = #{decompositionNo} AND deleted = 0")
    DecompositionRecord findByDecompositionNo(String decompositionNo);

    @Update("""
        UPDATE decomposition_record
        SET status = #{status}, stage = #{stage}, started_at = COALESCE(started_at, CURRENT_TIMESTAMP(3))
        WHERE decomposition_no = #{decompositionNo} AND deleted = 0
        """)
    void updateStage(@Param("decompositionNo") String decompositionNo,
                     @Param("status") String status, @Param("stage") String stage);

    @Update("""
        UPDATE decomposition_record
        SET status = #{status}, stage = #{stage}, raw_response = #{rawResponse},
            prompt_tokens = #{promptTokens}, completion_tokens = #{completionTokens}, completed_at = CURRENT_TIMESTAMP(3)
        WHERE decomposition_no = #{decompositionNo} AND deleted = 0
        """)
    void markSuccess(@Param("decompositionNo") String decompositionNo,
                     @Param("status") String status, @Param("stage") String stage,
                     @Param("rawResponse") String rawResponse,
                     @Param("promptTokens") Integer promptTokens, @Param("completionTokens") Integer completionTokens);

    @Update("""
        UPDATE decomposition_record
        SET status = #{status}, stage = #{stage}, error_code = #{errorCode}, error_message = #{errorMessage},
            completed_at = CURRENT_TIMESTAMP(3)
        WHERE decomposition_no = #{decompositionNo} AND deleted = 0
        """)
    void markFailed(@Param("decompositionNo") String decompositionNo,
                    @Param("status") String status, @Param("stage") String stage,
                    @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage);

    @Update("""
        UPDATE decomposition_record
        SET status = #{failedStatus}, stage = #{failedStage}, error_code = #{errorCode},
            error_message = #{errorMessage}, completed_at = CURRENT_TIMESTAMP(3)
        WHERE deleted = 0 AND status IN (#{queuedStatus}, #{processingStatus})
        """)
    int failInterruptedTasks(@Param("failedStatus") String failedStatus, @Param("failedStage") String failedStage,
                             @Param("errorCode") String errorCode, @Param("errorMessage") String errorMessage,
                             @Param("queuedStatus") String queuedStatus,
                             @Param("processingStatus") String processingStatus);
}
