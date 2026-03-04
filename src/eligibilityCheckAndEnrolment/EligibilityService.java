/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package eligibilityCheckAndEnrolment;

import java.util.*;
import model.Student;
import model.GradeRecord;
import model.EligibilityResult;

/**
 * Contains the core business logic for processing student eligibility.
 * This class is responsible for calculating CGPA, counting failed modules,
 * and determining the final "Eligible" or "Ineligible" status.
 */
public class EligibilityService {
    public static final double CGPA_THRESHOLD = 2.00;
    public static final int    MAX_FAILED_ALLOWED = 3;
    public static final double PASS_GP = 2.00;
    
    // Set of grades that count as a failure
    private static final Set<String> FAIL_TOKENS = Set.of("F","FAIL","U","UNSATISFACTORY","ABS","AB");

    /**
     * Evaluates a single student's academic performance against the intake rules.
     * @param s The Student profile object (contains Intake and Programme info).
     * @param grades The complete history of GradeRecords for this student.
     * @return An EligibilityResult object containing the calculated CGPA, Fail Count, and Status.
     */
    public EligibilityResult evaluate(Student s, List<GradeRecord> grades) {
        String targetIntake = (s.getIntake() == null) ? "" : s.getIntake().trim();

        // Filter for the latest attempt per module
        Map<String, GradeRecord> latest = new HashMap<>();
        for (GradeRecord g : grades) {
            String gi = (g.getIntake() == null) ? "" : g.getIntake().trim();
            
            // Only consider grades matching the Student's current intake
            if (!gi.equalsIgnoreCase(targetIntake)) continue;

            GradeRecord cur = latest.get(g.getModuleCode());
            // Logic: If we haven't seen this module yet OR if this is a later attempt (e.g. Attempt 2 vs 1)
            // We store this record as the "current best" for calculation
            if (cur == null || g.getAttemptNo() > cur.getAttemptNo()) {
                latest.put(g.getModuleCode(), g);
            }
        }

        // Calculate Grade Points and Credit Hours
        double gpSum = 0.0; 
        int creditSum = 0; 
        int failed = 0;
        
        for (GradeRecord g : latest.values()) {
            int ch = g.getCreditHours();
            double gp = g.getGradePoint();
            String gt = (g.getGrade() == null ? "" : g.getGrade().trim().toUpperCase());
            
            boolean textFail = FAIL_TOKENS.contains(gt);
            if (gp < PASS_GP || textFail) failed++;

            gpSum += gp * ch;
            creditSum += ch;
        }

        double cgpa = (creditSum == 0) ? 0.0 : Math.round((gpSum / creditSum) * 100.0) / 100.0;

        // Determine Status
        List<String> reasons = new ArrayList<>();
        if (cgpa < CGPA_THRESHOLD) reasons.add("CGPA below " + String.format("%.2f", CGPA_THRESHOLD));
        if (failed > MAX_FAILED_ALLOWED) reasons.add("Failed modules exceed " + MAX_FAILED_ALLOWED);

        var status = reasons.isEmpty()
            ? EligibilityResult.Status.ELIGIBLE
            : EligibilityResult.Status.NOT_ELIGIBLE;

        return new EligibilityResult(s, cgpa, failed, status, latest, reasons);
    }
}