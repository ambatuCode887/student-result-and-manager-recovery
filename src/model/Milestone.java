package model;

/**
 * Represents a specific task or goal within a Student's Course Recovery Plan.
 * <p>
 * Milestones are actionable items (e.g., "Submit Assignment") assigned to 
 * students who have failed specific modules, used to track their recovery progress.
 * </p>
 */
public class Milestone {
    private String course;
    private String studyWeek;
    private String task;
    private String studentID;
    private String progress;
    
    /**
     * Creates a new Milestone entry.
     * @param course The module associated with this recovery task.
     * @param studyWeek The target week number for completion (e.g., "Week 5").
     * @param task A description of the activity required.
     * @param studentID The ID of the student assigned to this milestone.
     * @param progress The current status of the task (e.g., "Pending", "Completed").
     */
    public Milestone(String course, String studyWeek, String task, String studentID, String progress) {
        this.course = course;
        this.studyWeek = studyWeek;
        this.task = task;
        this.studentID = studentID;
        this.progress = progress;
    }

    // Getters - These methods provide read-only access to the milestone's properties.
    public String getCourse() { return course; }
    public String getStudyWeek() { return studyWeek; }
    public String getTask() { return task; }
    public String getStudentID() { return studentID; }
    public String getProgress() { return progress; } 
    
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s", studentID, course, studyWeek, task, progress);
    }
}