/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package oodj_user_management;

import java.io.*;
import javax.swing.*;
import java.awt.Color;
import java.util.logging.Level;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.WindowConstants;

import oodj_user_management.Dashboard.AO_dashboard;
import oodj_user_management.Dashboard.CA_dashboard;

/**
 *
 * @author User
 */
public class UI_Login extends javax.swing.JFrame {
    String userid;
    String username;
    String passwd;
    String role;
    Float logtimestamp;
    Boolean accstatus;
    Boolean is_login = false;

    private static final String LOG_FILE_NAME = "src/oodj_user_management/login_log.dat";
    private LoginLogEntry currentLog;
    private final IAccountManagement accountHandler = new UM_OOP();

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UI_Login.class.getName());

    public UI_Login() {
        initComponents();
        lblMessage.setForeground(Color.BLACK);
    }

    public boolean authenticate(String userIdInput, String passwordInput) {

        String[] userData = accountHandler.findAccountByID(userIdInput);
        boolean loginSuccess = false;

        if (userData != null && userData.length >= 7) {
            String filePassword = userData[2].trim();
            String fileRole = userData[3].trim();
            String fileStatus = userData[4].trim();

            // Check if the passwords match
            if (filePassword.equals(passwordInput)) {
                // Check if the account is activated
                if (fileStatus.equalsIgnoreCase("Active")) {
                    this.userid = userData[0].trim();
                    this.username = userData[1].trim();
                    this.passwd = filePassword;
                    this.role = fileRole;
                    this.accstatus = true;
                    loginSuccess = true;
                } else {
                    lblMessage.setForeground(Color.RED);
                    lblMessage.setText("Account is inactive, please contact Admin.");
                    this.accstatus = false;
                }
            } else {
                lblMessage.setForeground(Color.RED);
                lblMessage.setText("Invalid username or password.");
            }
        } else {
            lblMessage.setForeground(Color.RED);
            lblMessage.setText("Invalid username or password.");
        }

        if (loginSuccess) {
            is_login = true;

            lblMessage.setForeground(new Color(0, 153, 51)); // Green
            lblMessage.setText("Login Success! Welcome, " + username + " (" + this.role + ").");

            this.currentLog = new LoginLogEntry(this.userid, LocalDateTime.now());
            // Add new log entries to the .dat file
            writeLogToFile(this.currentLog);

            if ("CourseAdmin".equalsIgnoreCase(this.role)) {
                CA_dashboard caDashboard = new CA_dashboard(this);

                caDashboard.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                caDashboard.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        int response = JOptionPane.showConfirmDialog(windowEvent.getWindow(), 
                                                                     "Are you sure you want to logout?", 
                                                                     "Confirm Logout", 
                                                                     JOptionPane.YES_NO_OPTION, 
                                                                     JOptionPane.QUESTION_MESSAGE);
                        
                        if (response == JOptionPane.YES_OPTION) {
                            logout(currentLog);
                            caDashboard.dispose();
                            UI_Login.this.setVisible(true);
                            UI_Login.this.clearFields(); // Clear fields after showing login frame
                        } else {
                            // Do nothing, keep the dashboard open
                        }
                    }
                });

                caDashboard.setVisible(true);
                this.setVisible(false);
            } else if ("AcademicOfficer".equalsIgnoreCase(this.role)) {
                AO_dashboard aoDashboard = new AO_dashboard(this, this.currentLog);

                aoDashboard.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                aoDashboard.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        int response = JOptionPane.showConfirmDialog(windowEvent.getWindow(), 
                                                                     "Are you sure you want to logout?", 
                                                                     "Confirm Logout", 
                                                                     JOptionPane.YES_NO_OPTION, 
                                                                     JOptionPane.QUESTION_MESSAGE);
                        
                        if (response == JOptionPane.YES_OPTION) {
                            logout(currentLog);
                            aoDashboard.dispose();
                            UI_Login.this.setVisible(true);
                            UI_Login.this.clearFields(); // Clear fields after showing login frame
                        } else {
                            // Do nothing, keep the dashboard open
                        }
                    }
                });

                aoDashboard.setVisible(true);
                this.setVisible(false);

            } else {
                JOptionPane.showMessageDialog(this,
                        "Unknown role: " + this.role,
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            return true;
        } else {

            is_login = false;
            return false;
        }
    }

    public List<LoginLogEntry> readAllLogs() {
        List<LoginLogEntry> logs = new ArrayList<>();

        File file = new File(LOG_FILE_NAME);

        // Checks if the file exists and is not empty. If the file does not exist or has
        // zero length, returns an empty list.
        if (!file.exists() || file.length() == 0) {
            if (file.exists() && file.length() == 0) {
                logger.log(Level.INFO, "Log file exists but is empty. Returning empty list.");
            } else {
                logger.log(Level.INFO, "Log file not found. Creating a new log file path: " + file.getAbsolutePath());
            }
            return logs;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            while (true) {
                try {
                    logs.add((LoginLogEntry) ois.readObject());
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Fatal Error: Log file may be corrupted or inaccessible. Path: " + file.getAbsolutePath(), e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Fatal Error: Log file contains unknown object types.", e);
        }

        return logs;
    }

    // write all log entries to a file (with overwriting)
    private void writeAllLogs(List<LoginLogEntry> logs) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LOG_FILE_NAME))) {
            for (LoginLogEntry log : logs) {
                oos.writeObject(log);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing log file: " + LOG_FILE_NAME, e);
        }
    }

    private void writeLogToFile(LoginLogEntry newLog) {
        List<LoginLogEntry> logs = readAllLogs();

        logs.add(newLog);

        writeAllLogs(logs);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        lblMessage = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPasswordField1 = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("UserID:");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Password:");

        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lblMessage.setText("Welcome to Admin system");

        jLabel3.setText("Login Page");

        jButton2.setText("Forget Password");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(170, 170, 170)
                                                .addComponent(jButton2)
                                                .addGap(71, 71, 71)
                                                .addComponent(jButton1))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(248, 248, 248)
                                                .addComponent(jLabel3))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(198, 198, 198)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(lblMessage,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 281,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jTextField1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 125,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jPasswordField1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 125,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(115, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addGap(32, 32, 32)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(lblMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2))
                                .addGap(34, 34, 34)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

        String username = jTextField1.getText().trim();
        String password = new String(jPasswordField1.getPassword());

        // Clear previous messages
        lblMessage.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            return;
        }

        authenticate(username, password);
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField1ActionPerformed
        // userid input
        jButton1.doClick();
    }// GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Forget_passwd fpDashboard = new Forget_passwd();
        fpDashboard.setVisible(true);
        this.dispose();
    }// GEN-LAST:event_jButton2ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
        jButton1.doClick();
    }// GEN-LAST:event_jPasswordField1ActionPerformed

    public void logout(LoginLogEntry logToUpdate) {
        is_login = false;

        // Set logout time
        logToUpdate.setLogoutTime(LocalDateTime.now());

        // read log
        List<LoginLogEntry> logs = readAllLogs();

        //
        for (int i = 0; i < logs.size(); i++) {
            LoginLogEntry log = logs.get(i);
            if (log.getUserId().equals(logToUpdate.getUserId()) &&
                    log.getLoginTime().equals(logToUpdate.getLoginTime())) {

                logs.set(i, logToUpdate);
                break;
            }
        }

        writeAllLogs(logs);

        logger.info("User " + logToUpdate.getUserId() + " logged out at " + logToUpdate.getLogoutTime());
    }

    public Boolean getAccstatus() {
        return accstatus;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void clearFields() {
        jTextField1.setText("");
        jPasswordField1.setText("");
        lblMessage.setText("Welcome to Admin system");
        lblMessage.setForeground(Color.BLACK);
    }

    public void confirmAndLogout(javax.swing.JFrame dashboardFrame, LoginLogEntry logEntry) {
        int response = JOptionPane.showConfirmDialog(dashboardFrame, 
                                                     "Are you sure you want to logout?", 
                                                     "Confirm Logout", 
                                                     JOptionPane.YES_NO_OPTION, 
                                                     JOptionPane.QUESTION_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {
            if (logEntry != null) {
                logout(logEntry);
            }
            setVisible(true);
            clearFields();
            dashboardFrame.dispose();
        } else {
            // Do nothing, keep the dashboard open
        }
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblMessage;
    // End of variables declaration//GEN-END:variables
}
