package com.c0324.casestudym5.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isLeader;

    @ManyToOne
    @JoinColumn(name = "clazz_id", referencedColumnName = "id")
    @JsonBackReference
    private Clazz clazz;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="team_id", referencedColumnName = "id")
    private Team team;

    @OneToMany(mappedBy = "student")
    @JsonManagedReference
    private List<Comment> comments;
}
