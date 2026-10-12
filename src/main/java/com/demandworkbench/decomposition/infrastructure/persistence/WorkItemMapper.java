package com.demandworkbench.decomposition.infrastructure.persistence;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkItemMapper {
    @Insert("""
        INSERT INTO work_item
        (work_item_no, decomposition_id, parent_id, type, name, description, status, role_name, priority,
         start_date, end_date, dependency_text, sort_order, created_at, updated_at)
        VALUES
        (#{workItemNo}, #{decompositionId}, #{parentId}, #{type}, #{name}, #{description}, #{status}, #{roleName}, #{priority},
         #{startDate}, #{endDate}, #{dependencyText}, #{sortOrder}, #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkItemRecord record);

    @Select("SELECT * FROM work_item WHERE decomposition_id = #{decompositionId} AND deleted = 0 ORDER BY sort_order ASC")
    List<WorkItemRecord> findByDecompositionId(long decompositionId);

    @Delete("DELETE FROM work_item WHERE decomposition_id = #{decompositionId}")
    void deleteByDecompositionId(long decompositionId);

    @Select("""
        <script>
        WITH RECURSIVE descendants AS (
            SELECT id
            FROM work_item
            WHERE deleted = 0 AND work_item_no IN
            <foreach collection="workItemNos" item="workItemNo" open="(" separator="," close=")">
                #{workItemNo}
            </foreach>
            UNION ALL
            SELECT child.id
            FROM work_item child
            JOIN descendants parent ON child.parent_id = parent.id
            WHERE child.deleted = 0
        )
        SELECT DISTINCT id FROM descendants
        </script>
        """)
    List<Long> findIdsIncludingDescendants(@Param("workItemNos") List<String> workItemNos);

    @Update("""
        <script>
        UPDATE work_item
        SET deleted = 1, updated_at = CURRENT_TIMESTAMP(3)
        WHERE deleted = 0 AND id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        </script>
        """)
    int softDeleteByIds(@Param("ids") List<Long> ids);
}
