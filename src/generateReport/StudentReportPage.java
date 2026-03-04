/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package generateReport;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Central Data Imports
import repository.Config;
import repository.TextDataRepository;
import model.Student;
import model.GradeRecord;
import model.Milestone;
import model.ModuleRow;
import repository.DataRepository;

/**
 *
 * @author HONG
 */
public class StudentReportPage extends javax.swing.JFrame {

    private final DataRepository repository;
    private javax.swing.JFrame parentDashboard;

    /**
     * Creates new form Report
     */
    public StudentReportPage() {
        initComponents();

        // Initialize the central repository
        this.repository = new TextDataRepository(
                Config.STUDENTS_FILE,
                Config.MODULES_FILE,
                Config.GRADES_FILE,
                Config.ENROLMENT_FILE,
                Config.COURSES_FILE
        );

        loadLectureToDropdown();
    }

    public void setParentDashboard(javax.swing.JFrame parent) {
        this.parentDashboard = parent;
    }

    private void searchStudent() {
        String searchId = jTextField1.getText().trim();
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.");
            return;
        }
        try {
            // Find Student
            List<Student> students = repository.loadStudents();
            Optional<Student> found = students.stream()
                    .filter(s -> s.getStudentId().equalsIgnoreCase(searchId))
                    .findFirst();
            if (found.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student not found!");
                // Clear fields
                jTextField2.setText("");
                jTextField3.setText("");
                jTextField4.setText("");
                jTextField5.setText("");
                jTextArea2.setText("");
                ((DefaultTableModel) jTable1.getModel()).setRowCount(0);
                ((DefaultTableModel) jTable2.getModel()).setRowCount(0);
                return;
            }
            Student s = found.get();
            jTextField2.setText(s.getName());
            jTextField3.setText(s.getStudentId());
            jTextField4.setText(s.getProgramme());
            jTextField5.setText(s.getIntake());

            List<GradeRecord> allGrades = repository.loadGradeRecords();

            // Filter the grades down to only the grades for this student ID.
            List<GradeRecord> studentGrades = allGrades.stream()
                    .filter(g -> g.getStudentId().equalsIgnoreCase(searchId))
                    .toList();
            loadStudentGrades(s.getStudentId());
            loadStudentMilestones(s.getStudentId());
            loadStudentRecommendations(searchId);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error data: " + e.getMessage());
        }
    }

    private void loadStudentMilestones(String studentId) throws Exception {
        // Get the table model for Table 2
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);

        // Load all milestones from the repository
        List<Milestone> allMilestones = repository.loadMilestones();

        // Filter and Add rows
        for (Milestone m : allMilestones) {
            // Check if this milestone belongs to the student
            if (m.getStudentID().equalsIgnoreCase(studentId)) {
                model.addRow(new Object[]{
                    m.getCourse(),
                    m.getStudyWeek(),
                    m.getTask(),
                    m.getProgress()
                });
            }
        }
    }

    private void loadStudentGrades(String studentId) throws Exception {
        List<GradeRecord> allGrades = repository.loadGradeRecords();
        List<ModuleRow> allModules = repository.loadModules();

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clear table

        int failCount = 0;
        boolean hasRecords = false;

        for (GradeRecord g : allGrades) {
            if (g.getStudentId().equalsIgnoreCase(studentId)) {
                hasRecords = true;

                String modName = allModules.stream()
                        .filter(m -> m.getCode().equalsIgnoreCase(g.getModuleCode()))
                        .map(ModuleRow::getName)
                        .findFirst()
                        .orElse("Unknown Module");

                model.addRow(new Object[]{
                    g.getModuleCode(),
                    modName,
                    g.getGrade(),
                    g.getGradePoint(),
                    g.getCreditHours()
                });
                if (g.getGradePoint() < 2.0) {
                    failCount++;
                }
            }
        }

        // Highlight with red color to fail subject only
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        double gp = Double.parseDouble(table.getValueAt(row, 3).toString());
                        c.setBackground(gp < 2.0 ? new Color(255, 220, 220) : Color.WHITE);
                    } catch (Exception e) {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        if (hasRecords && failCount == 0) {
            JOptionPane.showMessageDialog(null, "Congrats, You have no failed subjects.");
        }
    }

    private void generatePDF() {
        String id = jTextField3.getText().trim();
        String studentIntake = jTextField5.getText().trim();
        String selectedLecture = jComboBox1.getSelectedItem().toString();  
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for a student first.");
            return;
        }
        if (selectedLecture.equals("Select Lecture")) {
        JOptionPane.showMessageDialog(null, "lease select a valid Lecturer before generating the report!");
        return; 
    }
        String reportBaseDir = "Reports"; // file name
        String intakeFolderName = studentIntake.replaceAll("[\\s/\\\\]", "_");
        File intakeFolder = new File(reportBaseDir, intakeFolderName);
        if (!intakeFolder.exists()) {
            intakeFolder.mkdirs(); // create the file automatically
        }
        String fileName = "Report_" + id + ".pdf";
        String finalFilePath = Paths.get(intakeFolder.getAbsolutePath(), fileName).toString();
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(finalFilePath));
            doc.open();

            // Define Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 10);

            // make size 9
            Font recommendationFont = new Font(Font.FontFamily.HELVETICA, 9);

            //Header and student info
            Paragraph title = new Paragraph("Student Academic Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(Chunk.NEWLINE);

            // Student Info
            doc.add(new Paragraph("Name: " + jTextField2.getText(), contentFont));
            doc.add(new Paragraph("ID: " + jTextField3.getText(), contentFont));
            doc.add(new Paragraph("Program: " + jTextField4.getText(), contentFont));
            doc.add(new Paragraph("Intake: " + jTextField5.getText(), contentFont));
            doc.add(new Paragraph("Lecture: " + jComboBox1.getSelectedItem(), contentFont));
            doc.add(Chunk.NEWLINE);

            //table 1
            doc.add(new Paragraph("Module Grades & Performance", sectionFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable gradesTable = new PdfPTable(5);
            gradesTable.setWidthPercentage(100);
            String[] headers = {"Code", "Module Name", "Grade", "GP", "Credits"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, contentFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                gradesTable.addCell(cell);
            }

            DefaultTableModel gradesModel = (DefaultTableModel) jTable1.getModel();
            for (int row = 0; row < gradesModel.getRowCount(); row++) {
                for (int col = 0; col < 5; col++) {
                    Object val = gradesModel.getValueAt(row, col);
                    gradesTable.addCell(new Phrase(val == null ? "" : val.toString(), contentFont));
                }
            }
            doc.add(gradesTable);
            doc.add(Chunk.NEWLINE);

            //Table 2
            doc.add(new Paragraph("Academic Milestones", sectionFont));
            doc.add(Chunk.NEWLINE);

            PdfPTable milestonesTable = new PdfPTable(4);
            milestonesTable.setWidthPercentage(100);
            String[] milestoneHeaders = {"Module Code", "Study Week", "Task", "Progress"};

            for (String h : milestoneHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, contentFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                milestonesTable.addCell(cell);
            }

            DefaultTableModel milestonesModel = (DefaultTableModel) jTable2.getModel();
            for (int row = 0; row < milestonesModel.getRowCount(); row++) {
                for (int col = 0; col < 4; col++) {
                    Object val = milestonesModel.getValueAt(row, col);
                    milestonesTable.addCell(new Phrase(val == null ? "" : val.toString(), contentFont));
                }
            }
            doc.add(milestonesTable);
            doc.add(Chunk.NEWLINE);

            //recommandation
            doc.add(new Paragraph("Recovery Recommendations:", sectionFont));
            doc.add(Chunk.NEWLINE);

            // Use the smaller recommendationFont
            Paragraph recommendationContent = new Paragraph(jTextArea2.getText().trim(), recommendationFont);
            recommendationContent.setIndentationLeft(20);
            doc.add(recommendationContent);

            doc.close();
            JOptionPane.showMessageDialog(this, "PDF Generated: " + fileName);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + ex.getMessage());
        }
    }

    private void loadStudentRecommendations(String studentId) throws Exception {
        jTextArea2.setText("");
        List<String> plans = repository.loadRecoveryPlans();
        StringBuilder sb = new StringBuilder();
        for (String line : plans) {
            // Format: student_id,module_code,recommendations,milestones
            String[] parts = line.split("\\|", 4);  // prevent breaking if recommendation has commas
            if (parts.length >= 3) {
                String rowId = parts[0].trim();
                String moduleCode = parts[1].trim();
                String rec = parts[2].trim();
                if (rowId.equalsIgnoreCase(studentId)) {
                    if (sb.length() > 0) {
                        sb.append("\n"); // new line between modules
                    }
                    sb.append(moduleCode)
                            .append(": ")
                            .append(rec);
                }}}
        if (sb.length() > 0) {
            jTextArea2.setText(sb.toString());
        } else {
            jTextArea2.setText("No recommandation");
        }}
    
private void loadLectureToDropdown() {
    try {
        // Call the method in the Repository
        List<String> instructors = repository.loadLecture(); 
        // Clear existing items (This removes "Item 1", "Item 2"...)
        jComboBox1.removeAllItems();      
        jComboBox1.addItem("Select Lecture");
        for (String name : instructors) {
            jComboBox1.addItem(name);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading lecture: " + e.getMessage());
        e.printStackTrace(); 
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

        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();

        jLabel7.setText("jLabel7");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Student ID");

        jLabel2.setText("Student Name");

        jLabel3.setText("Program");

        jLabel4.setText("Intake");

        jLabel5.setText("Lecture");

        jLabel6.setText("Search");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Generate");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Module Code", "Module Name", "Grade", "CGPA", "Credit Hrs"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Module Code", "Study Week", "Task", "Progess"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jLabel8.setText("Recommendation");

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        jButton3.setText("Back");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(283, 283, 283)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField1)
                                    .addComponent(jTextField2)
                                    .addComponent(jTextField3)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton1)))
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(208, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        searchStudent();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        generatePDF();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
        }
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

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
            java.util.logging.Logger.getLogger(StudentReportPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentReportPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentReportPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentReportPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentReportPage().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
