package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.RegisterTeamDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import org.springframework.data.domain.Page;
import java.util.List;

public interface TeamService {

    public Page<TeamDTO> getPageTeams(int page, String keyword, User user);
    List<Team> findAll();
    Team save(Team team);
    Team findByName(String name);
    Team findById(Long teamId);
    boolean existsByName(String name);

    void deleteTeam(Long teamId, User sender);

    public Team getTeamById(Long id);

    public Team getTeamByStudentId(Long studentId);
    Team createNewTeam(TeamDTO teamDTO, Student currentStudent);


}
