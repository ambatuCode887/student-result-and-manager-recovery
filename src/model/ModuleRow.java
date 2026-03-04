/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Represents the metadata for an academic module offered by the institution.
 * <p>
 * Stores static information such as the module title, credit value, and
 * level, which are used for validating course details and calculating CGPA.
 * </p>
 */
public class ModuleRow {
    private final String code;
    private final String name;
    private final int creditHours;
    private final String programme;
    private final String level;

    /**
     * Initializes a new Module definition.
     * @param code The unique alphanumeric code for the module.
     * @param name The descriptive title of the subject.
     * @param creditHours The credit value assigned to this module.
     * @param programme The programme code this module belongs to.
     * @param level The academic level (e.g., "Level 1").
     */
    public ModuleRow(String code, String name, int creditHours, String programme, String level) {
        this.code = code;
        this.name = name;
        this.creditHours = creditHours;
        this.programme = programme;
        this.level = level;
    }
    
    // Getters - These methods provide read-only access to the module's immutable properties.
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getCreditHours() { return creditHours; }
    public String getProgramme() { return programme; }
    public String getLevel() { return level; }  
}