package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.RegisterTopicDTO;

public interface TopicService {

    void registerTopic(RegisterTopicDTO registerTopicDTO, String username);
}
