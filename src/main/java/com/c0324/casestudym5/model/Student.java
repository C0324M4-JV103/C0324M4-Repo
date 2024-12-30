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

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean invited; // Mặc định chưa được mời


    @ManyToOne
    @JoinColumn(name = "clazz_id", referencedColumnName = "id")
    private Clazz clazz;

    @ManyToOne
    @JoinColumn(name="team_id", referencedColumnName = "id")
    private Team team;

    @OneToMany(mappedBy = "student")
    private List<Comment> comments;

    public String getTeamStatus() {
        return team == null ? "Chưa có nhóm" : "Đã có nhóm";
    }

}
