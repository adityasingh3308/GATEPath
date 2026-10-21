
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.SyllabusSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusSubjectRepository
        extends JpaRepository<SyllabusSubject, Long> {

    List<SyllabusSubject> findByBranchOrderByDisplayOrderAsc(String branch);
}
