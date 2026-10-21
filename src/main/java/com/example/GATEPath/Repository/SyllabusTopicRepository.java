
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.SyllabusTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusTopicRepository
        extends JpaRepository<SyllabusTopic, Long> {

    List<SyllabusTopic> findBySubject_IdOrderByDisplayOrderAsc(Long subjectId);
}
