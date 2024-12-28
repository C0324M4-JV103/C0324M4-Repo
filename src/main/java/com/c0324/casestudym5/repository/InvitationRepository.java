package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByStudent(Student student);
    boolean existsByStudentAndTeam(Student student, Team team);
    void deleteByStudent(Student student);

}
