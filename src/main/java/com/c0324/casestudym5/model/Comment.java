package com.c0324.casestudym5.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String content;

    @Column(columnDefinition = "VARCHAR(50)")
    private String reply;

    @Column(columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    private Teacher teacher;
}
