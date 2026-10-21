
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.PasswordResetToken;
import com.example.GATEPath.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
