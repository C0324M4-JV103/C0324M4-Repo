package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicService {

    boolean registerTopic(RegisterTopicDTO registerTopicDTO, String username);

    Page<Topic> getAllTopics(Pageable pageable);

    List<Topic> getTopicCurrentStudent(Long id);

    Page<Topic> findByStatus(int status, Pageable pageable);

    Topic getTopicById(Long id);

    List<Topic> getLatestTopics(int limit);

    List<Topic> getPendingTopics();
    void approveTopic(Long id);
    void rejectTopic(Long id);
    Page<Topic> getPendingTopicsPage(Pageable pageable);
}
