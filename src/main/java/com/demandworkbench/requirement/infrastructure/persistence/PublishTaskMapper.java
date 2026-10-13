package com.demandworkbench.requirement.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PublishTaskMapper {
    @Select("""
        SELECT t.*, r.requirement_no
        FROM published_task t
        JOIN requirement r ON r.id = t.requirement_id AND r.deleted = 0
        WHERE r.requirement_no = #{requirementNo} AND t.deleted = 0
        """)
    PublishTaskRecord findByRequirementNo(String requirementNo);

    @Insert("""
        INSERT INTO published_task
        (requirement_id, status, title, category, summary, skills, experience, recruit_count,
         price_model, budget, start_date, deadline, work_mode, recruit_mode, visibility, published_at)
        SELECT id, #{status}, #{title}, #{category}, #{summary}, #{skills}, #{experience}, #{recruitCount},
               #{priceModel}, #{budget}, #{startDate}, #{deadline}, #{workMode}, #{recruitMode}, #{visibility},
               CASE WHEN #{status} = 'published' THEN CURRENT_TIMESTAMP(3) ELSE NULL END
        FROM requirement WHERE requirement_no = #{requirementNo} AND deleted = 0
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PublishTaskRecord record);

    @Update("UPDATE published_task SET task_no = #{taskNo} WHERE id = #{id} AND deleted = 0")
    int updateTaskNo(PublishTaskRecord record);

    @Update("""
        UPDATE published_task SET status = #{status}, title = #{title}, category = #{category},
            summary = #{summary}, skills = #{skills}, experience = #{experience},
            recruit_count = #{recruitCount}, price_model = #{priceModel}, budget = #{budget},
            start_date = #{startDate}, deadline = #{deadline}, work_mode = #{workMode},
            recruit_mode = #{recruitMode}, visibility = #{visibility},
            published_at = CASE WHEN #{status} = 'published'
                THEN COALESCE(published_at, CURRENT_TIMESTAMP(3)) ELSE published_at END
        WHERE id = #{id} AND deleted = 0
        """)
    int update(PublishTaskRecord record);

    @Delete("DELETE FROM published_task_work_item WHERE task_id = #{taskId}")
    void deleteWorkItems(long taskId);

    @Insert("""
        INSERT INTO published_task_work_item (task_id, work_item_id, sort_order)
        SELECT #{taskId}, w.id, #{sortOrder}
        FROM work_item w
        JOIN decomposition_record d ON d.id = w.decomposition_id AND d.deleted = 0
        JOIN requirement r ON r.current_decomposition_id = d.id AND r.deleted = 0
        WHERE r.requirement_no = #{requirementNo} AND w.work_item_no = #{workItemNo}
          AND w.type IN ('work', 'subtask') AND w.deleted = 0
        """)
    int insertWorkItem(@Param("taskId") long taskId, @Param("requirementNo") String requirementNo,
                       @Param("workItemNo") String workItemNo, @Param("sortOrder") int sortOrder);

    @Select("""
        SELECT w.work_item_no FROM published_task_work_item tw
        JOIN work_item w ON w.id = tw.work_item_id AND w.deleted = 0
        WHERE tw.task_id = #{taskId} AND tw.deleted = 0 ORDER BY tw.sort_order
        """)
    List<String> findWorkItemNos(long taskId);
}
