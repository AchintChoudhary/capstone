package com.capstone.scheduler.repository;

import com.capstone.scheduler.model.Category;
import com.capstone.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    Optional<Category> findByNameAndUser(String name, User user);
}
