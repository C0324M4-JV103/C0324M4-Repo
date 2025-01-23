package com.c0324.casestudym5.service;

import com.c0324.casestudym5.dto.NotificationDTO;
import com.c0324.casestudym5.model.Notification;
import com.c0324.casestudym5.model.User;
import com.c0324.casestudym5.repository.NotificationRepository;
import com.c0324.casestudym5.util.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void sendNotification(Notification notification) {

        User receiver = notification.getReceiver();
        notification.setCreatedAt(new Date());
        save(notification);
        NotificationDTO response = CommonMapper.toNotificationDTO(notification);

        int unreadCount = countUnreadNotifications(receiver.getId());

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getEmail(), "/socket/notification",
                Map.of("notification", response,
                        "unreadCount", unreadCount)
        );
        notificationRepository.save(notification);

    }

    public List<NotificationDTO> getTop3NotificationsByUserIdDesc(Long receiverId) {
        List<Notification> notifications = notificationRepository.findTop3NotificationsByReceiverId(receiverId);
        return notifications.stream()
                .map(CommonMapper::toNotificationDTO)
                .collect(Collectors.toList());
    }


    public void markAllAsRead(Long receiverId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdAndIsReadFalse(receiverId);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public int countUnreadNotifications(Long userId) {
        List<Notification> notificationList = notificationRepository.findTop3NotificationsByReceiverId(userId);
        return (int) notificationList.stream().filter(notification -> !notification.isRead()).count();
    }
}
