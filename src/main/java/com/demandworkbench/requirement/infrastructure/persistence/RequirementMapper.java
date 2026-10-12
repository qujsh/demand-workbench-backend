package com.demandworkbench.requirement.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RequirementMapper {
    @Insert("""
        INSERT INTO requirement
        (title_code, title, user_description, ai_description, stage, current_decomposition_id)
        VALUES
        (#{titleCode}, #{title}, #{userDescription}, #{aiDescription}, #{stage},
         (SELECT id FROM decomposition_record WHERE decomposition_no = #{currentDecompositionNo}))
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RequirementRecord record);

    @Update("UPDATE requirement SET requirement_no = #{requirementNo} WHERE id = #{id}")
    int updateRequirementNo(RequirementRecord record);

    @Update("""
        UPDATE requirement
        SET title = #{title}, user_description = #{userDescription}, ai_description = #{aiDescription},
            stage = #{stage}, current_decomposition_id =
                (SELECT id FROM decomposition_record WHERE decomposition_no = #{currentDecompositionNo})
        WHERE id = #{id} AND deleted = 0
        """)
    int update(RequirementRecord record);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id
        WHERE r.id = #{id} AND r.deleted = 0
        """)
    RequirementRecord findById(long id);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id
        WHERE r.requirement_no = #{requirementNo} AND r.deleted = 0
        """)
    RequirementRecord findByRequirementNo(String requirementNo);

    @Select("""
        SELECT r.*, d.decomposition_no AS current_decomposition_no,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type = 'module') AS module_count,
               (SELECT COUNT(*) FROM work_item w
                WHERE w.decomposition_id = r.current_decomposition_id AND w.deleted = 0 AND w.type IN ('work', 'subtask')) AS work_item_count
        FROM requirement r
        LEFT JOIN decomposition_record d ON d.id = r.current_decomposition_id
        WHERE r.deleted = 0
        ORDER BY r.updated_at DESC, r.id DESC
        """)
    List<RequirementRecord> findAll();

    @Update("""
        UPDATE requirement
        SET deleted = 1
        WHERE requirement_no = #{requirementNo} AND deleted = 0
        """)
    int softDelete(String requirementNo);
}
