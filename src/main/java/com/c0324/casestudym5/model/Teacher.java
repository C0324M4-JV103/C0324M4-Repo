package com.c0324.casestudym5.model;

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

    @OneToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

<<<<<<< HEAD
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Degree degree;

    public enum Degree {
        MASTER, DOCTOR, PROFESSOR
    }

=======
    @OneToOne
    @JoinColumn(name = "degree_id")
    private Degree degree;

>>>>>>> 467cbbf345d993ca64dd5dffb104b55f52e9266a
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "teacher")
    private List<Clazz> classes;

    @OneToMany(mappedBy = "teacher")
    private List<Team> teams;

    @OneToMany(mappedBy = "teacher")
    private List<Comment> replies;
}
