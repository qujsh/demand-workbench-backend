package com.demandworkbench.decomposition.infrastructure.persistence;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkItemMapper {
    @Insert("""
        INSERT INTO work_item
        (id, decomposition_id, parent_id, type, name, description, status, role_name, priority,
         start_date, end_date, dependency_text, sort_order, created_at, updated_at)
        VALUES
        (#{id}, #{decompositionId}, #{parentId}, #{type}, #{name}, #{description}, #{status}, #{roleName}, #{priority},
         #{startDate}, #{endDate}, #{dependencyText}, #{sortOrder}, #{createdAt}, #{updatedAt})
        """)
    void insert(WorkItemRecord record);

    @Select("SELECT * FROM work_item WHERE decomposition_id = #{decompositionId} ORDER BY sort_order ASC")
    List<WorkItemRecord> findByDecompositionId(String decompositionId);

    @Delete("DELETE FROM work_item WHERE decomposition_id = #{decompositionId}")
    void deleteByDecompositionId(String decompositionId);
}
