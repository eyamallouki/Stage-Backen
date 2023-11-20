package com.youtube.jwt.service;

import com.youtube.jwt.controller.ChequeWithImageResponse;
import com.youtube.jwt.dao.ChequeDao;
import com.youtube.jwt.dao.ImageDao;
import com.youtube.jwt.dao.UserDao;
import com.youtube.jwt.entity.EtatCheque;
import com.youtube.jwt.entity.Image;
import com.youtube.jwt.entity.User;
import com.youtube.jwt.entity.cheque;
import com.youtube.jwt.util.ImageUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChequeServiceimp implements ChequeService {

    private final ChequeDao chequeDao ;
    private final ImageDao imageDao;

    private  final UserDao userDao ;


    //Add cheque
    @Override
    public cheque addcheque(cheque ch) {
        // Définir l'état par défaut comme "non-traité"
        ch.setEtatCheque(EtatCheque.Non_Traité);
        return chequeDao.save(ch);
    }

    //Assign image lel cheque :
    public void assignImageToCheque(Integer id_cheque, MultipartFile imageFile) throws IOException {
        Optional<cheque> optionalCheque = chequeDao.findById(id_cheque);

        if (optionalCheque.isPresent()) {
            cheque ch = optionalCheque.get();

            Image image = new Image();
            image.setName(imageFile.getOriginalFilename());
            image.setType(imageFile.getContentType());
            image.setImage(ImageUtility.compressImage(imageFile.getBytes()));

            imageDao.save(image);

            ch.setImage(image);
            chequeDao.save(ch);
        } else {
            throw new EntityNotFoundException("Cheque not found with ID: " + id_cheque);
        }
    }

    public ChequeWithImageResponse getChequeWithImage(Integer id_cheque) {
        Optional<cheque> optionalCheque = chequeDao.findById(id_cheque);

        if (optionalCheque.isPresent()) {
            cheque ch = optionalCheque.get();
            ChequeWithImageResponse response = new ChequeWithImageResponse();
            response.setId_cheque(ch.getId_cheque());
            response.setDate(ch.getDate());
            response.setEtatCheque(ch.getEtatCheque());

            if (ch.getImage() != null) {
                ChequeWithImageResponse.ImageResponse imageResponse = new ChequeWithImageResponse.ImageResponse();
                imageResponse.setName(ch.getImage().getName());
                imageResponse.setType(ch.getImage().getType());
                imageResponse.setImageData(ch.getImage().getImage());

                response.setImage(imageResponse);
            }

            return response;
        } else {
            return null;
        }
    }

    //affichage des cheque eli andhom date = date system
    public List<cheque> getChequesWithSimilarDate() {
        LocalDate systemDate = LocalDate.now();
        return chequeDao.findChequesWithSimilarDate(systemDate);
    }

    //calcul des cheque eli andhom date = date system

    public int countChequesWithSimilarDate() {
        LocalDate systemDate = LocalDate.now();
        return chequeDao.countChequesWithSimilarDate(systemDate);
    }

    //Affichage des nbr de cheque selon leurs etat :
    public int getNumberOfProcessedCheques() {
        return chequeDao.countByEtatCheque(EtatCheque.Traité);
    }

    public int getNumberOfNonProcessedCheques() {
        return chequeDao.countByEtatCheque(EtatCheque.Non_Traité);
    }

    public int getNumberOfRejectedCheques() {
        return chequeDao.countByEtatCheque(EtatCheque.Rejeté);
    }


    public void traiterCheque(Integer idCheque,String causeRejet) {
        try {
            updateChequeEtat(idCheque, EtatCheque.Traité,causeRejet);
            // Mise à jour réussie, vous pouvez effectuer d'autres opérations ici si nécessaire.
        } catch (Exception e) {
            // Gérez l'erreur ici si la mise à jour de l'état du chèque échoue.
            // Vous pouvez enregistrer des erreurs dans les journaux ou lancer des exceptions personnalisées.
            throw new RuntimeException("Erreur lors du traitement du chèque : " + e.getMessage(), e);
        }
    }

    public void rejeterCheque(Integer idCheque,String causeRejet) {
        try {
            updateChequeEtat(idCheque, EtatCheque.Rejeté,causeRejet);
            // Mise à jour réussie, vous pouvez effectuer d'autres opérations ici si nécessaire.
        } catch (Exception e) {
            // Gérez l'erreur ici si la mise à jour de l'état du chèque échoue.
            // Vous pouvez enregistrer des erreurs dans les journaux ou lancer des exceptions personnalisées.
            throw new RuntimeException("Erreur lors du rejet du chèque : " + e.getMessage(), e);
        }
    }

    // Autres méthodes de service...

    // Méthode générique pour mettre à jour l'état du chèque
    public void updateChequeEtat(Integer idCheque, EtatCheque etatCheque, String causeRejet) {
        Optional<cheque> optionalCheque = chequeDao.findById(idCheque);

        if (optionalCheque.isPresent()) {
            cheque ch = optionalCheque.get();
            ch.setEtatCheque(etatCheque);
            ch.setCauseRejet(causeRejet);
            ch.updateEtatCounters(); // Mise à jour des compteurs d'état si nécessaire
            chequeDao.save(ch);
        } else {
            throw new EntityNotFoundException("Chèque non trouvé avec l'ID : " + idCheque);
        }
    }


    //division de cheque :
    @Override
    public Map<String, Integer> divideChequesAmongUsers() {
        int totalCheques = countChequesWithSimilarDate();
        List<User> users = (List<User>) userDao.findAll();
        Map<String, Integer> chequesPerUser = new HashMap<>();

        if (!users.isEmpty()) {
            int chequesPerUserCount = totalCheques / users.size();
            int remainingCheques = totalCheques % users.size();

            for (User user : users) {
                chequesPerUser.put(user.getUserName(), chequesPerUserCount);
            }

            // Distribute remaining cheques among users
            int i = 0;
            for (Map.Entry<String, Integer> entry : chequesPerUser.entrySet()) {
                if (i < remainingCheques) {
                    entry.setValue(entry.getValue() + 1);
                    i++;
                } else {
                    break;
                }
            }
        }

        return chequesPerUser;
    }

    @Override
    public Map<String, Integer> divideChequesAmongUsers1(String username) {
        int totalCheques = countChequesWithSimilarDate();
        List<User> users = (List<User>) userDao.findAll();
        Map<String, Integer> chequesPerUser = new HashMap<>();

        if (!users.isEmpty()) {
            int chequesPerUserCount = totalCheques / users.size();
            int remainingCheques = totalCheques % users.size();

            for (User user : users) {
                if (user.getUserName().equals(username)) {
                    // Si l'utilisateur est connecté, ne récupérez que les chèques qui lui sont assignés
                    chequesPerUser.put(user.getUserName(), chequesPerUserCount + remainingCheques);
                } else {
                    // Pour les autres utilisateurs, répartissez les chèques de manière égale
                    chequesPerUser.put(user.getUserName(), chequesPerUserCount);
                }
            }
        }

        return chequesPerUser;
    }


    public byte[] getChequeImageBytes(Integer id_cheque) {
        Optional<cheque> chequeOptional = chequeDao.findById(id_cheque);

        if (chequeOptional.isPresent()) {
            cheque ch = chequeOptional.get();
            // Assurez-vous que votre entité Cheque possède un champ "imageData" ou similaire pour stocker les données binaires de l'image
            byte[] imageData = ch.getImage().getImage();
            return imageData;
        } else {
            return null; // Gérer le cas où le chèque n'est pas trouvé
        }
    }

    public cheque getChequeById(Integer id_cheque) {
        // Vous pouvez implémenter cette méthode en utilisant le repository ou votre logique d'accès aux données
        Optional<cheque> optionalCheque = chequeDao.findById(id_cheque);

        // Vérifiez si le chèque existe
        if (optionalCheque.isPresent()) {
            return optionalCheque.get();
        } else {
            // Si le chèque n'est pas trouvé, vous pouvez renvoyer null ou générer une exception appropriée
            throw new EntityNotFoundException("Chèque introuvable avec l'identifiant : " + id_cheque);
        }
    }

    public List<cheque> getAllCheques() {
        return chequeDao.findAll(); // Utilisez la méthode appropriée de votre repository pour récupérer tous les chèques
    }

    public int getNumberOfChequesForCurrentUser() {
        // Obtenez le nom d'utilisateur de l'utilisateur connecté
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Appelez la fonction divideChequesAmongUsers1 avec le nom d'utilisateur actuel
        Map<String, Integer> chequesDistribution = divideChequesAmongUsers1(username);

        // Obtenez le nombre de chèques traités pour l'utilisateur connecté (s'il existe)
        Integer numberOfChequesProcessed = chequesDistribution.get(username);

        // Assurez-vous que numberOfChequesProcessed n'est pas nul
        if (numberOfChequesProcessed != null) {
            return numberOfChequesProcessed;
        } else {
            // Si l'utilisateur n'a pas de chèques traités, retournez 0 ou une autre valeur par défaut
            return 0;
        }
    }


    //
    @Override
    public List<cheque> getTraitedChequesWithDetailsForCurrentUser(String username) {
        User currentUser = userDao.findByUsername(username);
        List<cheque> treatedCheques = new ArrayList<>();

        if (currentUser != null) {
            List<User> currentUserList = new ArrayList<>();
            currentUserList.add(currentUser);
            treatedCheques = chequeDao.findAllByUserschequeInAndEtatCheque(currentUserList, EtatCheque.Non_Traité);
        }

        return treatedCheques;
    }

    // Service
    @Override
    public Map<String, List<cheque>> getNonTraitedChequesForUsers(List<String> usernames) {
        Map<String, List<cheque>> nonTraitedChequesByUser = new HashMap<>();

        for (String username : usernames) {
            User currentUser = userDao.findByUsername(username);
            List<cheque> nonTraitedCheques = new ArrayList<>();

            if (currentUser != null) {
                nonTraitedCheques = chequeDao.findAllByUserschequeInAndEtatCheque(
                        Collections.singletonList(currentUser), EtatCheque.Non_Traité);
            }

            nonTraitedChequesByUser.put(username, nonTraitedCheques);
        }

        return nonTraitedChequesByUser;
    }


    @Override
    public void assignerChequeAUtilisateur(String username, Integer chequeId) throws ChequeDejaAttribueException, IllegalArgumentException {
        // Recherchez l'utilisateur et le chèque dans la base de données
        Optional<User> userOptional = Optional.ofNullable(userDao.findByUsername(username));
        Optional<cheque> chequeOptional = chequeDao.findById(chequeId);

        if (userOptional.isPresent() && chequeOptional.isPresent()) {
            User user = userOptional.get();
            cheque ch = chequeOptional.get();

            // Vérifiez si le chèque n'est pas déjà attribué à un utilisateur
            if (ch.getUserscheque().isEmpty()) {
                // Attribuez le chèque à l'utilisateur
                ch.getUserscheque().add(user);
                // Mettez à jour l'état du chèque si nécessaire
                // cheque.setEtatCheque(EtatCheque.Traité);

                // Enregistrez les modifications dans la base de données
                chequeDao.save(ch);
            } else {
                throw new ChequeDejaAttribueException("Le chèque est déjà attribué à un utilisateur.");
            }
        } else {
            throw new IllegalArgumentException("L'utilisateur ou le chèque spécifié n'existe pas.");
        }
    }

    @Override
    public List<cheque> getRejectedChequesForCurrentUser(String username) {
        User currentUser = userDao.findByUsername(username);
        List<cheque> treatedCheques = new ArrayList<>();
        // Mettez en œuvre la logique pour obtenir les chèques rejetés pour l'utilisateur actuel
        // Vous pouvez utiliser le service Cheque pour cela.
        return chequeDao.findAllByUserschequeInAndEtatCheque(
                Collections.singletonList(currentUser), EtatCheque.Rejeté);
    }
@Override
    public String calculerRendement() {
        long nbChequesTraites = chequeDao.countByEtatCheque(EtatCheque.Traité);
        long nbChequesRejetes = chequeDao.countByEtatCheque(EtatCheque.Rejeté);

        if (nbChequesTraites > nbChequesRejetes) {
            return "Fort";
        } else {
            return "Faible";
        }
    }
@Override
    public double calculerPourcentageRendement() {
        long nbChequesTraites = chequeDao.countByEtatCheque(EtatCheque.Traité);
        long nbTotalCheques = chequeDao.count(); // Nombre total de chèques

        if (nbTotalCheques == 0) {
            // Évitez la division par zéro, par exemple, si aucune donnée n'est disponible
            return 0.0;
        }

        double pourcentageRendement = ((double) nbChequesTraites / nbTotalCheques) * 100.0;
        return pourcentageRendement;
    }

    @Override
    public Map<String, Object> calculerPourcentageRendementParUtilisateur() {
        Map<String, Double> rendementParUtilisateur = new HashMap<>();
        User utilisateurLePlusPerformant = null;
        double pourcentageLePlusEleve = -1.0; // Initialisé avec une valeur négative

        // Récupérez la liste des utilisateurs depuis votre base de données
        List<User> utilisateurs = (List<User>) userDao.findAll();

        for (User utilisateur : utilisateurs) {
            long nbChequesTraitesParUtilisateur = chequeDao.countByEtatChequeAndUser(EtatCheque.Traité, utilisateur);
            long nbTotalChequesTraitesParUtilisateur = chequeDao.countByUser(utilisateur);

            if (nbTotalChequesTraitesParUtilisateur == 0) {
                // Évitez la division par zéro, par exemple, si aucune donnée n'est disponible pour cet utilisateur
                rendementParUtilisateur.put(utilisateur.getUserName(), 0.0);
            } else {
                double pourcentageRendementUtilisateur = ((double) nbChequesTraitesParUtilisateur / nbTotalChequesTraitesParUtilisateur) * 100.0;
                rendementParUtilisateur.put(utilisateur.getUserName(), pourcentageRendementUtilisateur);

                // Mettez à jour l'utilisateur le plus performant si nécessaire
                if (pourcentageRendementUtilisateur > pourcentageLePlusEleve) {
                    pourcentageLePlusEleve = pourcentageRendementUtilisateur;
                    utilisateurLePlusPerformant = utilisateur;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pourcentageRendementParUtilisateur", rendementParUtilisateur);
        result.put("utilisateurLePlusPerformant", utilisateurLePlusPerformant);

        return result;
    }




}



