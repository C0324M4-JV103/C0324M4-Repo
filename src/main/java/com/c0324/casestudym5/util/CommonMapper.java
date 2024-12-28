package com.c0324.casestudym5.util;

import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.User;
import org.springframework.beans.BeanUtils;

public class CommonMapper {

    public static UserDTO mapUserToUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setDob(user.getDob());
        userDTO.setGender(user.getGender().name());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setAddress(user.getAddress());
        return userDTO;
    }

    public static TeamDTO mapToTeamDTO(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        BeanUtils.copyProperties(team, teamDTO);
        teamDTO.setMemberCount(team.getStudents().size());
        teamDTO.setDeadline(team.getTopic().getDeadline());
        teamDTO.setStatus(team.getTopic().getStatus());
        return teamDTO;
    }

}
