package repository;

import model.*;
import utility.TextDataReader;
import utility.FileStorage;
import java.util.*;

/**
 * A concrete implementation of the DataRepository interface using flat text files (.txt).
 * This class handles the specific low-level logic of parsing delimited strings (CSV/Pipe)
 * into Java Objects.
 */
public class TextDataRepository implements DataRepository {

    private final String studentsFile;
    private final String modulesFile;
    private final String gradesFile;
    private final String enrollmentFile;
    private final String coursesFile;

    /**
     * Initializes the repository with specific file paths.
     * @param studentsFile Path to the students data file.
     * @param modulesFile Path to the modules data file.
     * @param gradesFile Path to the grade history file.
     * @param enrollmentFile Path to the enrolment decisions audit log.
     * @param coursesFile Path to the course information file.
     */
    public TextDataRepository(String studentsFile, String modulesFile, String gradesFile, String enrollmentFile, String coursesFile) {
        this.studentsFile = studentsFile;
        this.modulesFile = modulesFile;
        this.gradesFile = gradesFile;
        this.enrollmentFile = enrollmentFile;
        this.coursesFile = coursesFile;
    }

    /**
     * Reads the students.txt file and converts it into Student objects.
     * Parses fields including ID, Name, Programme, and Intake Code.
     * @return A list of Student objects found in the file.
     * @throws Exception If the file is missing or cannot be read.
     */
    @Override
    public List<Student> loadStudents() throws Exception {
        List<Student> out = new ArrayList<>();
        List<Map<String,String>> rows = TextDataReader.readData(studentsFile);
        for (Map<String,String> r : rows) {
            String id = r.getOrDefault("student_id", r.getOrDefault("studentId", ""));
            String name = r.getOrDefault("name", "");
            String programme = r.getOrDefault("programme", "");
            String intake = r.getOrDefault("intake", "");
            String intakeCode = r.getOrDefault("intake_code", "");
            String level = r.getOrDefault("level", "");
            int year = 0;
            try { year = Integer.parseInt(r.getOrDefault("year", "0")); } catch (Exception e){}
            String status = r.getOrDefault("status", "");
            String email = r.getOrDefault("email", "");
            
            if (!id.isEmpty()) {
                out.add(new Student(id, name, programme, intake, intakeCode, level, year, status, email));
            }
        }
        return out;
    }

    /**
     * Reads the modules.txt file to get course metadata.
     * Extracts Module Code, Name, and Credit Hours for calculation purposes.
     * @return A list of ModuleRow objects.
     * @throws Exception If the file read fails.
     */
    @Override
    public List<ModuleRow> loadModules() throws Exception {
        List<ModuleRow> out = new ArrayList<>();
        List<Map<String,String>> rows = TextDataReader.readData(modulesFile);
        for (Map<String,String> r : rows) {
            String code = r.getOrDefault("module_code", r.getOrDefault("code", ""));
            String name = r.getOrDefault("module_name", r.getOrDefault("name", ""));
            int credit = 0;
            try { credit = Integer.parseInt(r.getOrDefault("credit_hours", "0")); } catch (Exception e){}
            String prog = r.getOrDefault("programme", "");
            String level = r.getOrDefault("level", "");
            
            if (!code.isEmpty()) {
                out.add(new ModuleRow(code, name, credit, prog, level));
            }
        }
        return out;
    }

    /**
     * Reads the gradebook.txt file.
     * @return A list of GradeRecord objects.
     * @throws Exception If the file read fails.
     */
    @Override
    public List<GradeRecord> loadGradeRecords() throws Exception {
        List<GradeRecord> out = new ArrayList<>();
        List<Map<String,String>> rows = TextDataReader.readData(gradesFile);
        for (Map<String,String> r : rows) {
            String sid = r.getOrDefault("student_id", "");
            String intake = r.getOrDefault("intake", "");
            String mod = r.getOrDefault("module_code", "");
            String sem = r.getOrDefault("semester", "");
            int att = 1;
            try { att = Integer.parseInt(r.getOrDefault("attempt", "1")); } catch (Exception e){}
            String grade = r.getOrDefault("grade", "");
            double gp = 0.0;
            try { gp = Double.parseDouble(r.getOrDefault("grade_point", "0.0")); } catch (Exception e){}
            int ch = 0;
            try { ch = Integer.parseInt(r.getOrDefault("credit_hours", "0")); } catch (Exception e){}
            
            if (!sid.isEmpty() && !mod.isEmpty()) {
                out.add(new GradeRecord(sid, intake, mod, sem, att, grade, gp, ch));
            }
        }
        return out;
    }

    /**
     * Reads the enrolment_decisions.txt file to load the audit log.
     * @return A list of past EnrolmentDecision objects.
     * @throws Exception If the file cannot be accessed.
     */
    @Override
    public List<EnrolmentDecision> loadEnrolmentDecisions() throws Exception {
        List<EnrolmentDecision> out = new ArrayList<>();
        List<Map<String,String>> rows = TextDataReader.readData(enrollmentFile);
        for (Map<String,String> r : rows) {
            String sid = r.getOrDefault("student_id", "");
            String intake = r.getOrDefault("intake", "");
            String dec = r.getOrDefault("decision", "");
            String ts = r.getOrDefault("timestamp", "");
            if (!sid.isEmpty()) {
                out.add(new EnrolmentDecision(sid, intake, dec, ts));
            }
        }
        return out;
    }
    
    /**
     * Reads the raw text lines of recovery plans.
     * @return A list of strings representing the file content.
     * @throws Exception If the file is missing.
     */
    @Override
    public List<String> loadRecoveryPlans() throws Exception {
        return FileStorage.readLinesSkipHeader(Config.RECOVERY_PLANS);
    }
    
    /**
     * Reads the milestones.txt file using a PIPE ("|") delimiter.
     * This special delimiter allows commas to be used within the task descriptions.
     * @return A list of Milestone objects.
     * @throws Exception If the file cannot be parsed.
     */
    @Override
    public List<Milestone> loadMilestones() throws Exception {
        List<Milestone> out = new ArrayList<>();
        
        List<Map<String,String>> rows = TextDataReader.readData(Config.MILESTONES_FILE, "|");
        
        for (Map<String,String> r : rows) {
            String sid = r.getOrDefault("student_id", "");
            String course = r.getOrDefault("module_code", "");
            String week = r.getOrDefault("study_week", "");
            String task = r.getOrDefault("task", "");
            String progress = r.getOrDefault("progress", "Ongoing");
            
            if (!sid.isEmpty()) {
                out.add(new Milestone(course, week, task, sid, progress));
            }
        }
        return out;
    }
    
    /**
     * Extracts a unique list of Instructors from the course information file.
     * @return A sorted list of unique instructor names.
     * @throws Exception If the course file is invalid.
     */
    @Override
    public List<String> loadLecture() throws Exception {
        Set<String> uniqueNames = new HashSet<>();
        List<Map<String, String>> rows = TextDataReader.readData(coursesFile);
        
        for (Map<String, String> r : rows) {
            String name = r.getOrDefault("Instructor", ""); 
            if (!name.isEmpty()) {
                uniqueNames.add(name.trim());
            }
        }
        
        List<String> sortedList = new ArrayList<>(uniqueNames);
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * Overwrites the recovery plans file with a new list of lines.
     * @param lines The complete list of strings to write.
     * @return true if the write operation was successful.
     * @throws Exception If the file access is denied.
     */
    @Override
    public boolean saveRecoveryPlans(List<String> lines) throws Exception {
        String[] header = {"student_id","module_code","recommendations"};
        return FileStorage.writeLinesWithHeader(Config.RECOVERY_PLANS, lines, header);
    }

    /**
     * Appends a new recovery plan entry to the end of the file.
     * @param line The CSV-formatted string to append.
     * @return true if the append was successful.
     * @throws Exception If the file cannot be written to.
     */
    @Override
    public boolean appendRecoveryPlan(String line) throws Exception {
        String header = "student_id,module_code,recommendations";
        return FileStorage.appendLine(Config.RECOVERY_PLANS, line, header);
    }
    
    /**
     * Overwrites the milestones file with a new list of Milestone objects.
     * Converts each object to a pipe-separated string before writing.
     * @param milestones The new list of milestones to save.
     * @return true if the operation succeeded.
     * @throws Exception If the file write fails.
     */
    @Override
    public boolean saveMilestones(List<Milestone> milestones) throws Exception {
        List<String> lines = new ArrayList<>();
        for (Milestone m : milestones) {
            lines.add(m.toFileString());
        }
        String[] header = {"student_id|module_code|study_week|task|progress"}; 
        return FileStorage.writeLinesWithHeader(Config.MILESTONES_FILE, lines, header);
    }

    /**
     * Appends a single new milestone to the milestones.txt file.
     * @param m The Milestone object to append.
     * @return true if the append was successful.
     * @throws Exception If the file cannot be accessed.
     */
    @Override
    public boolean appendMilestone(Milestone m) throws Exception {
        String header = "student_id|module_code|study_week|task|progress";
        return FileStorage.appendLine(Config.MILESTONES_FILE, m.toFileString(), header);
    }

    /**
     * Appends a new enrolment decision to the audit file.
     * Records the student ID, intake, decision status, and current timestamp.
     * @param decision The decision object to record.
     * @return true if successful.
     * @throws Exception If the file write error occurs.
     */
    @Override
    public boolean saveEnrolmentDecision(EnrolmentDecision decision) throws Exception {
        String line = String.format("%s,%s,%s,%s", 
            decision.getStudentId(), 
            decision.getIntake(), 
            decision.getDecision(), 
            decision.getTimestamp()
        );
        String header = "student_id,intake,decision,timestamp";
        return FileStorage.appendLine(Config.ENROLMENT_FILE, line, header);
    }
}