package com.c0324.casestudym5.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class NotificationDTO {

    private Long id;

    private String content;

    private String timeDifference;

    private String senderName;

    private String senderAvatar;

    private String url;


}
