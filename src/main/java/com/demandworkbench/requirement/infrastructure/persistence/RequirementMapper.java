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
        (#{titleCode}, #{title}, #{userDescription}, #{aiDescription}, #{stage}, #{currentDecompositionId})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RequirementRecord record);

    @Update("UPDATE requirement SET requirement_no = #{requirementNo} WHERE id = #{id}")
    int updateRequirementNo(RequirementRecord record);

    @Update("""
        UPDATE requirement
        SET title = #{title}, user_description = #{userDescription}, ai_description = #{aiDescription},
            stage = #{stage}, current_decomposition_id = #{currentDecompositionId}
        WHERE id = #{id}
        """)
    int update(RequirementRecord record);

    @Select("SELECT * FROM requirement WHERE id = #{id}")
    RequirementRecord findById(long id);

    @Select("SELECT * FROM requirement WHERE requirement_no = #{requirementNo}")
    RequirementRecord findByRequirementNo(String requirementNo);

    @Select("SELECT * FROM requirement ORDER BY updated_at DESC, id DESC")
    List<RequirementRecord> findAll();
}
