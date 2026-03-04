package oodj_user_management;

import java.util.List;

public interface IAccountManagement {
    List<String[]> readAllAccounts();
    String[] findAccountByID(String userId);
    boolean appendNewAccount(String userId, String username, String password, String role, String question, String answer);
    boolean updateAccount(String[] updatedAccount);
}
