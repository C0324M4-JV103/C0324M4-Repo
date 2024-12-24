package com.c0324.casestudym5.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer status;

    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne
    @JoinColumn(name="image_id")
    private MultiFile image;

    @OneToMany(mappedBy = "topic")
    private Set<Phase> phases;

    @OneToMany(mappedBy = "topic")
    private Set<PhaseReport> reports;
}
