package com.c0324.casestudym5.util;

import com.c0324.casestudym5.dto.*;
import com.c0324.casestudym5.model.Comment;
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
        notificationDTO.setSenderName(notification.getSender().getName());
        notificationDTO.setSenderAvatar(notification.getSender().getAvatar() != null ? notification.getSender().getAvatar().getUrl() : AppConstants.URL_DEFAULT_AVATAR);
        notificationDTO.setTimeDifference(DateTimeUtil.getTimeDifference(notification.getCreatedAt()));
        return notificationDTO;
    }

    public static CommentDTO toCommentDTO(Comment comment) {

        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .reply(comment.getReply() != null ? comment.getReply() : "")
                .createdTimeDifference(comment.getCreatedAt() != null ? DateTimeUtil.getTimeDifference(comment.getCreatedAt()) : null)
                .repliedTimeDifference(comment.getRepliedAt() != null ? DateTimeUtil.getTimeDifference(comment.getRepliedAt()) : null)
                .studentName(comment.getStudent() != null ? comment.getStudent().getUser().getName() : "Ẩn danh")
                .teacherName(comment.getTeacher() != null ? comment.getTeacher().getUser().getName() : "Ẩn danh")
                .studentAvatar(comment.getStudent() != null ? comment.getStudent().getUser().getAvatar().getUrl() : AppConstants.URL_DEFAULT_AVATAR)
                .teacherAvatar(comment.getTeacher() != null ? comment.getTeacher().getUser().getAvatar().getUrl() : AppConstants.URL_DEFAULT_AVATAR)
                .build();
    }


}
