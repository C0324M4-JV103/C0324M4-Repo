package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface TeamService {

    public Page<TeamDTO> getPageTeams(int page, String keyword);

    public void deleteTeam(Long teamId);

    public Team getTeamById(Long id);

    public Team getTeamByStudentId(Long studentId);
}
