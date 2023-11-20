package com.youtube.jwt.controller;

import com.youtube.jwt.entity.EtatCheque;
import lombok.Getter;
import lombok.Setter;


import java.util.Date;

@Getter
@Setter
public class ChequeWithImageResponse {
    private Integer id_cheque;
    private Date date;
    private EtatCheque etatCheque;
    private ImageResponse image;



    // Getters and setters

    @Getter
    @Setter
    public static class ImageResponse {
        private String name;
        private String type;
        private byte[] imageData;



        // Getters and setters
    }
}

