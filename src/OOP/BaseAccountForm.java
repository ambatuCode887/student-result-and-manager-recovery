package OOP;

import javax.swing.JOptionPane;
import oodj_user_management.UM_OOP;

public abstract class BaseAccountForm extends javax.swing.JFrame {
    protected final UM_OOP accountHandler = new UM_OOP();
    protected String[] userData;

    public BaseAccountForm() {
        // Constructor for JFrame, no special initialization here.
        // Subclasses will call initComponents() and then custom initialization.
    }

    protected abstract void clearAccInfoDisplay();
    protected abstract void displayAccountInfo(String[] data);

    protected boolean checkUser(String userId) {
        clearAccInfoDisplay();
        
        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a UserID to check.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        this.userData = accountHandler.findAccountByID(userId);
        
        if (this.userData != null && this.userData.length >= 7) {
            displayAccountInfo(this.userData);
            JOptionPane.showMessageDialog(this, "User data loaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(this, 
                "UserID '" + userId + "' not found.", 
                "User Not Found", JOptionPane.WARNING_MESSAGE);
            this.userData = null;
            return false;
        }
    }
}
