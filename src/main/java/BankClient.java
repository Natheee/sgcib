import java.util.List;

/**
 * Defines what operations to do with an client
 */
public interface BankClient {

    /**
     * Give all the accounts of this client
     * @return A list containing all of the clients accounts
     */
    List<BankAccount> getAccounts();
}
