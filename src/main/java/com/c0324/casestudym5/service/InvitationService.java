package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import java.util.List;

public interface InvitationService {
    void save(Invitation invitation);
    List<Invitation> findByStudent(Student student);
    Invitation findById(Long id);
    void delete(Invitation invitation);
    boolean existsByStudentAndTeam(Student student, Team team);
    void deleteAllByStudent(Student student);
    void inviteStudent(Long studentId, Student currentStudent, Team currentTeam);
    String handleInvitation(Long invitationId, boolean accept);
}
