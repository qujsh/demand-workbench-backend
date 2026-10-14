package com.demandworkbench.requirement.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RequirementMapper {
    @Insert("""
        INSERT INTO requirement
        (owner_user_id, title_code, title, user_description, ai_description, stage, current_decomposition_id)
        VALUES
        (#{ownerUserId}, #{titleCode}, #{title}, #{userDescription}, #{aiDescription}, #{stage},
         (SELECT id FROM decomposition_record WHERE decomposition_no = #{currentDecompositionNo} AND deleted = 0))
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RequirementRecord record);

    @Update("UPDATE requirement SET requirement_no = #{requirementNo} WHERE id = #{id}")
    int updateRequirementNo(RequirementRecord record);

    @Update("""
        UPDATE requirement
        SET title = #{title}, user_description = #{userDescription}, ai_description = #{aiDescription},
            stage = #{stage}, current_decomposition_id =
                (SELECT id FROM decomposition_record WHERE decomposition_no = #{currentDecompositionNo} AND deleted = 0)
        WHERE id = #{id} AND owner_user_id = #{ownerUserId} AND deleted = 0
        """)
    int update(RequirementRecord record);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id AND d.deleted = 0
        WHERE r.id = #{id} AND r.deleted = 0
        """)
    RequirementRecord findById(@Param("id") long id, @Param("ownerUserId") long ownerUserId);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id AND d.deleted = 0
        WHERE r.requirement_no = #{requirementNo} AND r.owner_user_id = #{ownerUserId} AND r.deleted = 0
        """)
    RequirementRecord findByRequirementNo(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id AND d.deleted = 0
        WHERE r.owner_user_id = #{ownerUserId} AND r.deleted = 0
        ORDER BY r.updated_at DESC, r.id DESC
        """)
    List<RequirementRecord> findAll(long ownerUserId);

    @Select("""
        SELECT requirement_no, publish_summary, publish_summary_status,
               publish_summary_prompt_version, publish_summary_generated_at
        FROM requirement
        WHERE requirement_no = #{requirementNo} AND owner_user_id = #{ownerUserId} AND deleted = 0
        """)
    RequirementRecord findPublishSummary(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo);

    @Update("""
        UPDATE requirement
        SET publish_summary_status = #{status}, publish_summary_prompt_version = #{promptVersion}
        WHERE requirement_no = #{requirementNo} AND owner_user_id = #{ownerUserId} AND deleted = 0
        """)
    int updatePublishSummaryStatus(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo,
                                   @Param("status") String status, @Param("promptVersion") String promptVersion);

    @Update("""
        UPDATE requirement
        SET publish_summary = #{content}, publish_summary_status = 'success',
            publish_summary_prompt_version = #{promptVersion},
            publish_summary_generated_at = CURRENT_TIMESTAMP(3)
        WHERE requirement_no = #{requirementNo} AND owner_user_id = #{ownerUserId} AND deleted = 0
        """)
    int savePublishSummary(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo,
                           @Param("content") String content, @Param("promptVersion") String promptVersion);

    @Update("UPDATE requirement SET stage = #{stage} WHERE requirement_no = #{requirementNo} AND owner_user_id = #{ownerUserId} AND deleted = 0")
    int updateStage(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo, @Param("stage") String stage);

    @Update("""
        UPDATE requirement
        SET deleted = 1
        WHERE requirement_no = #{requirementNo} AND owner_user_id = #{ownerUserId} AND deleted = 0
        """)
    int softDelete(@Param("ownerUserId") long ownerUserId, @Param("requirementNo") String requirementNo);
}
