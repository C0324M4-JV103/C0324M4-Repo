package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Blogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogsRepository extends JpaRepository<Blogs, Long> {
    List<Blogs> findTop6ByOrderByCreatedAtDesc();
} 