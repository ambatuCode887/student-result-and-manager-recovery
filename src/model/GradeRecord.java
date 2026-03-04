/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


/**
 * Represents a single academic grade record for a specific module.
 * <p>
 * This class maps a Student ID to a Module Code and stores the 
 * performance metrics (Grade, Grade Point) achieved in a specific semester.
 * </p>
 */
public class GradeRecord {
    private final String studentId;
    private final String intake;
    private final String moduleCode;
    private final String semester;
    private final int attemptNo;
    private final String grade;
    private final double gradePoint;
    private final int creditHours;

    /**
     * Creates a new GradeRecord.
     * @param studentId The ID of the student who took the module.
     * @param intake The intake code associated with this attempt.
     * @param moduleCode The unique code of the module (e.g., CT038-3-2).
     * @param semester The semester in which the module was taken.
     * @param attemptNo The attempt count (1 for first try, 2 for resit, etc.).
     * @param grade The letter grade obtained (e.g., "A", "F").
     * @param gradePoint The numerical point value (0.0 - 4.0).
     * @param creditHours The credit value of the module.
     */
    public GradeRecord(String studentId, String intake, String moduleCode, String semester,
                       int attemptNo, String grade, double gradePoint, int creditHours) {
        this.studentId = studentId;
        this.intake = intake;
        this.moduleCode = moduleCode;
        this.semester = semester;
        this.attemptNo = attemptNo;
        this.grade = grade;
        this.gradePoint = gradePoint;
        this.creditHours = creditHours;
    }

    // Getters - These methods provide read-only access to the grade record's immutable properties.
    public String getStudentId() { return studentId; }
    public String getIntake() { return intake; }
    public String getModuleCode() { return moduleCode; }
    public String getSemester() { return semester; }
    public int getAttemptNo() { return attemptNo; }
    public String getGrade() { return grade; }
    public double getGradePoint() { return gradePoint; }
    public int getCreditHours() { return creditHours; }
}