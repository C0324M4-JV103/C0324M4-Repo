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
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Degree degree;

    public enum Degree {
        MASTER, DOCTOR, PROFESSOR
    }

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "teacher")
    @JsonManagedReference
    private List<Clazz> classes;

    @OneToMany(mappedBy = "teacher")
    @JsonManagedReference
    private List<Team> teams;

    @OneToMany(mappedBy = "teacher")
    @JsonManagedReference
    private List<Comment> replies;
}
