import oodj_user_management.UI_Login;

public class Run_main {
    /**
        * 1.run UI_Authentication.java to login
        * 2.run .java to control UM dashboard
        * 3.if select UI_AccManagement go adduser,deactive user and change passwd
        * 
     * @param args
        */
    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UI_Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        // Launch the login page
        java.awt.EventQueue.invokeLater(() -> new UI_Login().setVisible(true));
    }
}
