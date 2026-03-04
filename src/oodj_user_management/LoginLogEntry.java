package oodj_user_management;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LoginLogEntry implements Serializable {
    
    private static final long serialVersionUID = 1L; 
    
    private String userId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;

    public LoginLogEntry(String userId, LocalDateTime loginTime) {
        this.userId = userId;
        this.loginTime = loginTime;
        this.logoutTime = null;
    }
    
    // Getter
    public String getUserId() {
        return userId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    // Setter
    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    @Override
    public String toString() {
        return "Log [userId='" + userId + "', loginTime=" + loginTime +
               ", logoutTime=" + (logoutTime != null ? logoutTime : "N/A") +
               "]";
    }
}