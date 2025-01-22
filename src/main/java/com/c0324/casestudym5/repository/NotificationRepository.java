package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    //    @Query("select n from Notification n where n.receiver.id = :receiverId order by  n.createdAt desc")
//    List<Notification> findTop3ByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId);
    @Query(value = "select * from notification n where n.receiver_id = :receiverId order by n.created_at desc LIMIT 3", nativeQuery = true)
    List<Notification> findTop3NotificationsByReceiverId(@Param("receiverId") Long receiverId);

    List<Notification> findByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("select COUNT(n) from Notification n where n.receiver.id = :receiverId and n.isRead = false")
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId); //lấy số lượng thông báo chưa đọc
}
