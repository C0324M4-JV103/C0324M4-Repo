package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface TeamService {

    List<Team> findAll();
    Team save(Team team);
    Team findByName(String name);
    Team findById(Long teamId);
    boolean existsByName(String name);
    public Page<TeamDTO> getPageTeams(int page, String keyword);

    public void deleteTeam(Long teamId);

    public Team getTeamById(Long id);
}
