import java.math.BigDecimal;

/**
 * Defines what operations to do in a bank
 */
public interface Bank {

    /**
     * Make a deposit
     *
     * @param account The account
     * @param money   How much you want to dispose
     * @throws IllegalArgumentException If the client is unknown from this bank, the account is unknown from this client
     *                                  or the money is negative
     */
    void deposit(BankClient client, BankAccount account, BigDecimal money);

    /**
     * Make a withdrawal
     *
     * @param account The account
     * @param money   How much you want to withdrawal
     * @throws IllegalArgumentException If the client is unknown from this bank, the account is unknown from this client
     *                                  or the money is negative
     * @throws IllegalStateException    If the amount of the account is insufficient in comparison to the wanted amount
     */
    void withdrawal(BankClient client, BankAccount account, BigDecimal money);

    /**
     * Create an account
     *
     * @param client the client who wants to create an account
     * @return A new account
     * @throws IllegalArgumentException If the client is unknown from this bank
     */
    BankAccount createAccount(BankClient client);

    /**
     * Create a new empty client
     *
     * @return a new client
     */
    BankClient createClient();
}
