package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.model.Phase;
import com.c0324.casestudym5.model.Student;
import com.c0324.casestudym5.model.Team;
import com.c0324.casestudym5.model.Topic;
import com.c0324.casestudym5.service.PhaseService;
import com.c0324.casestudym5.service.StudentService;
import com.c0324.casestudym5.service.TopicService;
import com.c0324.casestudym5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@Controller
public class PhaseController {

    private final PhaseService phaseService;
    private final TopicService topicService;
    private final StudentService studentService;

    @Autowired

    public PhaseController(PhaseService phaseService, TopicService topicService, StudentService studentService) {
        this.phaseService = phaseService;
        this.topicService = topicService;
        this.studentService = studentService;
    }


    @GetMapping("/progress/{topicId}")
    public String showTopicProgress(@PathVariable Long topicId, Model model) {

        Topic topic = topicService.getTopicById(topicId);
        Team team = topic.getTeam();
        List<Student> students = studentService.findStudentsByTeamId(team.getId());
        List<Phase> phases = phaseService.findPhasesByTopic(topic);

        model.addAttribute("team", team);
        model.addAttribute("topic", topic);
        model.addAttribute("students", students);
        model.addAttribute("phases", phases);

        return "phase";
    }
}
