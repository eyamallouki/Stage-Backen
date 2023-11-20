package com.youtube.jwt.dao;

import com.youtube.jwt.entity.EtatCheque;
import com.youtube.jwt.entity.User;
import com.youtube.jwt.entity.cheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;

public interface ChequeDao extends JpaRepository<cheque, Integer> {
    @Query("SELECT c FROM cheque c WHERE DATE(c.date) = DATE(:systemDate)")
    List<cheque> findChequesWithSimilarDate(LocalDate systemDate);

    @Query("SELECT COUNT(c) FROM cheque c WHERE DATE(c.date) = DATE(:systemDate)")
    int countChequesWithSimilarDate(LocalDate systemDate);

    @Query("SELECT COUNT(c) FROM cheque c WHERE c.etatCheque = :etat")
    int countByEtatCheque(@Param("etat") EtatCheque etat);

    List<cheque> findAllByUserschequeInAndEtatCheque(List<User> users, EtatCheque etatCheque);

    @Query("SELECT COUNT(c) FROM cheque c JOIN c.userscheque u WHERE c.etatCheque = :etat AND u = :user")
    long countByEtatChequeAndUser(@Param("etat") EtatCheque etat, @Param("user") User user);

    @Query("SELECT COUNT(c) FROM cheque c JOIN c.userscheque u WHERE u = :user")
    long countByUser(@Param("user") User user);

}
