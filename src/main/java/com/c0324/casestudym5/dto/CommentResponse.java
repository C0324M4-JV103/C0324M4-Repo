package com.c0324.casestudym5.dto;


import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CommentResponse {

    private Long id;

    private String content;

    private String senderAvatar;

    private String senderName;

    private String timeDifference;

    private Long topicId;

}
