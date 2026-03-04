/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oodj_user_management;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UM_OOP implements IAccountManagement {
    private static final String FILE_PATH = "src/oodj_user_management/acc_info.txt";
    private static final Logger logger = Logger.getLogger(UM_OOP.class.getName());
    

    private static final int FIELD_COUNT = 7;
    
    public List<String[]> readAllAccounts() {
        List<String[]> accounts = new ArrayList<>();

        Path path = Paths.get(FILE_PATH); 
        
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true; 
            
            while ((line = br.readLine()) != null) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue; 
                }
                
                if (isFirstLine) {
                    isFirstLine = false;
                    if (trimmedLine.toLowerCase().startsWith("userid")) {
                         continue; 
                    }
                }
                
                String[] parts = trimmedLine.split(",");
                
                if (parts.length >= FIELD_COUNT) {
                    accounts.add(parts);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading account file at: " + FILE_PATH, e);
        }
        return accounts;
    }
    
    public String[] findAccountByID(String userId) {
        for (String[] account : readAllAccounts()) {
            if (account[0].trim().equalsIgnoreCase(userId.trim())) {
                return account;
            }
        }
        return null;
    }
    
    public boolean appendNewAccount(String userId, String username, String password, String role, String question, String answer) {
        String status = "Active"; // New account default status
        String newLine = String.format("%s,%s,%s,%s,%s,%s,%s", 
            userId, username, password, role, status, question, answer);
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            File file = new File(FILE_PATH);
            
            // add space
            if (file.exists() && file.length() > 0) {
                 pw.println(); 
            }
            
            pw.print(newLine);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error appending new user to file: " + FILE_PATH, e);
            return false;
        }
    }


    public boolean updateAccount(String[] updatedAccount) {
        if (updatedAccount.length < FIELD_COUNT) {
             logger.log(Level.WARNING, "Attempted to update account with insufficient fields.");
             return false;
        }
        
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            String targetUserId = updatedAccount[0].trim();
            boolean userFound = false;
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",");
                

                if (parts.length >= FIELD_COUNT && parts[0].trim().equalsIgnoreCase(targetUserId)) {
                    
                    //formating
                    String newLine = String.format("%s,%s,%s,%s,%s,%s,%s", 
                                updatedAccount[0].trim(), 
                                updatedAccount[1].trim(), 
                                updatedAccount[2].trim(), // New Password or Old Password
                                updatedAccount[3].trim(), 
                                updatedAccount[4].trim(), // New Status or Old Status
                                updatedAccount[5].trim(), 
                                updatedAccount[6].trim());
                    
                    lines.set(i, newLine);
                    userFound = true;
                    break;
                }
            }

            if (userFound) {
                Files.write(path, lines);
                return true;
            } else {
                return false; 
            }
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing updated user data to file: " + FILE_PATH, e);
            return false;
        }
    }
}
