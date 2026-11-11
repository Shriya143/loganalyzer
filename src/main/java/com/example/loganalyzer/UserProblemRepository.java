package com.example.loganalyzer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProblemRepository extends JpaRepository<UserProblem, Long> {

    List<UserProblem> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}
