package CourseRecoverySystem.Milestone;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import model.Milestone;
import model.GradeRecord;
import repository.Config;
import repository.TextDataRepository;
import repository.DataRepository;

public abstract class MilestoneBase extends JFrame {
    
    protected abstract void performOperation();
    
    //implementing interface and encapsulation
    protected final DataRepository repository;

    public MilestoneBase() {
        this.repository = new TextDataRepository(
            Config.STUDENTS_FILE, 
            Config.MODULES_FILE, 
            Config.GRADES_FILE, 
            Config.ENROLMENT_FILE,
            Config.COURSES_FILE
        );
    }

    //this read the file that have been saved by the courserecoverysystem to get
    //only student that had failed module
    protected List<String> getFailedStudentIDs() {
        Set<String> failedIDs = new HashSet<>();

        try {
            List<GradeRecord> grades = repository.loadGradeRecords();
            for (GradeRecord g : grades) {
                if (g.getGradePoint() < 2.0) {
                    failedIDs.add(g.getStudentId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(failedIDs);
    }

    //this is mainly for the dropdown on the add milestone so that it can align with the student ID and their fail courses
    protected void loadFailedStudentsToDropdown(JComboBox<String> dropdown) {
        dropdown.removeAllItems();
        dropdown.addItem("Select a failed student...");
        
        List<String> failedIDs = getFailedStudentIDs();
        for (String id : failedIDs) {
            dropdown.addItem(id);
        }
    }
    //same purpose as the above show the student failed course depend on which TP number user choose
    protected void showFailedCourses(String studentID, JComboBox<String> subjectDropdown) {
        subjectDropdown.removeAllItems();
        subjectDropdown.addItem("Select a subject...");
        
        try {
            List<GradeRecord> grades = repository.loadGradeRecords();
            List<String> addedCourses = new ArrayList<>();
            
            for (GradeRecord g : grades) {
                if (g.getStudentId().equalsIgnoreCase(studentID) && g.getGradePoint() < 2.0) {
                    if (!addedCourses.contains(g.getModuleCode())) {
                        subjectDropdown.addItem(g.getModuleCode());
                        addedCourses.add(g.getModuleCode());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this is for the editMilestone so when the user updated it will append the new content to the txt file
    protected void appendMilestoneToFile(String course, String studyWeek, String task, String studentID, String progress) {
        try {
            Milestone m = new Milestone(course, studyWeek, task, studentID, progress);
            repository.appendMilestone(m);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }
    //this is the validation for the addmilestone to avoid duplication of the same content
    protected boolean milestoneExists(String course, String week, String task, String id) {
        try {
            List<Milestone> list = repository.loadMilestones();
            for (Milestone m : list) {
                if (m.getCourse().equalsIgnoreCase(course) && 
                    m.getStudyWeek().equalsIgnoreCase(week) &&
                    m.getTask().equalsIgnoreCase(task) &&
                    m.getStudentID().equalsIgnoreCase(id)) {
                    return true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    //delete functions
    protected boolean deleteByStudentID(String studentID) {
        try {
            List<Milestone> list = repository.loadMilestones();
            List<Milestone> kept = list.stream()
                .filter(m -> !m.getStudentID().equalsIgnoreCase(studentID))
                .collect(Collectors.toList());
            
            if (kept.size() < list.size()) {
                return repository.saveMilestones(kept);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage());
        }
        return false;
    }
    
    //a functions that allow us to load the content to the table
    protected void loadFromFileToTable(DefaultTableModel model) {
    try {
        model.setRowCount(0);
        List<Milestone> list = repository.loadMilestones();
        for (Milestone m : list) {
            model.addRow(new Object[]{
                m.getStudentID(),
                m.getCourse(),
                m.getStudyWeek(),
                m.getTask(),
                m.getProgress() != null ? m.getProgress() : "Ongoing" 
            });
        }
    } catch (Exception e) {
        System.err.println("Error loading table: " + e.getMessage());
    }
}
    //on the edit and delete theres a textfield allow us to search so this is the function
    protected void searchByStudentID(String searchID, DefaultTableModel model) {
    try {
        model.setRowCount(0);
        List<Milestone> list = repository.loadMilestones();
        boolean found = false;
        for (Milestone m : list) {
            if (m.getStudentID().equalsIgnoreCase(searchID)) {
                model.addRow(new Object[]{
                    m.getStudentID(), 
                    m.getCourse(), 
                    m.getStudyWeek(), 
                    m.getTask(),
                    m.getProgress() != null ? m.getProgress() : "Ongoing"
                });
                found = true;
            }
        }
        if (!found) JOptionPane.showMessageDialog(this, "No record found.");
    } catch (Exception e) { e.printStackTrace(); }
}

    protected void saveTableToFile(DefaultTableModel model) {
        
    }
    //navigation
    protected void navigateToManageMilestone() {
        ManageMilestone manageButton = new ManageMilestone();
        manageButton.setVisible(true);
        manageButton.setLocationRelativeTo(this);
        this.dispose();
    }
    //validation
    protected boolean validateAllFields(String... fields) {
        for (String field : fields) {
            if (field == null || field.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
    //validation for the student TP number
    protected boolean validateStudentID(String studentID) {
        if (studentID == null || !studentID.matches("^TP\\d{6}$")) {
            JOptionPane.showMessageDialog(this, "Invalid ID format (TPxxxxxx).", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    //clear the field after submit or add content
    protected void clearFields(JTextField... fields) {
        for (JTextField field : fields) if (field != null) field.setText("");
    }
}