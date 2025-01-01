package com.c0324.casestudym5.util;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.dto.TeamDTO;
import com.c0324.casestudym5.dto.UserDTO;
import com.c0324.casestudym5.model.Notification;
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
        userDTO.setAvatar(user.getAvatar().getUrl());
        return userDTO;
    }

    public static TeamDTO mapToTeamDTO(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        BeanUtils.copyProperties(team, teamDTO);
        teamDTO.setMemberCount(team.getStudents().size());
        teamDTO.setDeadline(team.getTopic() != null ? team.getTopic().getDeadline() : null);
        teamDTO.setStatus(team.getTopic() != null ? team.getTopic().getStatus() : null);
        return teamDTO;
    }

    public static NotificationDTO toNotificationDTO(Notification notification){
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setContent(notification.getContent() != null ? notification.getContent() : "No content");
        notificationDTO.setSenderName(notification.getSender() != null && notification.getSender().getName() != null ? notification.getSender().getName() : "Unknown sender");
        notificationDTO.setSenderAvatar(notification.getSender() != null && notification.getSender().getAvatar() != null && notification.getSender().getAvatar().getUrl() != null ? notification.getSender().getAvatar().getUrl() : AppConstants.URL_DEFAULT_AVATAR);
        notificationDTO.setTimeDifference(notification.getCreatedAt() != null ? DateTimeUtil.getTimeDifference(notification.getCreatedAt()) : "Unknown time");
        return notificationDTO;
    }

}
