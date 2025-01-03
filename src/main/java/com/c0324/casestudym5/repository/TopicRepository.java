package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Page<Topic> findByApprovedTrue(Pageable pageable);
    Page<Topic> findByApprovedFalse(Pageable pageable);
    List<Topic> findByApprovedFalse();
    @Query("select t from Topic t where t.approved = true and t.status = 1")
    Page<Topic> findByApprovedTrueAndStatus(Pageable pageable);
}