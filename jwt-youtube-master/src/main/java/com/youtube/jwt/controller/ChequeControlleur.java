package com.youtube.jwt.controller;

import com.youtube.jwt.dao.ChequeDao;
import com.youtube.jwt.entity.EtatCheque;
import com.youtube.jwt.entity.User;
import com.youtube.jwt.entity.cheque;
import com.youtube.jwt.service.ChequeDejaAttribueException;
import com.youtube.jwt.service.ChequeService;
import com.youtube.jwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cheque")
@RequiredArgsConstructor
public class ChequeControlleur {

    private final ChequeService chequeService ;
    private final ChequeDao chequeDao ;


    //Ajout d'un cheque
    @PostMapping("/addcheque")
    public cheque addcheque(@RequestBody cheque ch) {
        ch.setEtatCheque(EtatCheque.Non_Traité);
        return chequeService.addcheque(ch);
    }


    //attribuer image a un cheque pour faciliter traitement des cheques

    @PostMapping("/assign-image-to-cheque/{id}")
    public ResponseEntity<String> assignImageToCheque(
            @PathVariable("id") Integer id_cheque,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            chequeService.assignImageToCheque(id_cheque, imageFile);
            return ResponseEntity.ok("Image assigned to cheque successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to assign image to cheque.");
        }
    }

    @GetMapping("/with-image/{id}")
    public ResponseEntity<ChequeWithImageResponse> getChequeWithImage(@PathVariable("id") Integer id_cheque) {
        try {
            ChequeWithImageResponse response = new ChequeWithImageResponse();

            // Obtenez les informations du chèque à partir de votre service
            cheque ch = chequeService.getChequeById(id_cheque);

            // Vérifiez si le chèque existe
            if (ch != null) {
                response.setId_cheque(ch.getId_cheque());
                response.setDate(ch.getDate());
                response.setEtatCheque(ch.getEtatCheque());

                // Obtenez les données de l'image du chèque à partir de votre service
                byte[] imageBytes = chequeService.getChequeImageBytes(id_cheque);

                // Vérifiez si des données d'image existent
                if (imageBytes != null) {
                    ChequeWithImageResponse.ImageResponse imageResponse = new ChequeWithImageResponse.ImageResponse();
                    imageResponse.setType("image/jpeg"); // Remplacez par le type MIME approprié de votre image
                    imageResponse.setImageData(imageBytes);
                    response.setImage(imageResponse);
                }

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }





//Fonction qui permet d'afficher les cheque a traiter chaque jours

    @GetMapping("/cheques-with-similar-date")
    public ResponseEntity<List<cheque>> getChequesWithSimilarDate() {
        List<cheque> cheques = chequeService.getChequesWithSimilarDate();
        return ResponseEntity.ok(cheques);
    }

    //calcul des cheque eli andhom date = date system
    @GetMapping("/count-cheques-with-similar-date")
    public ResponseEntity<String> countChequesWithSimilarDate() {
        int count = chequeService.countChequesWithSimilarDate();
        String responseMsg = "Le nombre de chèques à traiter aujourd'hui est : " + count;
        return ResponseEntity.ok(responseMsg);
    }

    //Fonction qui affiche les nombre de cheque traité  ,  les nombre de cheque Non traité et les nbr de cheque rejeté
    @GetMapping("/display-cheque-counts")
    public ResponseEntity<String> displayChequeCounts() {
        try {
            int processedCheques = chequeService.getNumberOfProcessedCheques();
            int nonProcessedCheques = chequeService.getNumberOfNonProcessedCheques();
            int rejectedCheques = chequeService.getNumberOfRejectedCheques();

            String response = "Nombre de chèques traités : " + processedCheques + "\n" +
                    "Nombre de chèques non traités : " + nonProcessedCheques + "\n" +
                    "Nombre de chèques rejetés : " + rejectedCheques;

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'affichage des informations de chèque");
        }
    }

    @GetMapping("/get-number-of-processed-cheques")
    public ResponseEntity<Integer> getNumberOfProcessedCheques() {
        int processedCheques = chequeService.getNumberOfProcessedCheques();
        return ResponseEntity.ok(processedCheques);
    }

    @GetMapping("/get-number-of-non-processed-cheques")
    public ResponseEntity<Integer> getNumberOfNonProcessedCheques() {
        int nonProcessedCheques = chequeService.getNumberOfNonProcessedCheques();
        return ResponseEntity.ok(nonProcessedCheques);
    }

    @GetMapping("/get-number-of-rejected-cheques")
    public ResponseEntity<Integer> getNumberOfRejectedCheques() {
        int rejectedCheques = chequeService.getNumberOfRejectedCheques();
        return ResponseEntity.ok(rejectedCheques);
    }

    //update cheque
    @PutMapping("/{id_cheque}/etat")
    public ResponseEntity<String> updateChequeEtat(
            @PathVariable("id_cheque") Integer id_cheque,
            @RequestParam("etatCheque") EtatCheque etatCheque,
            @RequestParam(value = "causeRejet", required = false) String causeRejet
    ) {
        try {
            // Convertissez la chaîne 'etatCheque' en Enum ou utilisez-la directement selon votre besoin.
            chequeService.updateChequeEtat(id_cheque, EtatCheque.valueOf(String.valueOf(etatCheque)), causeRejet);
            return ResponseEntity.ok("L'état du chèque a été mis à jour avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la mise à jour de l'état du chèque.");
        }
    }



    @GetMapping("/divide")
    public ResponseEntity<Map<String, Integer>> divideChequesAmongUsers() {
        Map<String, Integer> chequesPerUser = chequeService.divideChequesAmongUsers();
        return ResponseEntity.ok(chequesPerUser);
    }
    @GetMapping("/all")
    public List<cheque> getAllCheques() {
        return chequeService.getAllCheques();
    }

    @GetMapping("/cheques/{username}")
    public ResponseEntity<Map<String, Integer>> divideChequesAmongUsers1(@PathVariable String username) {
        Map<String, Integer> chequesParUtilisateur = chequeService.divideChequesAmongUsers1(username);
        return ResponseEntity.ok(chequesParUtilisateur);
    }

    @GetMapping("/cheques/counts")
    public ResponseEntity<Integer> getNumberOfChequesForCurrentUser() {
        int numberOfCheques = chequeService.getNumberOfChequesForCurrentUser();
        return ResponseEntity.ok(numberOfCheques);
    }

    // y5dmou
   /* @GetMapping("/traited")
    public ResponseEntity<List<cheque>> getTraitedChequesForCurrentUser(@RequestParam String username) {
        List<cheque> traitedCheques = chequeService.getTraitedChequesWithDetailsForCurrentUser(username);
        return new ResponseEntity<>(traitedCheques, HttpStatus.OK);
    }*/

    @GetMapping("/traited")
    public ResponseEntity<Map<String, List<cheque>>> getTraitedChequesForCurrentUser(@RequestParam String username) {
        Map<String, List<cheque>> traitedChequesByUser = new HashMap<>();
        List<cheque> traitedCheques = chequeService.getTraitedChequesWithDetailsForCurrentUser(username);

        // Ajoutez la liste de chèques traités sous le nom d'utilisateur dans la map
        traitedChequesByUser.put(username, traitedCheques);

        if (traitedCheques.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(traitedChequesByUser);
        }
    }



    @GetMapping("/nontraitedcheques")
    public ResponseEntity<Map<String, List<cheque>>> getNonTraitedChequesForUsers(@RequestParam List<String> usernames) {
        Map<String, List<cheque>> nonTraitedChequesByUser = chequeService.getNonTraitedChequesForUsers(usernames);

        if (nonTraitedChequesByUser.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(nonTraitedChequesByUser);
        }
    }
@PostMapping("/assignerChequeAUtilisateur")
public ResponseEntity<String> assignerChequeAUtilisateur(
        @RequestParam String username,
        @RequestParam Integer chequeId) {
    try {
        chequeService.assignerChequeAUtilisateur(username, chequeId);
        return ResponseEntity.ok("Chèque attribué avec succès à l'utilisateur.");
    } catch (ChequeDejaAttribueException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le chèque est déjà attribué à un utilisateur.");
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'utilisateur ou le chèque spécifié n'existe pas.");
    }
}

    @GetMapping("/currentuser")
    public ResponseEntity<List<cheque>> getRejectedChequesForCurrentUser(@RequestParam String username) {
        List<cheque> rejectedCheques = chequeService.getRejectedChequesForCurrentUser(username);
        return ResponseEntity.ok(rejectedCheques);
    }
    @GetMapping("/calculer-rendement")
    public ResponseEntity<String> calculerRendement() {
        String rendement = chequeService.calculerRendement();
        return ResponseEntity.ok(rendement);
    }
    @GetMapping("/calculer-pourcentage-rendement")
    public ResponseEntity<Double> calculerPourcentageRendement() {
        long nbChequesTraites = chequeDao.countByEtatCheque(EtatCheque.Traité);
        long nbTotalCheques = chequeDao.count(); // Nombre total de chèques

        if (nbTotalCheques == 0) {
            // Évitez la division par zéro, par exemple, si aucune donnée n'est disponible
            return ResponseEntity.ok(0.0);
        }

        double pourcentageRendement = ((double) nbChequesTraites / nbTotalCheques) * 100.0;
        return ResponseEntity.ok(pourcentageRendement);
    }

    @GetMapping("/calculer-pourcentage-rendement-par-utilisateur")
    public ResponseEntity<Map<String, Object>> calculerPourcentageRendementParUtilisateur() {
        Map<String, Object> result = chequeService.calculerPourcentageRendementParUtilisateur();
        Map<String, Double> rendementParUtilisateur = (Map<String, Double>) result.get("pourcentageRendementParUtilisateur");
        User utilisateurLePlusPerformant = (User) result.get("utilisateurLePlusPerformant");

        // Find the user with the highest performance
        String userLePlusPerfermant = "";
        double performanceMax = -1.0;

        for (Map.Entry<String, Double> entry : rendementParUtilisateur.entrySet()) {
            if (entry.getValue() > performanceMax) {
                performanceMax = entry.getValue();
                userLePlusPerfermant = entry.getKey();
            }
        }

        String message = "Le user le plus performant est : " + userLePlusPerfermant;

        Map<String, Object> response = new HashMap<>();
        response.put("rendementParUtilisateur", rendementParUtilisateur);
        response.put("utilisateurLePlusPerformant", utilisateurLePlusPerformant);
        response.put("message", message);

        return ResponseEntity.ok(response);
    }


}








