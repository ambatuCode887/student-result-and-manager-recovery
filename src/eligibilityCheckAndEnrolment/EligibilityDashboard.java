/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package eligibilityCheckAndEnrolment;

import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

import repository.Config;
import repository.TextDataRepository;
import model.Student;
import model.GradeRecord;
import model.EnrolmentDecision;
import model.ModuleRow;
import model.EligibilityResult;
import repository.DataRepository;

/**
 * The main graphical interface for the Academic Officer.
 * <p>
 * This dashboard provides a centralized view of all students, allowing the officer to:
 * <ul>
 * <li>Filter students by Programme, Intake, or Search terms.</li>
 * <li>View real-time calculated eligibility status (Eligible/Ineligible).</li>
 * <li>Drill down into specific student details via pop-up.</li>
 * <li>Perform bulk operations like "Auto-Decide".</li>
 * </ul>
 * </p>
 */
public class EligibilityDashboard extends javax.swing.JFrame {
    
    private static final String SEARCH_PLACEHOLDER = "Student Name/ID";
    private static final java.awt.Color PLACEHOLDER_COLOR = new java.awt.Color(150, 150, 150);
    private enum FilterState { ALL, INELIGIBLE, ELIGIBLE }
    private FilterState currentFilterState = FilterState.ALL;

    // --- Navigation ---
    private javax.swing.JFrame parentFrame;
    
    // --- Components ---
    private javax.swing.JTable resultsTable;
    private javax.swing.table.DefaultTableModel resultsModel;
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter;

    // --- Data Layer ---
    private DataRepository repository;
    private EligibilityService service;

    // --- In-Memory Data Cache ---
    private List<Student> allStudents = new ArrayList<>();
    private Map<String, List<GradeRecord>> gradesMap = new HashMap<>();
    private List<EnrolmentDecision> allDecisions = new ArrayList<>();
    private List<ModuleRow> allModules = new ArrayList<>();
    private List<EligibilityResult> currentResults = new ArrayList<>();

    // --- Filter State  ---
    private String currentSearchText = "";
    
    public EligibilityDashboard() {
        initComponents();
        commonInit();
    }

    public EligibilityDashboard(javax.swing.JFrame parentFrame) {
        this();
        this.parentFrame = parentFrame;
    }
    
    private void commonInit() {
        // Initialize Central Repository
        this.repository = new TextDataRepository(
            Config.STUDENTS_FILE, 
            Config.MODULES_FILE, 
            Config.GRADES_FILE, 
            Config.ENROLMENT_FILE,
            Config.COURSES_FILE
        );
        this.service = new EligibilityService();

        // Setup UI components and load data
        setupTable();
        reloadData();
        populateFilters();
        performEligibilityCheck();
    }

    private void reloadData() {
        try {
            allStudents = repository.loadStudents();
            List<GradeRecord> rawGrades = repository.loadGradeRecords();
            gradesMap = rawGrades.stream().collect(Collectors.groupingBy(GradeRecord::getStudentId));
            allDecisions = repository.loadEnrolmentDecisions();
            allModules = repository.loadModules();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void setupTable() {
        resultsModel = new javax.swing.table.DefaultTableModel(
            new Object[]{"Student ID", "Name", "Programme", "Intake", "CGPA", "Failed", "Status", "Decision", "Reasons"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 4) return Double.class;
                if (col == 5) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultsTable = new javax.swing.JTable(resultsModel);
        resultsTable.setFillsViewportHeight(true);
        sorter = new javax.swing.table.TableRowSorter<>(resultsModel);
        resultsTable.setRowSorter(sorter);
        jScrollPane1.setViewportView(resultsTable);
        
        // Listeners
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && resultsTable.getSelectedRow() >= 0) {
                    showDetailsFor(resultsTable.convertRowIndexToModel(resultsTable.getSelectedRow()));
                }
            }
        });
        
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            btnViewDetails.setEnabled(resultsTable.getSelectedRow() >= 0);
        });
        btnViewDetails.setEnabled(false);
        
        configureTableColumns();
    }

    /**
     * The core filtering and processing engine of the dashboard.
     * <p>
     * This method executes in three steps:
     * 1. Reads the current filter selections (Dropdowns & Search Bar).
     * 2. Filters the in-memory list of students to find matches.
     * 3. Calls the EligibilityService to calculate fresh results for every visible student.
     * </p>
     * Updates the JTable model with the final results.
     */
    private void performEligibilityCheck() {
        // Retrieve dropdown selections
        String pSel = (String) cmbProgramme.getSelectedItem();
        String iSel = (String) cmbIntake.getSelectedItem();
        String p = (pSel == null || pSel.equals("All Programmes")) ? "" : pSel.trim();
        String i = (iSel == null || iSel.equals("All Intakes")) ? "" : iSel.trim();

        // Filter the in-memory student list
        List<Student> targets = new ArrayList<>();
        for (Student s : allStudents) {
            if (!p.isEmpty() && !s.getProgramme().equals(p)) continue;
            if (!i.isEmpty() && !s.getIntake().equals(i)) continue;
            targets.add(s);
        }

        // Evaluate & Populate
        resultsModel.setRowCount(0);
        currentResults.clear();
        
        for (Student s : targets) {
            List<GradeRecord> studentGrades = gradesMap.getOrDefault(s.getStudentId(), new ArrayList<>());
            
            if (studentGrades.isEmpty()) continue;

            EligibilityResult r = service.evaluate(s, studentGrades);
            currentResults.add(r);

            String decision = "Pending";
            // Find the most recent decision for this student/intake pair.
            // We reduce the stream to get the last element (latest decision).
            Optional<EnrolmentDecision> lastDec = allDecisions.stream()
                .filter(d -> d.getStudentId().equals(s.getStudentId()) && d.getIntake().equals(s.getIntake()))
                .reduce((first, second) -> second); 

            if (lastDec.isPresent()) decision = lastDec.get().getDecision();

            resultsModel.addRow(new Object[]{
                s.getStudentId(), s.getName(), s.getProgramme(), s.getIntake(),
                r.getCgpa(), r.getFailedCount(),
                (r.getStatus() == EligibilityResult.Status.ELIGIBLE ? "Eligible" : "Not Eligible"),
                decision, String.join("; ", r.getReasons())
            });
        }
        
        // Re-apply existing text filters after data reload
        applyFilters();
    }
    
    private void applyFilters() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Apply text search
        if (!currentSearchText.isEmpty() && !currentSearchText.equals(SEARCH_PLACEHOLDER)) {
            filters.add(RowFilter.regexFilter("(?i)" + currentSearchText, 0, 1));
        }

        // Status Filter
        switch (currentFilterState) {
            case INELIGIBLE:
                filters.add(RowFilter.regexFilter("Not Eligible", 6));
                break;
            case ELIGIBLE:
                filters.add(RowFilter.regexFilter("^Eligible$", 6));
                break;
            case ALL:
            default:
                // No filter needed for ALL
                break;
        }

        // Apply Combined Filter
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }
    
    private void populateFilters() {
        Set<String> progs = new TreeSet<>();
        Set<String> intakes = new TreeSet<>();
        for (Student s : allStudents) {
            if (s.getProgramme() != null) progs.add(s.getProgramme());
            if (s.getIntake() != null) intakes.add(s.getIntake());
        }
        cmbProgramme.setModel(new javax.swing.DefaultComboBoxModel<>(combine("All Programmes", progs)));
        cmbIntake.setModel(new javax.swing.DefaultComboBoxModel<>(combine("All Intakes", intakes)));
    }
    
    private String[] combine(String first, Set<String> others) {
        List<String> list = new ArrayList<>();
        list.add(first);
        list.addAll(others);
        return list.toArray(String[]::new);
    }
    
    private void configureTableColumns() {
        // Disable auto-resize to allow horizontal scrolling for many columns
        resultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

        // Defined layout: Min width ensures readability; Max width prevents small cols (like GPA) from stretching.
        int[] min  = { 70,  100,   150,   80,   50,    50,    80,      80,    150 };
        int[] pref = { 80, 110,   250,   100,  60,    60,    100,     100,   400 };
        int[] max  = { 100, 200,   400,   150,  80,    80,    150,     150,   9999 };

        for (int i = 0; i < resultsTable.getColumnCount() && i < pref.length; i++) {
            javax.swing.table.TableColumn col = resultsTable.getColumnModel().getColumn(i);
            col.setMinWidth(min[i]);
            col.setPreferredWidth(pref[i]);
            col.setMaxWidth(max[i]);

            // Right-aligning numbers
            if (i == 4 || i == 5) {
                javax.swing.table.DefaultTableCellRenderer right = new javax.swing.table.DefaultTableCellRenderer();
                right.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                col.setCellRenderer(right);
            }
            
            // Tooltip for Text Heavy Columns
            if (i == 1 || i == 2 || i == 8) {
                col.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(
                            javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setToolTipText(getText());
                        return this;
                    }
                });
            }
            
            //Color Coding Status
            else if (i == 6) { 
                col.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(
                            javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        String status = (value != null) ? value.toString() : "";
                        
                        if ("ELIGIBLE".equalsIgnoreCase(status)) {
                            setForeground(new java.awt.Color(0, 150, 0));
                            setFont(getFont().deriveFont(java.awt.Font.BOLD));
                        } else if ("NOT ELIGIBLE".equalsIgnoreCase(status)) {
                            setForeground(java.awt.Color.RED);
                            setFont(getFont().deriveFont(java.awt.Font.BOLD));
                        } else {
                            setForeground(java.awt.Color.BLACK);
                        }
                        return this;
                    }
                });
            }
        }
    }
    
    /**
     * Executes the Batch Decision Logic.
     * <p>
     * Iterates through the currently visible list of students. If a student's
     * decision status is "Pending", it automatically assigns:
     * - "Approved" if status is ELIGIBLE.
     * - "Rejected" if status is NOT ELIGIBLE.
     * </p>
     * Saves all changes to the audit log immediately.
     */
    private void autoDecide() {
        int approved = 0, rejected = 0;
        String timestamp = java.time.LocalDateTime.now().toString();

        for (int row = 0; row < resultsModel.getRowCount(); row++) {
            String status = (String) resultsModel.getValueAt(row, 6); 
            String currentDec = (String) resultsModel.getValueAt(row, 7); 
            String sid = (String) resultsModel.getValueAt(row, 0);
            String intake = (String) resultsModel.getValueAt(row, 3);

            if (!"Pending".equalsIgnoreCase(currentDec)) continue;
            String newDecision = "Eligible".equalsIgnoreCase(status) ? "Approved" : "Rejected";
            
            EnrolmentDecision ed = new EnrolmentDecision(sid, intake, newDecision, timestamp);
            try {
                repository.saveEnrolmentDecision(ed);
                if (newDecision.equals("Approved")) approved++; else rejected++;
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        reloadData();
        performEligibilityCheck();
        JOptionPane.showMessageDialog(this, "Auto-decision complete.\nApproved: " + approved + "\nRejected: " + rejected);
    }

    private void showDetailsFor(int modelRow) {
        if (modelRow < 0 || modelRow >= currentResults.size()) return;
        
        // Get the specific student result
        EligibilityResult r = currentResults.get(modelRow);
        
        // Open the dialog window
        new StudentDetailsDialog(this, r, allModules).setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbProgramme = new javax.swing.JComboBox<>();
        cmbIntake = new javax.swing.JComboBox<>();
        btnExport = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnAutoDecide = new javax.swing.JButton();
        btnFind = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        btnClear = new javax.swing.JButton();
        btnViewDetails = new javax.swing.JButton();
        btnFilterCycle = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Eligibility Dashboard");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Toolbar:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Search:");

        cmbProgramme.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Programmes", "Diploma in ICT (Software Engineering)", "Diploma in ICT (Networking)", "BSc (Hons) in Computer Science", "BSc (Hons) in Software Engineering", "BSc (Hons) in Artificial Intelligence", "BSc (Hons) in Cyber Security", "BSc (Hons) in Information Technology", "BSc (Hons) in Data Analytics" }));
        cmbProgramme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbProgrammeActionPerformed(evt);
            }
        });

        cmbIntake.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Intakes", "Mar 2024", "Jul 2024", "Sep 2024", "Nov 2024", "Mar 2025", "Jul 2025", "Sep 2025", "Nov 2025" }));
        cmbIntake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbIntakeActionPerformed(evt);
            }
        });

        btnExport.setText("Export Results");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        txtSearch.setForeground(new java.awt.Color(150, 150, 150));
        txtSearch.setText("Student Name/ID");
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearchFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearchFocusLost(evt);
            }
        });
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnAutoDecide.setText("Auto Decide");
        btnAutoDecide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutoDecideActionPerformed(evt);
            }
        });

        btnFind.setText("Find");
        btnFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Results");

        btnClear.setText("Clear Filter");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnViewDetails.setText("View Details");
        btnViewDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewDetailsActionPerformed(evt);
            }
        });

        btnFilterCycle.setText("Show: All");
        btnFilterCycle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterCycleActionPerformed(evt);
            }
        });

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnBack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnViewDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAutoDecide))
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbProgramme, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbIntake, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFind, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFilterCycle, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRefresh))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 884, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbProgramme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbIntake, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear)
                    .addComponent(btnFilterCycle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFind))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(btnRefresh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnViewDetails)
                    .addComponent(btnExport)
                    .addComponent(btnAutoDecide)
                    .addComponent(btnBack))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbProgrammeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbProgrammeActionPerformed
        performEligibilityCheck();
    }//GEN-LAST:event_cmbProgrammeActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // Enter Key in Text Field
        currentSearchText = txtSearch.getText().trim();
        applyFilters();
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindActionPerformed
        // Find Button
        currentSearchText = txtSearch.getText().trim();
        applyFilters();
    }//GEN-LAST:event_btnFindActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // Reset Search
        txtSearch.setText(SEARCH_PLACEHOLDER);
        txtSearch.setForeground(PLACEHOLDER_COLOR);
        currentSearchText = "";
        
        // Reset Filter Cycle
        currentFilterState = FilterState.ALL;
        btnFilterCycle.setText("Filter: Show All");
        
        // Reset Dropdowns
        cmbProgramme.setSelectedIndex(0);
        cmbIntake.setSelectedIndex(0);
        
        performEligibilityCheck();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnViewDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewDetailsActionPerformed
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow >= 0) showDetailsFor(resultsTable.convertRowIndexToModel(viewRow));
    }//GEN-LAST:event_btnViewDetailsActionPerformed

    private void cmbIntakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbIntakeActionPerformed
        performEligibilityCheck();
    }//GEN-LAST:event_cmbIntakeActionPerformed

    private void txtSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusGained
        if(txtSearch.getText().equals(SEARCH_PLACEHOLDER)) {
            txtSearch.setText("");
            txtSearch.setForeground(java.awt.Color.BLACK);
        }
    }//GEN-LAST:event_txtSearchFocusGained

    private void txtSearchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusLost
        if (txtSearch.getText().trim().isEmpty()) {
            txtSearch.setText(SEARCH_PLACEHOLDER);
            txtSearch.setForeground(PLACEHOLDER_COLOR);
        }
    }//GEN-LAST:event_txtSearchFocusLost

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setSelectedFile(new java.io.File("eligibility_results.csv"));
        
        if (fc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File f = fc.getSelectedFile();
            try (java.io.PrintWriter w = new java.io.PrintWriter(
                    java.nio.file.Files.newBufferedWriter(f.toPath(), java.nio.charset.StandardCharsets.UTF_8))) {

                // Write Header
                w.println("student_id,name,programme,intake,cgpa,failed,status,decision,reasons");

                // Write Rows (respecting current sort/filter)
                for (int v = 0; v < resultsTable.getRowCount(); v++) {
                    int m = resultsTable.convertRowIndexToModel(v);
                    String[] fields = new String[resultsModel.getColumnCount()];
                    for (int c = 0; c < resultsModel.getColumnCount(); c++) {
                        Object val = resultsModel.getValueAt(m, c);
                        String cell = val == null ? "" : val.toString();
                        fields[c] = cell.replace("\n", " ").replace(",", ";").trim();
                    }
                    w.println(String.join(",", fields));
                }
                JOptionPane.showMessageDialog(this, "Export Successful!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnAutoDecideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutoDecideActionPerformed
        autoDecide();
    }//GEN-LAST:event_btnAutoDecideActionPerformed

    private void btnFilterCycleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterCycleActionPerformed
        // Cycles through (ALL -> INELIGIBLE -> ELIGIBLE -> ALL)
        switch (currentFilterState) {
            case ALL:
                currentFilterState = FilterState.INELIGIBLE;
                btnFilterCycle.setText("Show: Ineligible Only");
                break;
            case INELIGIBLE:
                currentFilterState = FilterState.ELIGIBLE;
                btnFilterCycle.setText("Show: Eligible Only");
                break;
            case ELIGIBLE:
                currentFilterState = FilterState.ALL;
                btnFilterCycle.setText("Show: All");
                break;
        }
        // Re-run the filter logic
        applyFilters();
    }//GEN-LAST:event_btnFilterCycleActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        }
        this.dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        reloadData();
        performEligibilityCheck();
        JOptionPane.showMessageDialog(this, "Data Refreshed!");
    }//GEN-LAST:event_btnRefreshActionPerformed

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
            java.util.logging.Logger.getLogger(EligibilityDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EligibilityDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EligibilityDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EligibilityDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EligibilityDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAutoDecide;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFilterCycle;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnViewDetails;
    private javax.swing.JComboBox<String> cmbIntake;
    private javax.swing.JComboBox<String> cmbProgramme;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
