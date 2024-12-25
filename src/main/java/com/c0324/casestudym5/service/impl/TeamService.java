package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }
}
