package com.c0324.casestudym5.repository;

import com.c0324.casestudym5.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "select * from notification n where n.receiver_id = :receiverId order by n.created_at desc LIMIT 3", nativeQuery = true)
    List<Notification> findTop3NotificationsByReceiverId(@Param("receiverId") Long receiverId);

    List<Notification> findByReceiverIdAndIsReadFalse(Long receiverId);

}
