package com.demandworkbench.requirement.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
        WHERE requirement_id = #{requirementId}
        ORDER BY sort_order ASC, id ASC
        """)
    List<RequirementAttachmentRecord> findByRequirementId(long requirementId);

    @Delete("DELETE FROM requirement_attachment WHERE requirement_id = #{requirementId}")
    void deleteByRequirementId(long requirementId);
}
