package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findNotificationByReceiverId(Long receiverId);

}
