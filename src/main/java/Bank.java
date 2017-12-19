import java.math.BigDecimal;

/**
 * Defines what operations to do in a bank
 */
public interface Bank {

    /**
     * Make a deposit
     * @param account The account
     * @param money How much you want to dispose
     */
    void deposit(Account account, BigDecimal money);

    /**
     * Make a withdrawal
     * @param account The account
     * @param money How much you want to withdrawal
     */
    void withdrawal(Account account, BigDecimal money);

    /**
     * Create an account
     * @return A new account
     */
    Account createAccount();
}
