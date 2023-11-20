package com.youtube.jwt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class cheque implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cheque ;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date ;
    @Enumerated(EnumType.STRING)
    private  EtatCheque etatCheque ;
    private int nbTraite = 0;
    private int nbNonTraite = 0;
    private int nbRejete = 0;

    private String causeRejet;
    public void updateEtatCounters() {
        if (this.etatCheque == EtatCheque.Traité) {
            this.nbTraite++;
            this.nbNonTraite--;
        } else if (this.etatCheque == EtatCheque.Non_Traité) {
            this.nbNonTraite++;
        } else if (this.etatCheque == EtatCheque.Rejeté) {
            this.nbRejete++;
            this.nbNonTraite--;
        }
    }
    @ManyToMany()
    @JsonIgnore
    private List<User> userscheque;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

}
