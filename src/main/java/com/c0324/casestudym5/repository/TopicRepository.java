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
    Page<Topic> findByStatus(int status, Pageable pageable);
    @Query("select t from Topic t where t.approved = 1")
    Page<Topic> findByApprovedTrueAndStatus(Pageable pageable);

    List<Topic> findByTeam_Id(Long id);
}