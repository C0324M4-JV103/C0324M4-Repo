package com.c0324.casestudym5.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    public void sendMailInvitedTeamToStudent(String to, String subject, String recipientName, String senderName , String teamName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            // Create the email content using Thymeleaf
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("senderName", senderName);
            context.setVariable("teamName", teamName);
            String content = templateEngine.process("common/invited-team-mail", context);
            helper.setText(content, true); // set true to send HTML content
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage(), e);
        }
    }

    public void sendRegisterTopicEmail(String to, String subject, String senderName, String teacherName, String topicName, String teamName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            // Create the email content using Thymeleaf
            Context context = new Context();
            context.setVariable("senderName", senderName);
            context.setVariable("teacherName", teacherName);
            context.setVariable("topicName", topicName);
            context.setVariable("teamName", teamName);
            String content = templateEngine.process("common/register-topic-mail", context);

            helper.setText(content, true); // set true to send HTML content
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage(), e);
        }
    }
}
