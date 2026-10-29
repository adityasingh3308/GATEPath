
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.MockTestQuestionPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestQuestionPoolRepository
        extends JpaRepository<MockTestQuestionPool, Long> {

    List<MockTestQuestionPool> findByMockTest_IdAndActiveTrue(Long mockTestId);

    List<MockTestQuestionPool> findByMockTest_IdAndSectionAndActiveTrue(
            Long mockTestId,
            String section
    );
}
