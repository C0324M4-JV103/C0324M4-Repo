package com.c0324.casestudym5.service;

import com.c0324.casestudym5.model.Notification;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    public void sendNotification(Notification notification){
        User sender = notification.getSender();
        User receiver = notification.getReceiver();
        notificationRepository.save(notification);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiver.getEmail()), "/queue/notify", notification);
    }

    public List<Notification> getNotificationsByUserId(Long receiverId){
        return notificationRepository.findNotificationByReceiverId(receiverId);
    }
}
