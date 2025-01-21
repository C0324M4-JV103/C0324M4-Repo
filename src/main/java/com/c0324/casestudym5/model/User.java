package com.c0324.casestudym5.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
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
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
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
    @JsonBackReference
    private Set<Notification> sentNotifications;

    @OneToMany(mappedBy = "receiver")
    @JsonBackReference
    private Set<Notification> receivedNotifications;

}
