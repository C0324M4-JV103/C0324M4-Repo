package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.util.CommonMapper;
import org.hibernate.Hibernate;
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

    public TeamServiceImpl(TeamRepository teamRepository, StudentService studentService, TeacherRepository teacherRepository, NotificationService notificationService) {
        this.teamRepository = teamRepository;
        this.studentService = studentService;
        this.teacherRepository = teacherRepository;
        this.notificationService = notificationService;
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
    public Team findByName(String name) {
        return teamRepository.findTeamByName(name);
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
        Pageable pageable = PageRequest.of(page, 3);
        Teacher teacher = teacherRepository.findTeacherByUserEmail(user.getEmail());
        Page<Team> teams;
        if(keyword.isEmpty()){
            teams = teamRepository.findTeamsByTeacherId(teacher.getId(), pageable);
        } else {
            teams = teamRepository.searchTeamByNameAndTeacherId(teacher.getId(), keyword ,pageable);
        }
        return teams.map(CommonMapper::mapToTeamDTO);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId, User sender) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if(team != null){
            //Update related topics
            Hibernate.initialize(team.getTopic());
            Topic topic = team.getTopic();
            topic.setTeam(null);
            //Update related students and send notification to them
            Hibernate.initialize(team.getStudents());
            List<Student> students = team.getStudents();
            for(Student student : students){
                student.setTeam(null);
                student.setLeader(false);
                studentService.save(student);
                Notification notification = Notification.builder()
                        .content(" đã xóa nhóm " + team.getName() + " mà bạn đang tham gia")
                        .sender(sender)
                        .receiver(student.getUser())
                        .build();
                notificationService.sendNotification(notification);
            }
            //Delete team
            teamRepository.delete(team); 
        }
    }

    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Override
    public Team getTeamByStudentId(Long studentId) {
        return teamRepository.findTeamByStudentsId(studentId);
    }

}
