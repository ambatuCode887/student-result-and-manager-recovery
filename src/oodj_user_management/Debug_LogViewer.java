package oodj_user_management;

import java.util.List;

public class Debug_LogViewer {

    public static void main(String[] args) {
        UI_Login loginApp = new UI_Login(); 
        
        System.out.println("--- Reading Login Logs ---");
        
        // Call the 'readAllLogs' method in UI_Login
        List<LoginLogEntry> logs = loginApp.readAllLogs();
        
        if (logs.isEmpty()) {
            System.out.println("No logs found or log file is empty/corrupted.");
            return;
        }
        
        // Print each log entry
        for (LoginLogEntry log : logs) {
            System.out.println(log.toString());
        }
        System.out.println("--- Reading Complete. Total entries: " + logs.size() + " ---");
    }
}
