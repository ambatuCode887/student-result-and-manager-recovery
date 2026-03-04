package model;

/**
 * Represents a student entity within the Course Recovery System.
 * <p>
 * This class is designed to be immutable to ensure data integrity. 
 * It holds essential profile information such as the unique Student ID, 
 * enrolled programme, and intake code.
 * </p>
 */
public class Student {
    private final String studentId;
    private final String name;
    private final String programme;
    private final String intake;
    private final String intakeCode;
    private final String level;
    private final int year;
    private final String status;
    private final String email;
    
    /**
     * Constructs a new Student instance with the specified profile details.
     * @param studentId The unique identifier for the student (e.g., TP012345).
     * @param name The full legal name of the student.
     * @param programme The academic programme code (e.g., APU2F2402CS).
     * @param intake The specific intake code associated with the student.
     */
    public Student(String studentId, String name, String programme, String intake,
                   String intakeCode, String level, int year, String status, String email) {
        this.studentId = studentId;
        this.name = name;
        this.programme = programme;
        this.intake = intake;
        this.intakeCode = intakeCode;
        this.level = level;
        this.year = year;
        this.status = status;
        this.email = email;
    }
    
    // Getters - These methods provide read-only access to the student's immutable properties.
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getProgramme() { return programme; }
    public String getIntake() { return intake; }
    public String getIntakeCode() { return intakeCode; }
    public String getLevel() { return level; }
    public int getYear() { return year; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }

    /**
     * Returns a string representation of the Student object.
     * <p>
     * Overrides the default object method to provide a readable format
     * (ID followed by Name), which is useful for debugging and logging.
     * </p>
     * @return A formatted string (e.g., "TP012345 - John Doe").
     */
    @Override
    public String toString() {
        return studentId + " - " + name;
    }
}