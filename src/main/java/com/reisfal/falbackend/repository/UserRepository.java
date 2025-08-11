package com.reisfal.falbackend.repository;

import com.reisfal.falbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
<<<<<<< HEAD

}


=======
    Optional<User> findByUsernameOrEmail(String username, String email);

    // 🔽 ekle (email'i case-insensitive aramak için)
    Optional<User> findByEmailIgnoreCase(String email);
}
>>>>>>> recover-2157
