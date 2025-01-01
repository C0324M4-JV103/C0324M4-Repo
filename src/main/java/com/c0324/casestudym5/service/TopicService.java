package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TopicService {

    void registerTopic(RegisterTopicDTO registerTopicDTO, String username);

    Page<Topic> getAllTopics(Pageable pageable);
}
