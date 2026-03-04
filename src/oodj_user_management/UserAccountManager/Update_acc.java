package oodj_user_management.UserAccountManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import java.io.InputStream;

import oodj_user_management.UM_OOP;
import oodj_user_management.UserAccountManager.UI_AccManagement;

/**
 *
 * @author User
 */
public class Update_acc extends javax.swing.JFrame {

        private final UM_OOP accountHandler = new UM_OOP();

        private static final java.util.logging.Logger logger = java.util.logging.Logger
                        .getLogger(Update_acc.class.getName());

        private UI_AccManagement parentUI;

        public Update_acc() {
                initComponents();
                initializeCustomComponents();
        }

        public Update_acc(UI_AccManagement parentUI) {
                this();
                this.parentUI = parentUI;
        }

        private void initializeCustomComponents() {
                clearAccInfoDisplay();
        }

        private void clearAccInfoDisplay() {
                jLabel4.setText("ID"); // UserID
                jLabel6.setText("Name"); // Username
                jLabel14.setText("Password"); // Password
                jLabel15.setText("Role"); // Role
                jLabel16.setText("Status"); // Status
                jLabel18.setText("Question"); // Question
                jLabel20.setText("Answer"); // Answer
                jTextField2.setText(""); // Old Password
                jTextField3.setText(""); // New Password
                jTextField4.setText(""); // Question Input
                jTextField5.setText(""); // Answer Input
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated
        // Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                jScrollPane1 = new javax.swing.JScrollPane();
                jTextArea1 = new javax.swing.JTextArea();
                jLabel12 = new javax.swing.JLabel();
                jSeparator1 = new javax.swing.JSeparator();
                jButton1 = new javax.swing.JButton();
                jLabel1 = new javax.swing.JLabel();
                jTextField1 = new javax.swing.JTextField();
                jPanel1 = new javax.swing.JPanel();
                jLabel3 = new javax.swing.JLabel();
                jLabel2 = new javax.swing.JLabel();
                jLabel4 = new javax.swing.JLabel();
                jLabel5 = new javax.swing.JLabel();
                jLabel6 = new javax.swing.JLabel();
                jLabel10 = new javax.swing.JLabel();
                jLabel11 = new javax.swing.JLabel();
                jLabel13 = new javax.swing.JLabel();
                jLabel14 = new javax.swing.JLabel();
                jLabel15 = new javax.swing.JLabel();
                jLabel16 = new javax.swing.JLabel();
                jLabel17 = new javax.swing.JLabel();
                jLabel18 = new javax.swing.JLabel();
                jLabel19 = new javax.swing.JLabel();
                jLabel20 = new javax.swing.JLabel();
                jButton2 = new javax.swing.JButton();
                jLabel7 = new javax.swing.JLabel();
                jTextField2 = new javax.swing.JTextField();
                jLabel8 = new javax.swing.JLabel();
                jTextField3 = new javax.swing.JTextField();
                jButton3 = new javax.swing.JButton();
                jLabel21 = new javax.swing.JLabel();
                jTextField4 = new javax.swing.JTextField();
                jLabel22 = new javax.swing.JLabel();
                jTextField5 = new javax.swing.JTextField();

                jTextArea1.setColumns(20);
                jTextArea1.setRows(5);
                jScrollPane1.setViewportView(jTextArea1);

                jLabel12.setText("jLabel12");

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

                jButton1.setText("Back");
                jButton1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton1ActionPerformed(evt);
                        }
                });

                jLabel1.setText("UserID:");

                jTextField1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jTextField1ActionPerformed(evt);
                        }
                });

                jPanel1.setBackground(new java.awt.Color(255, 255, 255));

                jLabel3.setText("Account Info:");

                jLabel2.setText("UserID:");

                jLabel4.setText("ID");

                jLabel5.setText("Password:");

                jLabel6.setText("Name");

                jLabel10.setText("Role:");

                jLabel11.setText("Username:");

                jLabel13.setText("Status:");

                jLabel14.setText("Password");

                jLabel15.setText("Role");

                jLabel16.setText("Status");

                jLabel17.setText("Question:");

                jLabel18.setText("Question");

                jLabel19.setText("Answer:");

                jLabel20.setText("Answer");

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(jLabel5)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(jLabel14,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                145,
                                                                                                                Short.MAX_VALUE))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                false)
                                                                                                                .addComponent(jLabel19,
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                .addComponent(jLabel17,
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE))
                                                                                                .addGap(18, 18, 18)
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(jLabel18,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                .addComponent(jLabel20,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)))
                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                jPanel1Layout.createSequentialGroup()
                                                                                                                .addComponent(jLabel10,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                43,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addGap(28, 28, 28)
                                                                                                                .addComponent(jLabel15,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                false)
                                                                                                                .addComponent(jLabel3,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                .addComponent(jLabel2,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE))
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(jLabel4,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(jLabel11)
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(jLabel6,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(jLabel13,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                43,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(28, 28, 28)
                                                                                                .addComponent(jLabel16,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)))
                                                                .addContainerGap()));
                jPanel1Layout.setVerticalGroup(
                                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(jLabel3)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel2)
                                                                                .addComponent(jLabel4))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel11)
                                                                                .addComponent(jLabel6))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel5)
                                                                                .addComponent(jLabel14))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel10)
                                                                                .addComponent(jLabel15))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel13)
                                                                                .addComponent(jLabel16))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel17)
                                                                                .addComponent(jLabel18))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel19)
                                                                                .addComponent(jLabel20))
                                                                .addContainerGap(14, Short.MAX_VALUE)));

                jButton2.setText("Check");
                jButton2.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton2ActionPerformed(evt);
                        }
                });

                jLabel7.setText("Old Password:");

                jLabel8.setText("New Password:");

                jButton3.setText("Update");
                jButton3.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jButton3ActionPerformed(evt);
                        }
                });

                jLabel21.setText("Question:");

                jTextField4.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jTextField4ActionPerformed(evt);
                        }
                });

                jLabel22.setText("Answer:");

                jTextField5.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                jTextField5ActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jButton1)
                                                                .addGap(32, 32, 32)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                false)
                                                                                .addComponent(jTextField1,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                71,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLabel1,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                47,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLabel7)
                                                                                .addComponent(jLabel8)
                                                                                .addComponent(jLabel21)
                                                                                .addComponent(jLabel22,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                43,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jTextField4,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                135,
                                                                                                Short.MAX_VALUE)
                                                                                .addComponent(jTextField5)
                                                                                .addComponent(jTextField3)
                                                                                .addComponent(jTextField2))
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addGap(27, 27, 27)
                                                                                                .addComponent(jPanel1,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                .addContainerGap())
                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                layout
                                                                                                                .createSequentialGroup()
                                                                                                                .addPreferredGap(
                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                .addGroup(layout
                                                                                                                                .createParallelGroup(
                                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                                .addComponent(jButton3)
                                                                                                                                .addComponent(jButton2))
                                                                                                                .addGap(93, 93, 93)))));
                layout.setVerticalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout
                                                                .createSequentialGroup()
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                false)
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addComponent(jPanel1,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                .addGap(18, 18, 18))
                                                                                .addGroup(layout.createSequentialGroup()
                                                                                                .addComponent(jLabel1)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(jTextField1,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                .addComponent(jLabel7)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(jTextField2,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                .addComponent(jLabel8)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(jTextField3,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                .addComponent(jLabel21)
                                                                                                .addGap(4, 4, 4)))
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(jTextField4,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jButton2))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel22)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(layout.createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(jTextField5,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(jButton3))
                                                                                .addComponent(jButton1))
                                                                .addContainerGap(16, Short.MAX_VALUE)));

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

                if (parentUI != null) {
                        parentUI.setVisible(true);
                }
                this.dispose();
        }// GEN-LAST:event_jButton1ActionPerformed

        private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField1ActionPerformed
                // userID
                jButton2.doClick();
        }// GEN-LAST:event_jTextField1ActionPerformed

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
                // Enter the user information in the txt file from userID and then view the user
                // information such as userID, username, password, role, status displayed in
                String inputUserID = jTextField1.getText().trim();
                clearAccInfoDisplay();

                if (inputUserID.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter a UserID to check.", "Input Required",
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                }

                String[] userData = accountHandler.findAccountByID(inputUserID);

                if (userData != null && userData.length >= 7) {
                        // show user info at right panel
                        String userID = userData[0].trim();
                        String username = userData[1].trim();
                        String password = userData[2].trim();
                        String role = userData[3].trim();
                        String status = userData[4].trim();
                        String question = userData[5].trim();
                        String answer = userData[6].trim();

                        jLabel4.setText(userID);
                        jLabel6.setText(username);
                        jLabel14.setText(password);
                        jLabel15.setText(role);
                        jLabel16.setText(status);
                        jLabel18.setText(question);
                        jLabel20.setText(answer);
                        jTextField4.setText(question);
                        jTextField5.setText(answer);

                        JOptionPane.showMessageDialog(this, "User data loaded successfully.", "Success",
                                        JOptionPane.INFORMATION_MESSAGE);

                } else {
                        JOptionPane.showMessageDialog(this,
                                        "UserID '" + inputUserID + "' not found.",
                                        "User Not Found", JOptionPane.WARNING_MESSAGE);
                }
        }// GEN-LAST:event_jButton2ActionPerformed

        private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
                // Update button. after user click pop out lbMessage show new account info to do
                // confirmation then edit selected user info in txt file
                String targetUserId = jTextField1.getText().trim();

                if (targetUserId.isEmpty() || jLabel4.getText().equals("id")) {
                        JOptionPane.showMessageDialog(this, "Please check a UserID first.", "Update Error",
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                }

                String oldPasswordInput = jTextField2.getText();
                String newPasswordInput = jTextField3.getText();
                String newQuestionInput = jTextField4.getText().trim();
                String newAnswerInput = jTextField5.getText().trim();

                String displayedUsername = jLabel6.getText();
                String currentPassword = jLabel14.getText();
                String currentStatus = jLabel16.getText();

                if (!oldPasswordInput.equals(currentPassword)) {
                        JOptionPane.showMessageDialog(this, "Incorrect Old Password.", "Update Error",
                                        JOptionPane.ERROR_MESSAGE);
                        return;
                }

                String finalPassword = newPasswordInput.isEmpty() ? currentPassword : newPasswordInput;

                if (newQuestionInput.isEmpty() || newAnswerInput.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Question and Answer fields cannot be empty.",
                                        "Input Error",
                                        JOptionPane.WARNING_MESSAGE);
                        return;
                }

                String message = String.format(
                                "Confirm update for UserID: %s?\n" +
                                                "New Password: %s\n" +
                                                "New Question: %s",
                                targetUserId,
                                finalPassword.equals(currentPassword) ? "Unchanged" : "Changed",
                                newQuestionInput);

                int confirmation = JOptionPane.showConfirmDialog(this,
                                message, "Confirm Update", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {

                        String[] updatedData = accountHandler.findAccountByID(targetUserId);

                        if (updatedData == null || updatedData.length < 7) {
                                JOptionPane.showMessageDialog(this,
                                                "System Error: Failed to retrieve user data for update.",
                                                "System Error", JOptionPane.ERROR_MESSAGE);
                                return;
                        }

                        updatedData[1] = displayedUsername; // Username
                        updatedData[2] = finalPassword; // Password
                        updatedData[5] = newQuestionInput; // Question
                        updatedData[6] = newAnswerInput; // Answer

                        if (accountHandler.updateAccount(updatedData)) {
                                JOptionPane.showMessageDialog(this, "Account (Password/Question) updated successfully!",
                                                "Success",
                                                JOptionPane.INFORMATION_MESSAGE);
                                jButton2.doClick();
                        } else {
                                JOptionPane.showMessageDialog(this,
                                                "Error: Failed to update account due to a system error.",
                                                "System Error", JOptionPane.ERROR_MESSAGE);
                        }
                }

        }// GEN-LAST:event_jButton3ActionPerformed

        private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField4ActionPerformed
                // question
        }// GEN-LAST:event_jTextField4ActionPerformed

        private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField5ActionPerformed
                // answer
        }// GEN-LAST:event_jTextField5ActionPerformed

        /**
         * @param args the command line arguments
         */
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
                        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                                        .getInstalledLookAndFeels()) {
                                if ("Nimbus".equals(info.getName())) {
                                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                                        break;
                                }
                        }
                } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
                        logger.log(java.util.logging.Level.SEVERE, null, ex);
                }
                // </editor-fold>

                /* Create and display the form */
                // java.awt.EventQueue.invokeLater(() -> new Update_acc().setVisible(true));
        }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JButton jButton3;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel12;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel14;
        private javax.swing.JLabel jLabel15;
        private javax.swing.JLabel jLabel16;
        private javax.swing.JLabel jLabel17;
        private javax.swing.JLabel jLabel18;
        private javax.swing.JLabel jLabel19;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel20;
        private javax.swing.JLabel jLabel21;
        private javax.swing.JLabel jLabel22;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSeparator jSeparator1;
        private javax.swing.JTextArea jTextArea1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JTextField jTextField3;
        private javax.swing.JTextField jTextField4;
        private javax.swing.JTextField jTextField5;
        // End of variables declaration//GEN-END:variables
}
