package com.reisfal.falbackend.repository;

import com.reisfal.falbackend.model.Fortune;
import com.reisfal.falbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FortuneRepository extends JpaRepository<Fortune, Long> {
    List<Fortune> findByUser(User user);
}
