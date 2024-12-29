package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Notification;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.util.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, NotificationService notificationService, UserService userService) {
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }


    @Override
    public Page<TeamDTO> getPageTeams(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 3);
        Page<Team> teams;
        if(keyword.isEmpty()){
            teams = teamRepository.findAll(pageable);
        } else {
            teams = teamRepository.searchTeamByName(pageable, keyword);
        }
        return teams.map(CommonMapper::mapToTeamDTO);
    }

    @Override
    public void deleteTeam(Long teamId, User sender) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if(team != null){
            List<Student> students = team.getStudents();
            for(Student student : students){
                Notification notification = Notification.builder()
                        .content("Nhóm của bạn đã bị xóa khỏi hệ thống")
                        .receiver(student.getUser())
                        .sender(sender)
                        .build();
                notificationService.sendNotification(notification);
            }
        }
    }

    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }
}
