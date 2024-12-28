package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findTeamByName(String name);
    boolean existsByName(String name);


    @Query("SELECT t FROM Team t WHERE t.name LIKE %:name%")
    Page<Team> searchTeamByName(Pageable pageable, String name);
}
