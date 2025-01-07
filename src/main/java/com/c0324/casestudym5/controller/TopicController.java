package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TopicController {

    @Autowired
    private TopicService topicService;

    @GetMapping("/topics")
    public String getTopics(@RequestParam(defaultValue = "0") int page, Model model) {
        PageRequest pageRequest = PageRequest.of(page, 12, Sort.by("id").descending());
        Page<Topic> topics = topicService.getAllTopics(pageRequest);
        model.addAttribute("topics", topics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topics.getTotalPages());
        return "student/topic-list";
    }

    @GetMapping("/topics/{id}")
    public String getTopicDetail(@PathVariable Long id, Model model) {
        Topic topic = topicService.getTopicById(id);
        model.addAttribute("topic", topic);
        return "student/topic-detail";
    }
}
