package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.teacher.id = ?1 AND t.name LIKE %?2%")
    Page<Team> searchTeamByNameAndTeacherId(Long teacherId, String teamName, Pageable pageable);

    Team findTeamByStudentsId(Long studentId);

    Page<Team> findTeamsByTeacherId(Long teacherId, Pageable pageable);

    Boolean existsByName(String name);

    int countByTeacherId(Long teacherId);
}
