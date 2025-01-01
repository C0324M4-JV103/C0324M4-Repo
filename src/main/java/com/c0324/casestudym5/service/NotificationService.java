package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.Notification;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.NotificationRepository;
import com.c0324.casestudym5.util.CommonMapper;
import com.c0324.casestudym5.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    public void save(Notification notification){
        notificationRepository.save(notification);
    }


    public void sendNotification(Notification notification){
        User receiver = notification.getReceiver();
        notification.setCreatedAt(new Date());
        save(notification);
        NotificationDTO response = CommonMapper.toNotificationDTO(notification);
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiver.getEmail()), "/socket/notification", response);
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(Long receiverId){
        return notificationRepository.findNotificationByReceiverId(receiverId);
    }
}
