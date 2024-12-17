package com.c0324.casestudym5.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class PhaseReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToOne
    @JoinColumn(name = "report_file_id", referencedColumnName = "id")
    private MultiFile reportFile;

    @Column(nullable = false)
    private Integer phaseStatus;

    @ManyToOne
    @JoinColumn(name = "phase_id", referencedColumnName = "id")
    private Phase phase;

    @ManyToOne
    @JoinColumn(name = "topic_id", referencedColumnName = "id")
    private Topic topic;
}
