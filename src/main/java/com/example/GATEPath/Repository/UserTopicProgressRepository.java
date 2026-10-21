
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.UserTopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTopicProgressRepository
        extends JpaRepository<UserTopicProgress, Long> {

    List<UserTopicProgress> findByUser_Id(Long userId);

    Optional<UserTopicProgress> findByUser_IdAndTopic_Id(
            Long userId,
            Long topicId
    );

    long countByUser_Id(Long userId);

    long countByUser_IdAndCompletedTrue(Long userId);
}
