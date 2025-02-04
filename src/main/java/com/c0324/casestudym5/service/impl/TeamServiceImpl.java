package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.util.CommonMapper;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final StudentService studentService;
    private final TeacherRepository teacherRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

    public TeamServiceImpl(TeamRepository teamRepository, StudentService studentService,
                           TeacherRepository teacherRepository, NotificationService notificationService, MailService mailService) {
        this.teamRepository = teamRepository;
        this.studentService = studentService;
        this.teacherRepository = teacherRepository;
        this.notificationService = notificationService;
        this.mailService = mailService;
    }


    @Override
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Override
    public Team save(Team team) {
        return teamRepository.save(team);
    }

    @Override
    public Team findById(Long teamId) {
        return teamRepository.findById(teamId).orElse(null);
    }

    @Override
    public boolean existsByName(String name) {
        return teamRepository.existsByName(name);
    }

    @Override
    public Page<TeamDTO> getPageTeams(int page, String keyword, User user) {
        Pageable pageable = PageRequest.of(page, 5);
        Teacher teacher = teacherRepository.findTeacherByUserEmail(user.getEmail());
        Page<Team> teams;
        if(keyword.isEmpty()){
            teams = teamRepository.findTeamsByTeacherId(teacher.getId(), pageable);
        } else {
            teams = teamRepository.searchTeamByNameAndTeacherId(teacher.getId(), keyword ,pageable);
        }
        List<Team> filteredTeams = teams.stream()
                .filter(team -> !team.getStudents().isEmpty())
                .toList();
        return new PageImpl<>(filteredTeams, pageable, filteredTeams.size())
                .map(CommonMapper::mapToTeamDTO);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId, User sender) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
            // Update related topics
            Topic topic = team.getTopic();
            if (topic != null) {
                topic.setTeam(null);
            }

            //Send notification and email to students in team
            List<Student> students = team.getStudents();
            String subject = "Thông báo xóa nhóm - " + team.getName();
            for (Student student : students) {
                // Remove team from student
                student.setTeam(null);
                student.setLeader(false);

                // Send email to student
                mailService.sendDeleteTeamEmail(student.getUser().getEmail(), subject, sender.getName(), student.getUser().getName(), team.getName());

                // Send notification to student
                Notification notification = Notification.builder()
                        .content(" đã xóa nhóm " + team.getName() + " mà bạn đang tham gia")
                        .sender(sender)
                        .receiver(student.getUser())
                        .url(null)
                        .build();
                notificationService.sendNotification(notification);
            }

            // Batch update students
            studentService.saveAll(students);

            // Delete team
            teamRepository.delete(team);
        }
    }


    @Override
    public void createNewTeam(TeamDTO teamDTO, Student currentStudent) {
        Team newTeam = new Team();
        newTeam.setName(teamDTO.getName());
        newTeam.setStudents(List.of(currentStudent));
        teamRepository.save(newTeam);
        currentStudent.setTeam(newTeam);
        currentStudent.setLeader(true);
        studentService.save(currentStudent);
    }


    @Override
    public void registerTeacher(Long teamId, Long teacherId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
            if (team.getTopic() != null && teacherId == 0) {
                throw new IllegalStateException("Đã đăng ký đề tài, không được hủy giáo viên.");
            }
            if (team.getTopic() != null) {
                throw new IllegalStateException("Nhóm đã đăng ký đề tài, không thể đăng ký giáo viên mới.");
            }
            if (teacherId == 0) {
                team.setTeacher(null);
                teamRepository.save(team);
            } else {
                Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
                if (teacher != null) {
                    team.setTeacher(teacher);
                    teamRepository.save(team);
                }
            }
        }
    }




    @Override
    public int countTeamsByTeacherId(Long teacherId) {
        return teamRepository.countByTeacherId(teacherId);
    }

}
