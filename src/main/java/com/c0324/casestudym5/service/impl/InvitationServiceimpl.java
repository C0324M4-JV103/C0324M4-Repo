package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.InvitationRepository;
import com.c0324.casestudym5.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvitationServiceimpl implements InvitationService {

    @Autowired
    InvitationRepository invitationRepository;
    @Override
    public boolean existsByStudentAndTeam(Student student, Team team) {
        return invitationRepository.existsByStudentAndTeam(student, team);
    }
    @Transactional
    @Override
    public void deleteAllByStudent(Student student) {
        invitationRepository.deleteByStudent(student);
    }
    @Transactional
    @Override
    public void delete(Invitation invitation) {
        invitationRepository.delete(invitation);
    }

    @Override

    public Invitation findById(Long id) {
        return invitationRepository.findById(id).orElse(null);
    }
    @Override

    public void save(Invitation invitation) {
        invitationRepository.save(invitation);
    }
    @Override
    public List<Invitation> findByStudent(Student student) {
        return invitationRepository.findByStudent(student);
    }
}
