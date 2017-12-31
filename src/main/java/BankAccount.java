import java.math.BigDecimal;

/**
 * Defines what operations to do with an account
 */
public interface BankAccount {

    /**
     * Check balance of the account
     * @return A positive amont
     */
    BigDecimal getBalance();

    /**
     * Get operations
     * @return A string with a description of operations
     */
    String getOperations();
}
