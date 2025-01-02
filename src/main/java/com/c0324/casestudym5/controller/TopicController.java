package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class TopicController {

    @Autowired
    private TopicService topicService;

    @GetMapping("/topics")
    public String getTopics(Model model) {
        List<Topic> topics = topicService.getAllTopics(Pageable.unpaged()).getContent();
        model.addAttribute("topics", topics);
        return "student/topic-list";
    }

    @GetMapping("/topics/{id}")
    public String getTopicDetail(@PathVariable Long id, Model model) {
        Topic topic = topicService.getTopicById(id);
        model.addAttribute("topic", topic);
        return "student/topic-detail";
    }
}
