/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eligibilityCheckAndEnrolment;

import model.EligibilityResult;
import model.GradeRecord;
import model.ModuleRow;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A pop-up dialog that shows the specific grade breakdown for a student.
 */
public class StudentDetailsDialog extends JDialog {

    public StudentDetailsDialog(Frame parent, EligibilityResult result, List<ModuleRow> allModules) {
        super(parent, "Eligibility Details", true);
        
        // Setup Header
        String headerHtml = String.format("<html><b>%s - %s</b><br>Programme: %s &nbsp;&nbsp; Intake: %s"
            + "<br>CGPA: %.2f &nbsp;&nbsp; Failed: %d &nbsp;&nbsp; Status: %s</html>",
            result.getStudent().getStudentId(), result.getStudent().getName(),
            result.getStudent().getProgramme(), result.getStudent().getIntake(),
            result.getCgpa(), result.getFailedCount(),
            (result.getStatus() == EligibilityResult.Status.ELIGIBLE ? "Eligible" : "Not Eligible")
        );

        // Prepare Data for Table
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Code", "Module Name", "Grade", "GP", "Credit", "Attempt"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 3) return Double.class;
                if (c == 4 || c == 5) return Integer.class;
                return String.class;
            }
        };

        List<GradeRecord> list = new ArrayList<>(result.getLatestByModule().values());
        list.sort(Comparator.comparing(GradeRecord::getModuleCode));
        
        for (GradeRecord g : list) {
            String name = allModules.stream()
                .filter(mod -> mod.getCode().equalsIgnoreCase(g.getModuleCode()))
                .map(ModuleRow::getName)
                .findFirst().orElse(g.getModuleCode());
                
            model.addRow(new Object[]{
                g.getModuleCode(), name, g.getGrade(), g.getGradePoint(), g.getCreditHours(), g.getAttemptNo()
            });
        }

        // Setup Table UI
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Configure column width
        int[] dMin  = {100,  150, 40, 40, 40, 40};
        int[] dPref = {120, 300, 50, 50, 50, 50};
        int[] dMax  = {150, 9999, 80, 80, 80, 80}; 

        for(int i=0; i<table.getColumnCount() && i<dPref.length; i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setMinWidth(dMin[i]);
            c.setPreferredWidth(dPref[i]);
            if (dMax[i] < 1000) c.setMaxWidth(dMax[i]);
            
            // Center Alignment for short columns
            if(i != 1) { 
                DefaultTableCellRenderer center = new DefaultTableCellRenderer();
                center.setHorizontalAlignment(SwingConstants.CENTER);
                c.setCellRenderer(center);
            }
        }

        // Setup "Show Failed Only" Filter
        JCheckBox failedOnly = new JCheckBox("Show failed only");
        failedOnly.addActionListener(e -> {
            if (!failedOnly.isSelected()) sorter.setRowFilter(null);
            else sorter.setRowFilter(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    double gp = 0.0;
                    try { gp = Double.parseDouble(entry.getValue(3).toString()); } catch(Exception ex){}
                    return gp < 2.0;
                }
            });
        });

        // Final Layout Assembly
        JLabel hdrLabel = new JLabel(headerHtml);
        hdrLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(hdrLabel, BorderLayout.CENTER);
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkPanel.add(failedOnly);
        topPanel.add(checkPanel, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(750, 350));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeBtn);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(parent);
    }
}