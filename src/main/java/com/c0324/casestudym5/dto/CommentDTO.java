package com.c0324.casestudym5.dto;


import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommentDTO {

    private Long id;

    private String content;

    private String reply;

    private Long topicId;

    private String createdTimeDifference;

    private String repliedTimeDifference;

    private String studentName;

    private String teacherName;

    private String studentAvatar;

    private String teacherAvatar;

}
