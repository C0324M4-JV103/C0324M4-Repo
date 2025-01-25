package com.c0324.casestudym5.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.c0324.casestudym5.model.Topic;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final BlockingQueue<MimeMessage> queue;

    @Autowired
    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.queue = new LinkedBlockingQueue<>();
        this.start();
    }

    private void start() {
        Thread newThread = new Thread(() -> {
            while (true) {
                try {
                    MimeMessage message = queue.take();
                    mailSender.send(message);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable e) {
                    System.err.println("Send mail error: " + e);
                }
            }
        });
        newThread.setName("mail-sender");
        newThread.start();
    }

    private void sendEmail(String to, String subject, Context context, String templateName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            // Tạo nội dung email từ Thymeleaf
            String content = templateEngine.process(templateName, context);
            helper.setText(content, true); // set true để gửi nội dung HTML
            queue.add(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage(), e);
        }
    }

    public void sendMailInvitedTeamToStudent(String to, String subject, String recipientName, String senderName, String teamName) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName);
        context.setVariable("senderName", senderName);
        context.setVariable("teamName", teamName);
        sendEmail(to, subject, context, "common/invited-team-mail");
    }

    public void sendMailRejectToTeam(Topic topic, String to, String subject, String teamName,
                                     String teacherName, String topicName, Long topicId, String action, String reason) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {

            // Create the email content using Thymeleaf
            Context context = new Context();
            context.setVariable("teamName", teamName);
            context.setVariable("teacherName", teacherName);
            context.setVariable("topicName", topicName);
            context.setVariable("topicId", topicId);
            context.setVariable("topic", topic);
            context.setVariable("action", action);
            context.setVariable("reason", reason); // Pass reason to email template
            sendEmail(to, subject, context, "common/topic-notification-mail");

        });
        executorService.shutdown();
    }

    public void sendMailApprovedToTeam(Topic topic, String to, String subject, String teamName,
                                       String teacherName, String topicName, Long topicId, String action) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            // Create the email content using Thymeleaf
            Context context = new Context();
            context.setVariable("teamName", teamName);
            context.setVariable("teacherName", teacherName);
            context.setVariable("topicName", topicName);
            context.setVariable("topicId", topicId);
            context.setVariable("topic", topic);
            context.setVariable("action", action);
            sendEmail(to, subject, context, "common/topic-notification-mail");
        });
        executorService.shutdown();
    }

    public void sendRegisterTopicEmail(String to, String subject, String senderName, String teacherName, String topicName, String teamName) {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("teacherName", teacherName);
        context.setVariable("topicName", topicName);
        context.setVariable("teamName", teamName);
        sendEmail(to, subject, context, "common/register-topic-mail");
    }

    public void sendSubmittedProgressReportEmail(String to, String subject, String senderName, String teacherName, Topic topic, String teamName, String phaseName) {
        Context context = new Context();
        context.setVariable("studentName", senderName);
        context.setVariable("teacherName", teacherName);
        context.setVariable("topic", topic);
        context.setVariable("teamName", teamName);
        context.setVariable("phaseName", phaseName);
        sendEmail(to, subject, context, "common/progress-report-mail");
    }

    public void sendDeleteTeamEmail(String to, String subject, String senderName, String studentName, String teamName) {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("studentName", studentName);
        context.setVariable("teamName", teamName);
        sendEmail(to, subject, context, "common/delete-team-email");
    }

    public void sendQuestionEmail(String to, String subject, String senderName, String teacherName, String topicName, Long topicId) {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("teacherName", teacherName);
        context.setVariable("topicName", topicName);
        context.setVariable("topicId", topicId);
        sendEmail(to, subject, context, "common/student-question-mail");
    }


    public void sendAnswerEmail(String to, String subject, String senderName, String studentName, String topicName, Long topicId) {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("studentName", studentName);
        context.setVariable("topicName", topicName);
        context.setVariable("topicId", topicId);
        sendEmail(to, subject, context, "common/teacher-answer-mail");
    }

    public void sendNewDeadlineEmail(String to, String subject, String senderName, String studentName, String topicName, Long topicId) {
        Context context = new Context();
        context.setVariable("senderName", senderName);
        context.setVariable("studentName", studentName);
        context.setVariable("topicName", topicName);
        context.setVariable("topicId", topicId);
        sendEmail(to, subject, context, "common/new-deadline-mail");
    }
}