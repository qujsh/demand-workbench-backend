package com.demandworkbench.requirement.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PublishTaskMapper {
    @Select("""
        SELECT t.*, r.requirement_no, d.decomposition_no
        FROM published_task t
        JOIN requirement r ON r.id = t.requirement_id AND r.deleted = 0
        LEFT JOIN decomposition_record d ON d.id = t.decomposition_id AND d.deleted = 0
        WHERE r.requirement_no = #{requirementNo} AND t.deleted = 0
        """)
    PublishTaskRecord findByRequirementNo(String requirementNo);

    @Insert("""
        INSERT INTO published_task
        (requirement_id, decomposition_id, status, title, category, summary, skills, experience, recruit_count,
         price_model, budget, start_date, deadline, work_mode, recruit_mode, visibility, published_at)
        SELECT id, current_decomposition_id, #{status}, #{title}, #{category}, #{summary}, #{skills}, #{experience}, #{recruitCount},
               #{priceModel}, #{budget}, #{startDate}, #{deadline}, #{workMode}, #{recruitMode}, #{visibility},
               CASE WHEN #{status} = 'published' THEN CURRENT_TIMESTAMP(3) ELSE NULL END
        FROM requirement WHERE requirement_no = #{requirementNo} AND deleted = 0
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PublishTaskRecord record);

    @Update("UPDATE published_task SET task_no = #{taskNo} WHERE id = #{id} AND deleted = 0")
    int updateTaskNo(PublishTaskRecord record);

    @Update("""
        UPDATE published_task SET decomposition_id =
                (SELECT current_decomposition_id FROM requirement WHERE requirement_no = #{requirementNo} AND deleted = 0),
            status = #{status}, title = #{title}, category = #{category},
            summary = #{summary}, skills = #{skills}, experience = #{experience},
            recruit_count = #{recruitCount}, price_model = #{priceModel}, budget = #{budget},
            start_date = #{startDate}, deadline = #{deadline}, work_mode = #{workMode},
            recruit_mode = #{recruitMode}, visibility = #{visibility},
            published_at = CASE WHEN #{status} = 'published'
                THEN COALESCE(published_at, CURRENT_TIMESTAMP(3)) ELSE published_at END
        WHERE id = #{id} AND deleted = 0
        """)
    int update(PublishTaskRecord record);

    @Update("""
        UPDATE published_task t
        JOIN requirement r ON r.id = t.requirement_id AND r.deleted = 0
        SET t.status = 'cancelled'
        WHERE r.requirement_no = #{requirementNo} AND t.status = 'published' AND t.deleted = 0
        """)
    int cancel(String requirementNo);

}
