package com.c0324.casestudym5.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(50)" , nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "VARCHAR(64)", nullable = false)
    private String password;

    @Column(nullable = false)
    private Date dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    public enum Gender {MALE, FEMALE}

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String address;

    @OneToOne
    @JoinColumn(name="avatar_id", referencedColumnName = "id")
    private MultiFile avatar;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToMany(mappedBy = "sender")
    private Set<Notification> sentNotifications;

    @OneToMany(mappedBy = "receiver")
    private Set<Notification> receivedNotifications;

}
