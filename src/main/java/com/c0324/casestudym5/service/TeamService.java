
package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import org.springframework.data.domain.Page;
import java.util.List;

public interface TeamService {

    List<Team> findAll();
    Team save(Team team);
    Team findByName(String name);
    Team findById(Long teamId);
    boolean existsByName(String name);

    Page<TeamDTO> getPageTeams(int page, String keyword);
    void deleteTeam(Long teamId, User sender);
    Team getTeamById(Long id);

}
