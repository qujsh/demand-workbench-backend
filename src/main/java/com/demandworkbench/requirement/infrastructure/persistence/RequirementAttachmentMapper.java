package com.demandworkbench.requirement.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RequirementAttachmentMapper {
    @Insert("""
        INSERT INTO requirement_attachment
        (requirement_id, file_name, storage_key, content_type, file_size, sort_order)
        VALUES
        (#{requirementId}, #{fileName}, #{storageKey}, #{contentType}, #{fileSize}, #{sortOrder})
        """)
    void insert(RequirementAttachmentRecord record);

    @Select("""
        SELECT * FROM requirement_attachment
        WHERE requirement_id = #{requirementId} AND deleted = 0
        ORDER BY sort_order ASC, id ASC
        """)
    List<RequirementAttachmentRecord> findByRequirementId(long requirementId);

    @Update("""
        UPDATE requirement_attachment
        SET deleted = 1
        WHERE requirement_id = #{requirementId} AND deleted = 0
        """)
    void softDeleteByRequirementId(long requirementId);
}
