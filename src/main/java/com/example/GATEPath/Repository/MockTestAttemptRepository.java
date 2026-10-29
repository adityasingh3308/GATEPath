
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.MockTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockTestAttemptRepository
        extends JpaRepository<MockTestAttempt, Long> {

    List<MockTestAttempt> findByUser_IdAndMockTest_IdOrderByAttemptNumberAsc(
            Long userId,
            Long mockTestId
    );

    long countByUser_IdAndMockTest_Id(
            Long userId,
            Long mockTestId
    );

    Optional<MockTestAttempt> findByUser_IdAndMockTest_IdAndStatus(
            Long userId,
            Long mockTestId,
            String status
    );

    List<MockTestAttempt> findByUser_IdOrderByStartedAtDesc(Long userId);
}
