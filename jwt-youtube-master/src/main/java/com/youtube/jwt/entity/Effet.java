package com.youtube.jwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Effet implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_effet ;
    @Temporal(TemporalType.DATE)
    private Date date ;
    @Enumerated(EnumType.STRING)
    private  EtatCheque etatCheque ;
    private String rendement;

    @ManyToMany()
    @JsonIgnore
    private List<User> userseffet;
}
