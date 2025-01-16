package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;
    private final CommentService commentService;

    @Autowired
    public TopicController(TopicService topicService, UserService userService, CommentService commentService) {
        this.topicService = topicService;
        this.userService = userService;
        this.commentService = commentService;
    }

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

    @MessageMapping("/add-comment")
    public void addComment(@Payload CommentDTO commentDTO , Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        commentService.addComment(commentDTO.getContent(), commentDTO.getTopicId(), currentUser);
    }

    @MessageMapping("/add-reply")
    public void addReply(@Payload CommentDTO commentDTO , Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        commentService.addReply(commentDTO.getId(), commentDTO.getReply(), commentDTO.getTopicId(), currentUser);
    }
}
