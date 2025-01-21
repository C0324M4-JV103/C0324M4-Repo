package com.c0324.casestudym5.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
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

    @OneToOne
    @JoinColumn(name="description_id")
    private MultiFile description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer status;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date deadline;

    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne
    @JoinColumn(name="image_id")
    private MultiFile image;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private Set<Phase> phases;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int approved ;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher approvedBy;

    @OneToMany(mappedBy = "topic")
    private List<Comment> comments;
}
