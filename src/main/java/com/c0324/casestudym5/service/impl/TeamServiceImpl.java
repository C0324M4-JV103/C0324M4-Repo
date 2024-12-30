package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.service.TeamService;
import com.c0324.casestudym5.util.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;



@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.teamRepository = teamRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
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
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if(team != null){

        }
    }

    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Override
    public void registTopic(Long teamId, Long topicId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Student student = (Student) auth.getPrincipal();
        Team team = teamRepository.findById(teamId).orElse(null);
        if(team != null && team.getStudents().contains(student) && team.getTopic() == null && student.isLeader()){
            teamRepository.save(team);
            simpMessagingTemplate.convertAndSend("/topic/teams", team);
        }

    }
}
