package com.c0324.casestudym5.service.impl;

import com.c0324.casestudym5.dto.CommentDTO;
import com.c0324.casestudym5.dto.CommentResponse;
import com.c0324.casestudym5.model.*;
import com.c0324.casestudym5.repository.CommentRepository;
import com.c0324.casestudym5.repository.StudentRepository;
import com.c0324.casestudym5.repository.TeacherRepository;
import com.c0324.casestudym5.repository.TopicRepository;
import com.c0324.casestudym5.service.MailService;
import com.c0324.casestudym5.service.NotificationService;
import com.c0324.casestudym5.util.CommonMapper;
import com.c0324.casestudym5.util.DateTimeUtil;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final TopicRepository topicRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationService notificationService;
    private final MailService mailService;

    @Autowired
    public CommentService(CommentRepository commentRepository, StudentRepository studentRepository, TeacherRepository teacherRepository, TopicRepository topicRepository, SimpMessagingTemplate simpMessagingTemplate, NotificationService notificationService, MailService mailService) {
        this.commentRepository = commentRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.topicRepository = topicRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.notificationService = notificationService;
        this.mailService = mailService;
    }

    public List<CommentDTO> getCommentsByTopicId(Long topicId) {
        List<Comment> comments = commentRepository.findTop3ByTopicIdOrderByCreatedAtDesc(topicId);
        List<CommentDTO> responses = new ArrayList<>();

        for (Comment comment : comments) {
            responses.add(CommonMapper.toCommentDTO(comment));
        }

        return responses;
    }

    public void addComment(String content, Long topicId, User creator) {
        Student student = studentRepository.findStudentByUserId(creator.getId());
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if(student != null && topic != null){
            Hibernate.initialize(topic.getApprovedBy());
            Teacher teacher = topic.getApprovedBy();
            Date createdAt = new Date();
            Comment comment = Comment.builder()
                    .content(content)
                    .topic(topic)
                    .student(student)
                    .teacher(teacher)
                    .createdAt(createdAt)
                    .build();
            commentRepository.save(comment);

            // send to socket
            CommentResponse response = CommentResponse.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .timeDifference(DateTimeUtil.getTimeDifference(createdAt))
                    .senderName(creator.getName())
                    .senderAvatar(creator.getAvatar().getUrl())
                    .topicId(topicId)
                    .build();
            simpMessagingTemplate.convertAndSend("/socket/comment", response);

            //send notification
            Notification notification = Notification.builder()
                    .content("có thắc mắc về chủ đề " + topic.getName())
                    .receiver(teacher.getUser())
                    .sender(student.getUser())
                    .createdAt(createdAt)
                    .url("/progress/" + topicId)
                    .build();
            notificationService.sendNotification(notification);
            String subject = "Thắc mắc của sinh viên về đề tài " + topic.getName();
            User receiver = teacher.getUser();

            //send mail
            mailService.sendQuestionEmail(receiver.getEmail(), subject, creator.getName(), receiver.getName(), topic.getName(), topic.getId());
        }

    }

    public void addReply(Long commentId, String content, Long topicId, User creator){
        Teacher teacher = teacherRepository.findTeacherByUserEmail(creator.getEmail());
        Topic topic = topicRepository.findById(topicId).orElse(null);
        Comment parentComment = commentRepository.findById(commentId).orElse(null);
        if(teacher != null && topic != null && parentComment != null){
            Date repliedAt = new Date();
            User receiver = parentComment.getStudent().getUser();
            parentComment.setReply(content);
            parentComment.setRepliedAt(repliedAt);
            parentComment.setTeacher(teacher);
            commentRepository.save(parentComment);

            // send to socket
            CommentResponse response = CommentResponse.builder()
                    .id(parentComment.getId())
                    .content(parentComment.getReply())
                    .timeDifference(DateTimeUtil.getTimeDifference(repliedAt))
                    .senderName(creator.getName())
                    .senderAvatar(creator.getAvatar().getUrl())
                    .topicId(topicId)
                    .build();
            // send to all online users subscribe : /user/socket/comment
            simpMessagingTemplate.convertAndSend("/socket/reply", response);

            //send notification
            Notification notification = Notification.builder()
                    .content("đã trả lời thắc mắc của bạn về chủ đề " + topic.getName())
                    .receiver(receiver)
                    .sender(teacher.getUser())
                    .createdAt(repliedAt)
                    .url("/progress/" + topicId)
                    .build();
            notificationService.sendNotification(notification);

            //send mail
            String subject = "Giải đáp thắc mắc về chủ đề " + topic.getName();
            mailService.sendAnswerEmail(receiver.getEmail(), subject, creator.getName(), receiver.getName(), topic.getName(), topic.getId());
        }
    }

}
