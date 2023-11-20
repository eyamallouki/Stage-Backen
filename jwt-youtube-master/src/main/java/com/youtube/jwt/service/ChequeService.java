package com.youtube.jwt.service;

import com.youtube.jwt.controller.ChequeWithImageResponse;
import com.youtube.jwt.entity.EtatCheque;
import com.youtube.jwt.entity.cheque;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ChequeService {

    cheque addcheque(cheque ch);


    public void assignImageToCheque(Integer id_cheque, MultipartFile imageFile) throws IOException;

    public ChequeWithImageResponse getChequeWithImage(Integer id_cheque) ;

    public List<cheque> getChequesWithSimilarDate();

    public int countChequesWithSimilarDate();

    public int getNumberOfProcessedCheques();

    public int getNumberOfNonProcessedCheques() ;

    public int getNumberOfRejectedCheques();



    public Map<String, Integer> divideChequesAmongUsers();
    public cheque getChequeById(Integer id_cheque);

    public byte[] getChequeImageBytes(Integer id_cheque);

    public List<cheque> getAllCheques();

    public Map<String, Integer> divideChequesAmongUsers1(String username);


    public int getNumberOfChequesForCurrentUser();

    List<cheque> getTraitedChequesWithDetailsForCurrentUser(String username);
    public Map<String, List<cheque>> getNonTraitedChequesForUsers(List<String> usernames);

    public void traiterCheque(Integer idCheque,String causeRejet);
    public void rejeterCheque(Integer idCheque,String causeRejet);
    public void assignerChequeAUtilisateur(String username, Integer chequeId) throws ChequeDejaAttribueException, IllegalArgumentException;
    public List<cheque> getRejectedChequesForCurrentUser(String username);
    public String calculerRendement();
    double calculerPourcentageRendement();
    public void updateChequeEtat(Integer idCheque, EtatCheque etatCheque, String causeRejet);
    public Map<String, Object> calculerPourcentageRendementParUtilisateur();
}
