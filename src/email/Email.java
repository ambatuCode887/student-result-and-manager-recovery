/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package email;

import java.awt.HeadlessException;
import java.io.File;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

// Import your new architecture components
import model.Student;
import repository.Config;
import repository.TextDataRepository;
import repository.DataRepository;
/**
 *
 * @author User
 */
public class Email extends javax.swing.JFrame {

    // --- Configuration ---
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String USERNAME = "charlshau03@gmail.com";
    private static final String APPPASS = "vkls fjdt ubax osdx";
    private File[] selectedPDFFiles;
    private javax.swing.JFrame parentDashboard;
    private final DataRepository repository;
    private List<Student> allStudents;
    private Map<String, Student> studentIdMap;
    private List<Student> currentIntakeStudents;
    private Map<File, Student> manualPdfStudentMap;

    public Email() {
        initComponents();
        
        //Initialize Repository
        this.repository = new TextDataRepository(
            Config.STUDENTS_FILE, 
            Config.MODULES_FILE, 
            Config.GRADES_FILE, 
            Config.ENROLMENT_FILE,
            Config.COURSES_FILE
        );
        
        studentIdMap = new HashMap<>();
        currentIntakeStudents = new ArrayList<>();
        manualPdfStudentMap = new HashMap<>();
        
        //Load Data using the Repository
        loadDataAndPopulateIntakes();
        listEmail.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    
    public void setParentDashboard(javax.swing.JFrame parent) {
        this.parentDashboard = parent;
    }
         //loads student data from repository and populate intake drop-down list
    private void loadDataAndPopulateIntakes() {
        try {
            allStudents = repository.loadStudents();
            
            if (allStudents == null) {
                JOptionPane.showMessageDialog(this, "No student data loaded.");
                return;
            }
            
            for (Student s : allStudents) {
                if (s != null && s.getStudentId() != null) {
                    studentIdMap.put(s.getStudentId(), s);
                }
            }
            
            Set<String> intakeSet = new HashSet<>();
            for (Student s : allStudents) {
                if (s != null && s.getIntake() != null && !s.getIntake().isEmpty()) {
                    intakeSet.add(s.getIntake());
                }
            }
            
            List<String> intakeList = new ArrayList<>(intakeSet);
            Collections.sort(intakeList);
            intakeList.add(0, "Select Intake");
            
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(intakeList.toArray(new String[0])));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage());
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Intake", "Error loading" }));
        }
    }
    //function meant for filtering out students for the selected intake
    private void populateCurrentIntakeStudents(String selectedIntake) {
        currentIntakeStudents.clear();
        manualPdfStudentMap.clear();
        
        if (allStudents == null || "Select Intake".equals(selectedIntake)) {
            return;
        }
        
        for (Student s : allStudents) {
            if (s != null && s.getIntake() != null && s.getIntake().equals(selectedIntake)) {
                currentIntakeStudents.add(s);
            }
        }
        
        Collections.sort(currentIntakeStudents, (s1, s2) -> s1.getName().compareTo(s2.getName()));
    }

    private Student getStudentById(String studentId) {
        return studentIdMap.get(studentId);
    }
    //extract the student ID from PDF filename eg TP123456 from the report_tp123456
    private String extractStudentIdFromFilename(String filename) {
        if (filename == null) return null;
        
        filename = filename.toUpperCase();
        
        int tpIndex = filename.indexOf("TP");
        if (tpIndex >= 0 && tpIndex + 8 <= filename.length()) {
            String possibleId = filename.substring(tpIndex, Math.min(tpIndex + 8, filename.length()));
            if (possibleId.matches("TP\\d{6}")) {
                return possibleId;
            }
        }
        
        String numbersOnly = filename.replaceAll("[^0-9]", " ");
        String[] numberParts = numbersOnly.trim().split("\\s+");
        for (String num : numberParts) {
            if (num.length() == 6) {
                String possibleId = "TP" + num;
                if (studentIdMap.containsKey(possibleId)) {
                    return possibleId;
                }
            }
        }
        
        return null;
    }
    //this function aim to help the PDFs align with the student via studentID
    private int autoMatchPdfsToStudents() {
        if (selectedPDFFiles == null || currentIntakeStudents.isEmpty()) {
            return 0;
        }
        
        manualPdfStudentMap.clear();
        int matchedCount = 0;
        
        for (File pdfFile : selectedPDFFiles) {
            String studentId = extractStudentIdFromFilename(pdfFile.getName());
            if (studentId != null) {
                Student student = getStudentById(studentId);
                if (student != null && currentIntakeStudents.contains(student)) {
                    manualPdfStudentMap.put(pdfFile, student);
                    matchedCount++;
                }
            }
        }
        
        return matchedCount;
    }
    //this come in play when there are 4 student and only 2 PDFs available 
    private void smartMatchPdfsToStudents() {
        if (selectedPDFFiles == null || selectedPDFFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No PDF files loaded.");
            return;
        }
        
        if (currentIntakeStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in selected intake.");
            return;
        }
        
        int autoMatched = autoMatchPdfsToStudents();
        
        if (autoMatched == selectedPDFFiles.length) {
            StringBuilder summary = new StringBuilder();
            summary.append("✓ ALL PDFs Automatically Matched!\n\n");
            summary.append("Successfully matched ").append(autoMatched).append(" PDF(s) to ").append(getUniqueStudentsFromMap().size()).append(" student(s):\n\n");
            
            Map<Student, List<File>> studentPdfs = new HashMap<>();
            for (Map.Entry<File, Student> entry : manualPdfStudentMap.entrySet()) {
                Student student = entry.getValue();
                File pdf = entry.getKey();
                studentPdfs.putIfAbsent(student, new ArrayList<>());
                studentPdfs.get(student).add(pdf);
            }
            
            for (Map.Entry<Student, List<File>> entry : studentPdfs.entrySet()) {
                Student student = entry.getKey();
                List<File> pdfs = entry.getValue();
                summary.append("• ").append(student.getName()).append(" (").append(student.getStudentId()).append(")\n");
                summary.append("  PDFs: ");
                for (File pdf : pdfs) {
                    summary.append(pdf.getName()).append(", ");
                }
                if (!pdfs.isEmpty()) {
                    summary.delete(summary.length() - 2, summary.length());
                }
                summary.append("\n\n");
            }
            
            summary.append("Total students in intake: ").append(currentIntakeStudents.size()).append("\n");
            summary.append("Students receiving PDFs: ").append(studentPdfs.size());
            
            int option = JOptionPane.showConfirmDialog(this, 
                summary.toString() + "\n\nProceed with these matches?",
                "Perfect Match Found!",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                return;
            }
            manualPdfStudentMap.clear();
        }

        if (autoMatched > 0 && autoMatched < selectedPDFFiles.length) {
            StringBuilder partialSummary = new StringBuilder();
            partialSummary.append("Partially matched ").append(autoMatched).append(" out of ").append(selectedPDFFiles.length).append(" PDFs.\n\n");
            
            Map<Student, List<File>> autoMatchedPdfs = new HashMap<>();
            for (Map.Entry<File, Student> entry : manualPdfStudentMap.entrySet()) {
                Student student = entry.getValue();
                File pdf = entry.getKey();
                autoMatchedPdfs.putIfAbsent(student, new ArrayList<>());
                autoMatchedPdfs.get(student).add(pdf);
            }
            
            if (!autoMatchedPdfs.isEmpty()) {
                partialSummary.append("Auto-matched:\n");
                for (Map.Entry<Student, List<File>> entry : autoMatchedPdfs.entrySet()) {
                    Student student = entry.getKey();
                    List<File> pdfs = entry.getValue();
                    partialSummary.append("• ").append(student.getName()).append(": ");
                    for (File pdf : pdfs) {
                        partialSummary.append(pdf.getName()).append(", ");
                    }
                    partialSummary.delete(partialSummary.length() - 2, partialSummary.length());
                    partialSummary.append("\n");
                }
                partialSummary.append("\n");
            }
            
            List<File> unmatchedPdfs = new ArrayList<>();
            for (File pdfFile : selectedPDFFiles) {
                if (!manualPdfStudentMap.containsKey(pdfFile)) {
                    unmatchedPdfs.add(pdfFile);
                }
            }
            
            if (!unmatchedPdfs.isEmpty()) {
                partialSummary.append("Unmatched PDFs (").append(unmatchedPdfs.size()).append("):\n");
                for (File pdf : unmatchedPdfs) {
                    partialSummary.append("• ").append(pdf.getName()).append("\n");
                }
            }
            
            Object[] options = {"Auto-Match Remaining", "Manual Match All", "Use Only Auto-Matched"};
            int choice = JOptionPane.showOptionDialog(this,
                partialSummary.toString() + "\n\nHow would you like to proceed?",
                "Partial Match Found",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            switch (choice) {
                case 0:
                    autoMatchRemainingPdfs();
                    break;
                case 1: 
                    manualPdfStudentMap.clear();
                    manualMatchPdfsToStudents();
                    break;
                case 2: 
                    break;
                default:
            }
        } else if (autoMatched == 0) {
            // No automatic matches at all
            int option = JOptionPane.showConfirmDialog(this,
                "No PDFs could be automatically matched.\n" +
                "Would you like to manually match PDFs to students?",
                "No Automatic Matches",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                manualMatchPdfsToStudents();
            }
        }
    }
    //automatically matches PDFs to student based on their ID in filename
    private void autoMatchRemainingPdfs() {
        List<File> unmatchedPdfs = new ArrayList<>();
        for (File pdfFile : selectedPDFFiles) {
            if (!manualPdfStudentMap.containsKey(pdfFile)) {
                unmatchedPdfs.add(pdfFile);
            }
        }
        
        int newlyMatched = 0;
        for (File pdfFile : unmatchedPdfs) {
            String studentId = extractStudentIdFromFilename(pdfFile.getName());
            if (studentId != null) {
                Student student = getStudentById(studentId);
                if (student != null && currentIntakeStudents.contains(student)) {
                    manualPdfStudentMap.put(pdfFile, student);
                    newlyMatched++;
                }
            }
        }
        
        JOptionPane.showMessageDialog(this,
            "Auto-matched " + newlyMatched + " additional PDF(s).\n" +
            "Total matched: " + manualPdfStudentMap.size() + "/" + selectedPDFFiles.length,
            "Auto-Match Complete",
            JOptionPane.INFORMATION_MESSAGE);
    }
    //manual matching user can decide whether sent or not the PDFs
    private void manualMatchPdfsToStudents() {
        for (File pdfFile : selectedPDFFiles) {
            if (manualPdfStudentMap.containsKey(pdfFile)) {
                continue;
            }
            
            String studentId = extractStudentIdFromFilename(pdfFile.getName());
            Student suggestedStudent = null;
            
            if (studentId != null) {
                suggestedStudent = getStudentById(studentId);
            }
            
            String[] studentOptions = new String[currentIntakeStudents.size() + 1];
            studentOptions[0] = "Skip this PDF (don't send)";
            
            int suggestedIndex = 0;
            for (int i = 0; i < currentIntakeStudents.size(); i++) {
                Student s = currentIntakeStudents.get(i);
                String option = s.getStudentId() + " - " + s.getName();
                studentOptions[i + 1] = option;
                
                if (suggestedStudent != null && s.getStudentId().equals(suggestedStudent.getStudentId())) {
                    suggestedIndex = i + 1;
                }
            }
            
            String selectedOption = (String) JOptionPane.showInputDialog(
                this,
                "Match PDF: " + pdfFile.getName() + "\n\nSelect student to send this PDF to:",
                "Match PDF to Student",
                JOptionPane.QUESTION_MESSAGE,
                null,
                studentOptions,
                studentOptions[suggestedIndex]
            );
            
            if (selectedOption != null && !selectedOption.startsWith("Skip")) {
                String selectedStudentId = selectedOption.split(" - ")[0];
                Student selectedStudent = getStudentById(selectedStudentId);
                
                if (selectedStudent != null) {
                    manualPdfStudentMap.put(pdfFile, selectedStudent);
                }
            }
        }
    }
    
    private Set<Student> getUniqueStudentsFromMap() {
        Set<Student> uniqueStudents = new HashSet<>();
        for (Student s : manualPdfStudentMap.values()) {
            uniqueStudents.add(s);
        }
        return uniqueStudents;
    }
    //main function of the sending email, this allow user to send the PDF with the attachment to those specific students.
    private void sendEmailWithAttachments(String to, String subject, String body, File[] attachments) {
        try {
            Properties p = new Properties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.host", SMTP_HOST);
            p.put("mail.smtp.port", SMTP_PORT);
            
            Session session = Session.getInstance(p, new Authenticator() {
               @Override
               protected PasswordAuthentication getPasswordAuthentication() {
                   return new PasswordAuthentication(USERNAME, APPPASS);
               }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            
            if (attachments != null && attachments.length > 0) {
                for (File attachment : attachments) {
                    if (attachment.exists()) {
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        FileDataSource source = new FileDataSource(attachment);
                        attachmentPart.setDataHandler(new DataHandler(source));
                        attachmentPart.setFileName(attachment.getName());
                        multipart.addBodyPart(attachmentPart);
                    }
                }
            }
            
            message.setContent(multipart);
            Transport.send(message);
            
            System.out.println("Email sent successfully to " + to);
            
        } catch (Exception e) {
            System.err.println("Failed to send to: " + to + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        MessageEmail = new javax.swing.JTextField();
        exportPDF = new javax.swing.JButton();
        SendEmail = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listEmail = new javax.swing.JList<>();
        jButton1 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Compose Email");

        jLabel3.setText("Message:");

        MessageEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MessageEmailActionPerformed(evt);
            }
        });

        exportPDF.setText("Attach PDF");
        exportPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPDFActionPerformed(evt);
            }
        });

        SendEmail.setText("Send");
        SendEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendEmailActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(listEmail);

        jButton1.setText("Back");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(exportPDF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(SendEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(MessageEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exportPDF)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(SendEmail))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(MessageEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MessageEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MessageEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MessageEmailActionPerformed
        //Triggered when clicking the attach pdf button, it will call the function above and load pdfs to matches student
    private void exportPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPDFActionPerformed
         String selectedIntake = (String) jComboBox1.getSelectedItem();
        if ("Select Intake".equals(selectedIntake)) {
            JOptionPane.showMessageDialog(this, "Please select an intake first.");
            return;
        }
        
        populateCurrentIntakeStudents(selectedIntake);
        
        String folderName = selectedIntake.replace(" ", "_");
        File intakeFolder = new File("Reports/" + folderName);
        //validates intake selection, loads PDFs from intake folder
        if (!intakeFolder.exists() || !intakeFolder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "No PDF folder found: Reports/" + folderName, "Folder Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        selectedPDFFiles = intakeFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        
        if (selectedPDFFiles == null || selectedPDFFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No PDF files found.", "No PDFs", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //update the Jlist display to show PDF matching status
        updatePdfListDisplay();
        
        StringBuilder summary = new StringBuilder();
        summary.append("Found ").append(selectedPDFFiles.length).append(" PDF file(s)\n");
        summary.append("Intake has ").append(currentIntakeStudents.size()).append(" student(s)\n\n");
        
        int autoMatched = autoMatchPdfsToStudents();
        summary.append("Auto-matched: ").append(autoMatched).append(" PDF(s)\n");
        
        if (autoMatched == selectedPDFFiles.length) {
            summary.append("\n✓ Perfect match! All PDFs can be sent automatically.");
        } else if (autoMatched > 0) {
            summary.append("\n⚠ Partial match. Some PDFs need manual assignment.");
        } else {
            summary.append("\n✗ No automatic matches found.");
        }
        
        JOptionPane.showMessageDialog(this, 
            summary.toString(),
            "PDFs Loaded", 
            JOptionPane.INFORMATION_MESSAGE);
        
        smartMatchPdfsToStudents();
        
        updatePdfListDisplay();
    }                                         

    private void updatePdfListDisplay() {
        if (selectedPDFFiles == null) return;
        
        List<String> fileInfo = new ArrayList<>();
        for (File file : selectedPDFFiles) {
            String status;
            if (manualPdfStudentMap.containsKey(file)) {
                Student student = manualPdfStudentMap.get(file);
                status = "✓ " + file.getName() + " → " + student.getName() + " (" + student.getStudentId() + ")";
            } else {
                String studentId = extractStudentIdFromFilename(file.getName());
                if (studentId != null) {
                    Student student = getStudentById(studentId);
                    if (student != null) {
                        status = "? " + file.getName() + " → [Not assigned] (ID detected: " + studentId + ")";
                    } else {
                        status = "? " + file.getName() + " → [No matching student for ID: " + studentId + "]";
                    }
                } else {
                    status = "? " + file.getName() + " → [ID not detected]";
                }
            }
            fileInfo.add(status);
        }
        
        listEmail.setListData(fileInfo.toArray(new String[0]));
    }//GEN-LAST:event_exportPDFActionPerformed
    //triggered when user click the sent button and it will called the function above to do the necessary sending to the student
    private void SendEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendEmailActionPerformed
        String selectedIntake = (String) jComboBox1.getSelectedItem();
        String message = MessageEmail.getText().trim();
        
        if ("Select Intake".equals(selectedIntake)) {
            JOptionPane.showMessageDialog(this, "Please select an intake.");
            return;
        }
        //validates matches exist, groups PDFs by student
        if (manualPdfStudentMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No PDFs have been matched to students.\n" +
                "Please load PDFs and match them to students first.",
                "No PDF Matches", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Map<Student, List<File>> studentPdfsMap = new HashMap<>();
        for (Map.Entry<File, Student> entry : manualPdfStudentMap.entrySet()) {
            Student student = entry.getValue();
            File pdf = entry.getKey();
            
            studentPdfsMap.putIfAbsent(student, new ArrayList<>());
            studentPdfsMap.get(student).add(pdf);
        }
        showSendConfirmation(studentPdfsMap, message);
    }                                         
        //a confirmation dialog which also called the function above before sending emails
        //it will first display which student will be receiving the email
    private void showSendConfirmation(Map<Student, List<File>> studentPdfsMap, String message) {
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("Send emails to ").append(studentPdfsMap.size()).append(" student(s):\n\n");
        
        for (Map.Entry<Student, List<File>> entry : studentPdfsMap.entrySet()) {
            Student student = entry.getKey();
            List<File> pdfs = entry.getValue();
            
            confirmMsg.append("• ").append(student.getName()).append(" (").append(student.getStudentId()).append(")\n");
            confirmMsg.append("  Email: ").append(student.getEmail()).append("\n");
            confirmMsg.append("  PDFs: ");
            for (File pdf : pdfs) {
                confirmMsg.append(pdf.getName()).append(", ");
            }
            if (!pdfs.isEmpty()) {
                confirmMsg.delete(confirmMsg.length() - 2, confirmMsg.length());
            }
            confirmMsg.append("\n\n");
        }
        
        confirmMsg.append("Total students in intake: ").append(currentIntakeStudents.size()).append("\n");
        confirmMsg.append("Students receiving emails: ").append(studentPdfsMap.size()).append("\n");
        confirmMsg.append("Students NOT receiving emails: ").append(currentIntakeStudents.size() - studentPdfsMap.size());
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            confirmMsg.toString() + "\n\nSend emails?",
            "Confirm Email Send", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        sendEmailsToStudents(studentPdfsMap, message);
    }
    //this is where the sending happen with each attach PDF to the student
    private void sendEmailsToStudents(Map<Student, List<File>> studentPdfsMap, String message) {
        int successCount = 0;
        Set<File> filesToDelete = new HashSet<>();
        //loops through each student , send email with their assigned PDF
        for (Map.Entry<Student, List<File>> entry : studentPdfsMap.entrySet()) {
            Student student = entry.getKey();
            List<File> pdfs = entry.getValue();
            
            if (student.getEmail() == null || student.getEmail().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No email for student: " + student.getName() + " (" + student.getStudentId() + ")",
                    "Missing Email", 
                    JOptionPane.WARNING_MESSAGE);
                continue;
            }
            
            try {
                String body = message.isEmpty() ? 
                        "Dear " + student.getName() + ",\n\nPlease find your documents attached." : 
                        "Dear " + student.getName() + ",\n\n" + message;
                //uses this function for actual email sending
                sendEmailWithAttachments(
                    student.getEmail(), 
                    "APU Exam Documents", 
                    body, 
                    pdfs.toArray(new File[0])
                );
                
                successCount++;
                filesToDelete.addAll(pdfs);
                
                Thread.sleep(500);
                
            } catch (Exception e) {
                System.out.println("Failed to send to " + student.getEmail() + ": " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Failed to send to " + student.getName() + ": " + e.getMessage(),
                    "Send Error", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        //once successful it will delete the PDF to avoid duplication
        for (File f : filesToDelete) {
            if (f.exists()) {
                if (f.delete()) {
                    System.out.println("Deleted: " + f.getName());
                }
            }
        }
        
        JOptionPane.showMessageDialog(this, 
            "Email sending complete!\n\n" +
            "Successfully sent: " + successCount + "/" + studentPdfsMap.size() + " students\n" +
            "Total students in intake: " + currentIntakeStudents.size() + "\n" +
            "Files deleted: " + filesToDelete.size(),
            "Send Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_SendEmailActionPerformed

    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(parentDashboard != null)
        {
            parentDashboard.setVisible(true);
        }
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String selectedIntake = (String) jComboBox1.getSelectedItem();
        if (!"Select Intake".equals(selectedIntake)) {
            populateCurrentIntakeStudents(selectedIntake);
            manualPdfStudentMap.clear();
            listEmail.setListData(new String[]{});
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Email.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Email.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Email.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Email.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Email().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField MessageEmail;
    private javax.swing.JButton SendEmail;
    private javax.swing.JButton exportPDF;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> listEmail;
    // End of variables declaration//GEN-END:variables
}
