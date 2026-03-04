package repository;

import model.*;
import java.util.List;

/**
 * The abstract contract for Data Access Objects (DAO) in the Course Recovery System.
 * <p>
 * This interface defines the mandatory operations for loading and saving academic data,
 * decoupling the high-level business logic (Dashboards) from the low-level storage details.
 * Any class implementing this interface must provide the logic for these CRUD operations.
 * </p>
 */
public interface DataRepository {
    // --- Loaders ---
    /** Loads the complete list of students. */
    List<Student> loadStudents() throws Exception;
    /** Retrieves the list of all academic modules. */
    List<ModuleRow> loadModules() throws Exception;
    /** Loads historical grade records for all students. */
    List<GradeRecord> loadGradeRecords() throws Exception;
    /** Retrieves the log of past enrolment decisions. */
    List<EnrolmentDecision> loadEnrolmentDecisions() throws Exception;
    /** Loads the list of recovery plans. */
    List<String> loadRecoveryPlans() throws Exception;
    /** Loads all milestone tasks for recovery plans. */
    List<Milestone> loadMilestones() throws Exception;
    /** Loads the list of lecturers/instructors. */
    List<String> loadLecture() throws Exception;

    // --- Savers ---
    /** Overwrites the recovery plans file with new data. */
    boolean saveRecoveryPlans(List<String> lines) throws Exception;
    /** Appends a single new recovery plan to the file. */
    boolean appendRecoveryPlan(String line) throws Exception;
    /** Overwrites the milestones file with a new list. */
    boolean saveMilestones(List<Milestone> milestones) throws Exception;
    /** Appends a single new milestone task to the file. */
    boolean appendMilestone(Milestone milestone) throws Exception;
    /** Saves a new enrolment decision (Approved/Rejected) to the file. */
    boolean saveEnrolmentDecision(EnrolmentDecision decision) throws Exception;
}