package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.TeamRepository;
import com.c0324.casestudym5.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Override
    public Team findByName(String name) {
        return teamRepository.findTeamByName(name);
    }

    @Override
    public Team findById(Long teamId) {
        return teamRepository.findById(teamId).orElse(null);
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
    public boolean existsByName(String name) {
        return teamRepository.existsByName(name);
    }

}
