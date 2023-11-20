package com.youtube.jwt.service;

import com.youtube.jwt.dao.ChequeDao;
import com.youtube.jwt.dao.RoleDao;
import com.youtube.jwt.dao.UserDao;
import com.youtube.jwt.entity.Role;
import com.youtube.jwt.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private   ChequeDao chequeDao ;

    public void initRoleAndUser() {

        Role adminRole = new Role();
        adminRole.setRoleName("Admin");
        adminRole.setRoleDescription("Admin role");
        roleDao.save(adminRole);

        Role userRole = new Role();
        userRole.setRoleName("User");
        userRole.setRoleDescription("Default role for newly created record");
        roleDao.save(userRole);

        User adminUser = new User();
        adminUser.setUserName("admin123");
        adminUser.setUserPassword(getEncodedPassword("admin@pass"));
        adminUser.setUserFirstName("admin");
        adminUser.setUserLastName("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRole(adminRoles);
        userDao.save(adminUser);

//        User user = new User();
//        user.setUserName("raj123");
//        user.setUserPassword(getEncodedPassword("raj@123"));
//        user.setUserFirstName("raj");
//        user.setUserLastName("sharma");
//        Set<Role> userRoles = new HashSet<>();
//        userRoles.add(userRole);
//        user.setRole(userRoles);
//        userDao.save(user);
    }


    // Méthode pour charger les matricules à partir du fichier CSV
    private List<String> loadMatriculesFromCsv(String csvFilePath) {
        List<String> matricules = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    String matricule = parts[0].trim();
                    matricules.add(matricule);
                    System.out.println("Matricule added: " + matricule); // Log the matricule added
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return matricules;
    }




    public boolean checkMatriculeIsValid(String matricule) {
        if (matricule != null && !matricule.isEmpty()) {
            return matricule.length() == 8;
        }
        return false;
    }
    public boolean checkMatriculeExistsInCsv(String matricule) {
        List<String> matriculesFromCsv = loadMatriculesFromCsv("C:/Users/eyama/OneDrive/Bureau/DatasetAttijari/dataset.csv");
        if (matriculesFromCsv.contains(matricule)) {
            return true; // Retourner true si le matricule est trouvé dans le fichier CSV
        }
        return false; // Retourner false si le matricule n'est pas trouvé
    }

    public void registerNewUser(User user) {
        if (checkMatriculeIsValid(user.getMatricule()) && !checkMatriculeExistsInCsv(user.getMatricule())) {
            Role role = roleDao.findById("User").orElseThrow(() -> new RuntimeException("Role 'User' non trouvé."));
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(role);

            user.setRole(userRoles);
            user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));

            userDao.save(user);
        } else {
            if (checkMatriculeExistsInCsv(user.getMatricule())) {
                throw new RuntimeException("Le matricule existe déjà. Compte non créé.");
            } else {
                throw new RuntimeException("Le matricule n'existe pas dans le fichier CSV. Compte non créé.");
            }
        }
    }



    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
