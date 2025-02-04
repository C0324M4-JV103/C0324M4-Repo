
package com.c0324.casestudym5.controller;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.dto.ProgressReportDTO;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.service.*;
import com.c0324.casestudym5.service.impl.CommentService;
import com.c0324.casestudym5.service.UserService;
import com.c0324.casestudym5.util.AppConstants;
import com.c0324.casestudym5.util.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import java.security.Principal;

@Controller
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;
    private final CommentService commentService;
    private final StudentService studentService;


    @Autowired
    public TopicController(TopicService topicService, StudentService studentService, UserService userService, CommentService commentService) {
        this.topicService = topicService;
        this.studentService = studentService;
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

    @GetMapping("/topics/{topicId}/progress/{phaseNum}")
    public String showProgressReportForm(@PathVariable Long topicId, @PathVariable Integer phaseNum, Model model, RedirectAttributes redirectAttributes) {
        Topic topic = topicService.getTopicById(topicId);
        User currentUser = getCurrentUser();
        Student student = studentService.findStudentByUserId(currentUser.getId());
        if (student == null) {
            return "common/404";
        }
        Team userTeam = student.getTeam();
        // check topic, approved and team
        if (topic == null || topic.getApproved() != AppConstants.TOPIC_APPROVED || !userTeam.getTopic().getId().equals(topic.getId())) {
            return "common/404";
        }
        // check topic status
        if (topic.getStatus() == 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đề tài đã được hoàn thành.");
            return "redirect:/progress/" + topic.getId();
        }
        // check phase number
        if (phaseNum < 1 || phaseNum > 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giai đoạn không hợp lệ");
            return "redirect:/progress/" + topic.getId();
        }
        // get the latest phase
        Phase latestPhase = topic.getPhases().stream()
                .filter(p -> Objects.equals(p.getPhaseNumber(), phaseNum))
                .max(Comparator.comparing(Phase::getReportDate))
                .orElse(null);

        if (latestPhase == null || latestPhase.getStatus() == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giai đoạn này chưa được mở.");
            return "redirect:/progress/" + topic.getId();
        }

        ProgressReportDTO progressReportDTO = CommonMapper.mapPhaseToProgressReportDTO(latestPhase);
        model.addAttribute("topic", topic);
        model.addAttribute("reportTopic", progressReportDTO);
        return "team/progress-report";
    }

    @PostMapping("/topics/handle-progress-report/{id}")
    public String submitProgressReport(@PathVariable Long id, @ModelAttribute ProgressReportDTO progressReportDTO, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Báo cáo không hợp lệ");
            return "redirect:/progress/" + id;
        }

        Topic topic = topicService.getTopicById(id);
        User currentUser = getCurrentUser();
        Student student = studentService.findStudentByUserId(currentUser.getId());
        Team userTeam = student.getTeam();
        if (topic == null || topic.getApproved() != AppConstants.TOPIC_APPROVED || !userTeam.getTopic().getId().equals(topic.getId())) {
            return "common/404";
        }
        String isReported = topicService.submitProgressReport(topic.getId(), progressReportDTO, student);
        if (!isReported.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", isReported);
            return "redirect:/progress/" + topic.getId();
        }
        redirectAttributes.addFlashAttribute("successMessage", "Báo cáo giai đoạn " + progressReportDTO.getPhaseNumber() + " đã được gửi.");
        return "redirect:/progress/" + topic.getId();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userService.findByEmail(userEmail);
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
