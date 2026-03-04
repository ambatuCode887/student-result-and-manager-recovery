package CourseRecoverySystem;

import CourseRecoverySystem.Milestone.ManageMilestone;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import repository.Config;
import repository.TextDataRepository;
import repository.DataRepository;
import model.EnrolmentDecision;
import model.Student;
import model.GradeRecord;
import model.Milestone;
import oodj_user_management.Dashboard.AO_dashboard;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Refactored CourseRecoveryPage
 Now uses DataRepository to load clean Objects instead of raw Strings.
 */
public class CourseRecoveryPage extends javax.swing.JFrame {

    // The single entry point for data
    private final DataRepository repository;
    // Data stored in memory as Objects
    private List<EnrolmentDecision> enrolmentDecisions = new ArrayList<>();
    private List<Student> allStudents = new ArrayList<>();
    private List<GradeRecord> failedRecords = new ArrayList<>();
    private List<String> recoveryPlans = new ArrayList<>();
    private List<Milestone> milestones = new ArrayList<>();

    public CourseRecoveryPage() {
        initComponents();
        
        // Initialize the repository using Config paths
        this.repository = new TextDataRepository(
            Config.STUDENTS_FILE, 
            Config.MODULES_FILE, 
            Config.GRADES_FILE, 
            Config.ENROLMENT_FILE,
            Config.COURSES_FILE
        );
        
        refreshData();
        
        setupTableListener();
        
        autoResizeTable(studentTable);
        
        generatePieChart();
        
        String placeholder = "Enter TP Number";
        searchBar.setText(placeholder);
        searchBar.setForeground(java.awt.Color.GRAY); // Gray color looks like a placeholder

        // 2. Add the Focus Listener
        searchBar.addFocusListener(new java.awt.event.FocusListener() {

            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // If the text is the placeholder, clear it and turn the text black
                if (searchBar.getText().equals(placeholder)) {
                    searchBar.setText("");
                    searchBar.setForeground(java.awt.Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                // If the user didn't type anything (empty), put the placeholder back
                if (searchBar.getText().isEmpty()) {
                    searchBar.setForeground(java.awt.Color.GRAY);
                    searchBar.setText(placeholder);
                }
            }
         });
    }

    private void autoResizeTable(javax.swing.JTable table){
        table.setRowHeight(30);
        for(int column = 0; column < table.getColumnCount(); column++){
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();
            for(int row = 0; row < table.getRowCount(); row++){
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                java.awt.Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                if(preferredWidth >= maxWidth){
                    preferredWidth = maxWidth;
                    break;
                }
            }
            tableColumn.setPreferredWidth(preferredWidth + 20); // Padding
        }
    }
    
    private void refreshData() {
        try {
            // Load all data into memory
            allStudents = repository.loadStudents();
            recoveryPlans = repository.loadRecoveryPlans();
            milestones = repository.loadMilestones();
            enrolmentDecisions = repository.loadEnrolmentDecisions();
            
            // Filter for failed records (GP < 2.0)
            List<GradeRecord> allGrades = repository.loadGradeRecords();
            failedRecords.clear();
            for (GradeRecord g : allGrades) {
                if (g.getGradePoint() < 2.0) {
                    failedRecords.add(g);
                }
            }
            loadTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTable() {
        String[] columns = {"Student ID", "Name", "Intake", "Level", "Year", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Find rejected students
        Set<String> failedIDs = new HashSet<>();
        for (EnrolmentDecision g : enrolmentDecisions){
            if (g.getDecision().equals("Rejected")) {
                failedIDs.add(g.getStudentId());
            }
        }

        for (Student s : allStudents) {
            if (failedIDs.contains(s.getStudentId())) {
                model.addRow(new Object[]{
                    s.getStudentId(), s.getName(), s.getIntakeCode(),
                    s.getLevel(), s.getYear(), s.getEmail()
                });
            }
        }
        studentTable.setModel(model);
    }

    private void setupTableListener() {
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                String sid = studentTable.getValueAt(studentTable.getSelectedRow(), 0).toString();
                loadStudentDetails(sid);
            }
        });
    }
    
    private void searchStudent(){
        String searchId = searchBar.getText().trim();
        TableRowSorter<DefaultTableModel> sorter;
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        sorter = new TableRowSorter<>(model);
        studentTable.setRowSorter(sorter);
        
        
        if (searchId.equals("Enter TP Number")) return;
        
        if (searchId.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!searchId.matches("^TP\\d{6}$")) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Invalid Format! \nStudent ID must be 'TP' followed by 6 digits (e.g., TP123456).", 
                "Invalid TP number", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }   
        else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchId, 0));
        }
        
        if (studentTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Student was not found", 
                "No Results", 
                JOptionPane.INFORMATION_MESSAGE);

            sorter.setRowFilter(null); 
            searchBar.setText("Enter TP Number");
        }
    }

    private void loadStudentDetails(String sid) {
        // Select student details
        Student sel = null;
        for(Student s : allStudents) {
            if(s.getStudentId().equals(sid)) sel = s;
        }
        if (sel != null) {
            studentTextField.setText(sel.getName());
            intakeTextField.setText(sel.getIntake());
            programTextField.setText(sel.getProgramme());
        }

        // Load Failed Modules
        failedModuleComboBox.removeAllItems();
        failedModuleComboBox.addItem("--Select a Module--");
        for (GradeRecord g : failedRecords) {
            if (g.getStudentId().equals(sid)) {
                failedModuleComboBox.addItem(g.getModuleCode());
            }
        }
        recommendationTextArea.setText(""); // Clear previous text
    }
    
    private void loadExistingRecommendation() {
        if (studentTable.getSelectedRow() == -1 || failedModuleComboBox.getSelectedItem() == null) return;
        
        String sid = studentTable.getValueAt(studentTable.getSelectedRow(), 0).toString();
        String mod = failedModuleComboBox.getSelectedItem().toString();
        
        recommendationTextArea.setText("");
        for (String line : recoveryPlans) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3 && parts[0].equals(sid) && parts[1].equals(mod)) {
                recommendationTextArea.setText(parts[2]);
                break;
            }
        }
    }
    
    private void loadMilestones() {
        String[] columns = {"Week", "Task", "Progress"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        if (studentTable.getSelectedRow() == -1 || failedModuleComboBox.getSelectedItem() == null) return;
        
        String sid = studentTable.getValueAt(studentTable.getSelectedRow(), 0).toString();
        String mod = failedModuleComboBox.getSelectedItem().toString();
        
        for (Milestone line : milestones){
            if (line.getStudentID().equals(sid) && line.getCourse().equals(mod)){
                 model.addRow(new Object[]{
                     line.getStudyWeek(),
                     line.getTask(),
                     line.getProgress()
                });
            }
        }
        milestoneTable.setModel(model);
    }
        
    private PieDataset createPieDataset(){
        int approved = 0;
        int rejected = 0;
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (EnrolmentDecision students : enrolmentDecisions) {
            if(students.getDecision().equals("Approved")) approved++;
            if(students.getDecision().equals("Rejected")) rejected++;
        }
        
        dataset.setValue("Approved Students (" + approved + ")", approved);
        dataset.setValue("Rejected Students (" + rejected + ")", rejected);
        
        return dataset;
    }
    
    private JFreeChart createChart(PieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
            "Student Performance",  // Chart title
            dataset,                       // Data
            true,                          // Include legend
            true,                          // Tooltips
            false                          // URLs
        );

        return chart;
    }
    
    private void generatePieChart(){
        PieDataset dataset = createPieDataset();
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        pieChartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        pieChartPanel.setLayout(new BorderLayout());
        pieChartPanel.add(chartPanel, BorderLayout.CENTER);
        pieChartPanel.revalidate();
        pieChartPanel.repaint();
    }
    /*
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        pageName = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        searchBar = new javax.swing.JTextField();
        findBtn = new javax.swing.JButton();
        studentTablePane = new javax.swing.JScrollPane();
        studentTable = new javax.swing.JTable();
        pieChartPanel = new javax.swing.JPanel();
        refreshBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        recommendationTextArea = new javax.swing.JTextArea();
        studentTextField = new javax.swing.JLabel();
        intakeTextField = new javax.swing.JLabel();
        programTextField = new javax.swing.JLabel();
        saveBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        failedModuleComboBox = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        milestoneTable = new javax.swing.JTable();
        manageMilestonesBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pageName.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        pageName.setText("Course Recovery");

        searchBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBarActionPerformed(evt);
            }
        });

        findBtn.setText("Find");
        findBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findBtnActionPerformed(evt);
            }
        });

        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        studentTable.setEditingColumn(0);
        studentTable.setEditingRow(0);
        studentTablePane.setViewportView(studentTable);

        pieChartPanel.setMaximumSize(new java.awt.Dimension(0, 279));
        pieChartPanel.setName(""); // NOI18N

        javax.swing.GroupLayout pieChartPanelLayout = new javax.swing.GroupLayout(pieChartPanel);
        pieChartPanel.setLayout(pieChartPanelLayout);
        pieChartPanelLayout.setHorizontalGroup(
            pieChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pieChartPanelLayout.setVerticalGroup(
            pieChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 283, Short.MAX_VALUE)
        );

        refreshBtn.setText("Refresh Table");
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pieChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(studentTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findBtn)
                    .addComponent(refreshBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pieChartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Program");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Student Name");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Intake");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Recommendation");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Failed Modules");

        recommendationTextArea.setColumns(20);
        recommendationTextArea.setLineWrap(true);
        recommendationTextArea.setRows(5);
        recommendationTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(recommendationTextArea);

        saveBtn.setText("Save");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        failedModuleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                failedModuleComboBoxActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Milestone");

        milestoneTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(milestoneTable);

        manageMilestonesBtn.setText("Manage Milestones");
        manageMilestonesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageMilestonesBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(manageMilestonesBtn))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(24, 24, 24))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(studentTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                            .addComponent(programTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(intakeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(68, 68, 68)
                        .addComponent(saveBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearBtn))
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(failedModuleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(studentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(programTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(intakeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(failedModuleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn)
                    .addComponent(clearBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manageMilestonesBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backBtn.setText("Back");
        backBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pageName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(backBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pageName, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void searchBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBarActionPerformed

    }//GEN-LAST:event_searchBarActionPerformed

    private void findBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findBtnActionPerformed
        searchStudent();
    }//GEN-LAST:event_findBtnActionPerformed

    private void failedModuleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_failedModuleComboBoxActionPerformed
        loadExistingRecommendation();
        loadMilestones();
    }//GEN-LAST:event_failedModuleComboBoxActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        int option = JOptionPane.showConfirmDialog(null, "Clear all the text?", "Clear Text", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION){
            recommendationTextArea.setText("");
        }
    }//GEN-LAST:event_clearBtnActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        if (studentTable.getSelectedRow() == -1 || failedModuleComboBox.getSelectedItem() == null || failedModuleComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Select a student and module first.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(null, "Save recommendation?", "Save", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sid = studentTable.getValueAt(studentTable.getSelectedRow(), 0).toString();
        String mod = failedModuleComboBox.getSelectedItem().toString();
        String text = recommendationTextArea.getText().trim();//replace("\n", " ")
        
        // Check for "|" symbol
        if (text.contains("|")) {
            JOptionPane.showMessageDialog(this, 
                "The recommendation cannot contain the '|' symbol.", 
                "Invalid Character", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Update memory
        boolean found = false;
        for (int i = 0; i < recoveryPlans.size(); i++) {
            String[] parts = recoveryPlans.get(i).split("\\|");
            if (parts.length >= 2 && parts[0].equals(sid) && parts[1].equals(mod)) {
                recoveryPlans.set(i, sid + "|" + mod + "|" + text);
                found = true;
                break;
            }
        }
        if (!found) {
            recoveryPlans.add(sid + "|" + mod + "|" + text);
        }
        
        // Save to File
        try {
            repository.saveRecoveryPlans(recoveryPlans);
            JOptionPane.showMessageDialog(this, "Saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveBtnActionPerformed

    private void manageMilestonesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageMilestonesBtnActionPerformed
        new ManageMilestone().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_manageMilestonesBtnActionPerformed

    private void backBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBtnActionPerformed
        new AO_dashboard().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_backBtnActionPerformed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
//        TableRowSorter<DefaultTableModel> sorter;
//        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
//        sorter = new TableRowSorter<>(model);
//        
//        searchBar.setText("Enter TP Number");
//        searchBar.setForeground(Color.GRAY);
//        sorter.
        searchBar.setText("Enter TP Number");
        searchBar.setForeground(java.awt.Color.GRAY);
        searchStudent();
    }//GEN-LAST:event_refreshBtnActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]){
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
            java.util.logging.Logger.getLogger(CourseRecoveryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CourseRecoveryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CourseRecoveryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CourseRecoveryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CourseRecoveryPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backBtn;
    private javax.swing.JButton clearBtn;
    private javax.swing.JComboBox<String> failedModuleComboBox;
    private javax.swing.JButton findBtn;
    private javax.swing.JLabel intakeTextField;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton manageMilestonesBtn;
    private javax.swing.JTable milestoneTable;
    private javax.swing.JLabel pageName;
    private javax.swing.JPanel pieChartPanel;
    private javax.swing.JLabel programTextField;
    private javax.swing.JTextArea recommendationTextArea;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JButton saveBtn;
    private javax.swing.JTextField searchBar;
    private javax.swing.JTable studentTable;
    private javax.swing.JScrollPane studentTablePane;
    private javax.swing.JLabel studentTextField;
    // End of variables declaration//GEN-END:variables
}
