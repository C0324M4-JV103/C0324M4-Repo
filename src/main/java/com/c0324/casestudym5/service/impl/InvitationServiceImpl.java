package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Invitation;
import com.c0324.casestudym5.model.Notification;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.InvitationRepository;
import com.c0324.casestudym5.service.InvitationService;
import com.c0324.casestudym5.service.MailService;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;
    private final StudentService studentService;
    private final MailService mailService;
    private final NotificationService notificationService;
    @Autowired
    public InvitationServiceImpl(InvitationRepository invitationRepository, StudentService studentService, MailService mailService, NotificationService notificationService) {
        this.invitationRepository = invitationRepository;
        this.studentService = studentService;
        this.mailService = mailService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean existsByStudentAndTeam(Student student, Team team) {
        return invitationRepository.existsByStudentAndTeam(student, team);
    }

    @Override
    public void inviteStudent(Long studentId, Student currentStudent, Team currentTeam) {
        // Láº¥y email cá»§a sinh viÃªn Ä‘Æ°á»£c má»�i
        String email = studentService.getStudentEmailById(studentId);
        Student invitedStudent = studentService.findById(studentId);
        if (invitedStudent.getTeam() == null && !invitationRepository.existsByStudentAndTeam(invitedStudent, currentTeam)) {
            Invitation invitation = new Invitation();
            invitation.setStudent(invitedStudent);
            invitation.setTeam(currentTeam);
            invitation.setInviter(currentStudent);
            invitationRepository.save(invitation);
        }
        // Tạo subject và content cho email
        String subject = "Lời mời tham gia nhóm từ " + currentTeam.getName();
        mailService.sendMailInvitedTeamToStudent(email, subject, invitedStudent.getUser().getName(), currentStudent.getUser().getName(), currentTeam.getName());
        // Tạo notification websocket
        Notification notification = new Notification();
        notification.setSender(currentStudent.getUser());
        notification.setReceiver(invitedStudent.getUser());
        notification.setContent(" đã gửi đến bạn 1 lời mời tham gia Nhóm: " + currentTeam.getName());
        notification.setCreatedAt(new Date());
        notificationService.sendNotification(notification);
    }

    @Override
    public void notifyStudentsAboutTopic(Long teamId, String topicName, String action, Long topicId, String teamName) {

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
