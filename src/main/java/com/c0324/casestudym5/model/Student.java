package com.c0324.casestudym5.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    private String code;

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
    @JoinColumn(name="team_id", referencedColumnName = "id")
    @JsonBackReference
    private Team team;

    @OneToMany(mappedBy = "student")
    @JsonBackReference
    private List<Comment> comments;

    public String getTeamStatus() {
        return team == null ? "Chưa có nhóm" : team.getName();
    }

}
