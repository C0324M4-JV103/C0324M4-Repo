package com.c0324.casestudym5.service;


import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import org.springframework.data.domain.Page;
import java.util.List;

public interface TeamService {

    Page<TeamDTO> getPageTeams(int page, String keyword, User user);
    List<Team> findAll();
    Team save(Team team);
    Team findById(Long teamId);
    boolean existsByName(String name);

    void deleteTeam(Long teamId, User sender);

    void createNewTeam(TeamDTO teamDTO, Student currentStudent);

    int countTeamsByTeacherId(Long teacherId);

    void registerTeacher(Long id, Long teacherId);

}
