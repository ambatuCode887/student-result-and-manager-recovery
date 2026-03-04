/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * An immutable record of an enrolment decision made by an Academic Officer.
 * <p>
 * This class serves as an audit trail, capturing the final status (Approved/Rejected)
 * assigned to a student and the timestamp of when that decision was finalized.
 * </p>
 */
public class EnrolmentDecision {
    private final String studentId;
    private final String intake;
    private final String decision;
    private final String timestamp;

    /**
     * Records a new enrolment decision.
     * @param studentId The ID of the student being processed.
     * @param intake The intake code for which the decision applies.
     * @param decision The final decision status (e.g., "Approved", "Rejected").
     * @param timestamp The date and time string indicating when the decision was made.
     */
    public EnrolmentDecision(String studentId, String intake, String decision, String timestamp) {
        this.studentId = studentId;
        this.intake = intake;
        this.decision = decision;
        this.timestamp = timestamp;
    }

    // Getters - These methods provide read-only access to the decision's immutable properties.
    public String getStudentId() { return studentId; }
    public String getIntake() { return intake; }
    public String getDecision() { return decision; }
    public String getTimestamp() { return timestamp; }
}
