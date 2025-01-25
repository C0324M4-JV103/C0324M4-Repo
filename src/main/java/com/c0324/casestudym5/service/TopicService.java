package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.ProgressReportDTO;
import com.c0324.casestudym5.dto.RegisterTopicDTO;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface TopicService {

    boolean registerTopic(RegisterTopicDTO registerTopicDTO, String username);

    Page<Topic> getAllTopics(Pageable pageable);

    Page<Topic> findByStatus(int status, Pageable pageable);

    Topic getTopicById(Long id);

    List<Topic> getLatestTopics(int limit);
    void approveTopic(Long id);
    void rejectTopic(Long id, String reason);
    Page<Topic> getPendingTopicsPage(Pageable pageable);

    String submitProgressReport(Long topicId, ProgressReportDTO progressReportDTO, Student student);
    void setNewDeadline(Long teamId, Date newDeadline, User setBy) throws ParseException;

    Page<Topic> getTopicByTeacher(Long id, PageRequest pageRequest);
}
