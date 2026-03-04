/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.List;
import java.util.Map;

/**
 * A Data Transfer Object (DTO) that encapsulates the outcome of an eligibility check.
 * <p>
 * This class holds the calculated academic metrics (CGPA, Failed Count) and 
 * the derived status (Eligible/Ineligible), along with any reasons for rejection.
 * </p>
 */
public class EligibilityResult {
    public enum Status { ELIGIBLE, NOT_ELIGIBLE }

    private final Student student;
    private final double cgpa;
    private final int failedCount;
    private final Status status;
    private final Map<String, GradeRecord> latestByModule; 
    private final List<String> reasons;
    
    /**
     * Constructs the result of an eligibility evaluation.
     * @param student The student profile being evaluated.
     * @param cgpa The calculated Cumulative Grade Point Average.
     * @param failedCount The total number of unique modules failed.
     * @param status The determined status (ELIGIBLE or NOT_ELIGIBLE).
     * @param latestByModule A map of the most recent attempts for each module.
     * @param reasons A list of strings explaining why a student is ineligible.
     */
    public EligibilityResult(Student student, double cgpa, int failedCount, Status status,
                             Map<String, GradeRecord> latestByModule, List<String> reasons) {
        this.student = student;
        this.cgpa = cgpa;
        this.failedCount = failedCount;
        this.status = status;
        this.latestByModule = latestByModule;
        this.reasons = reasons;
    }
    
    // Getters - These methods provide read-only access to the calculation results.
    public Student getStudent() {
        return student;
    }
    public double getCgpa() {
        return cgpa;
    }
    public int getFailedCount() {
        return failedCount;
    }
    public Status getStatus() {
        return status;
    }
    public Map<String, GradeRecord> getLatestByModule() {
        return latestByModule;
    }
    public List<String> getReasons() {
        return reasons;
    }
}
    
