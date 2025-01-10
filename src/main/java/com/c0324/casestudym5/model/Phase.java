package com.c0324.casestudym5.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Phase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id", referencedColumnName = "id")
    private Topic topic;

    @Column(nullable = false)
    private Integer phaseNumber;

    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer phaseProgressPercent;

    @Column(columnDefinition = "INT DEFAULT 0", nullable = false)
    private Integer status;

    private LocalDate startDate;

    private LocalDate endDate;

    @OneToOne
    private MultiFile reportFile;

    @Column(columnDefinition = "TEXT")
    private String reportContent;

}
