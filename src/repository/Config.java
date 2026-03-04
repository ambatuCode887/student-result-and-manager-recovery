/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

/**
 * Global configuration constants for the application.
 * <p>
 * Centralizes the file paths for all data stores (students, grades, milestones)
 * to ensure consistency across the application.
 * </p>
 */
public final class Config {
    public static final String STUDENTS_FILE = "data/students.txt";
    public static final String MODULES_FILE  = "data/modules.txt";
    public static final String GRADES_FILE   = "data/gradebook.txt";
    public static final String ENROLMENT_FILE = "runtime/enrolment_decisions.txt";
    public static final String RECOVERY_PLANS = "runtime/recovery_plans.txt";
    public static final String MILESTONES_FILE = "runtime/studentMilestone.txt";
    public static final String COURSES_FILE = "runtime/course_information.txt";
    private Config() {}
}
