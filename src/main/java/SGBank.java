import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.math.BigDecimal.ZERO;

/**
 * Implementation of a bank
 */
public class SGBank implements Bank {

    /**
     * Format of statements
     */
    private static final String STATEMENT_FORMAT = "%s - %s - %s - %s";
    private static final String WITHDRAWAL_TYPE = "W";
    private static final String DEPOSIT_TYPE = "D";

    /**
     * Date dispenser
     */
    private final DateFactory dateFactory;

    /**
     * Date formatter
     */
    private SimpleDateFormat dateFormat;

    /**
     * Clients managed by ths bank
     */
    private Set<BankClient> clients;

    public SGBank(DateFactory dateFactory) {
        this.dateFactory = dateFactory;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        clients = new HashSet<>();
    }

    /**
     * Check that the given amount is not null, zero or negative
     * @param money The amount
     * @param operation The operation to realize (for the error message)
     */
    private static void checkAmount(BigDecimal money, String operation) {
        if (money == null || money.compareTo(ZERO) == 0) {
            throw new IllegalArgumentException("Unable to make a " + operation + " : Null amount");
        } else if (money.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Unable to make a " + operation + " : Negative amount");
        }
    }

    /**
     * Check that the client is managed by this bank, and that this account belongs to the client
     * @param client The client to check
     * @param account The account to check
     * @param operation The operation to realize (for the error message)
     */
    private void checkClientAndAccount(BankClient client, BankAccount account, String operation) {
        if (!(client instanceof SGBankClient) || !clients.contains(client)){
            throw new IllegalArgumentException("Unable to make a " + operation + " : Unknown client");
        }
        if (!(account instanceof SGBankAccount) || !((SGBankClient) client).hasAccount((SGBankAccount) account)){
            throw new IllegalArgumentException("Unable to make a " + operation + " : Wrong account");
        }

    }


    @Override
    public void deposit(BankClient client, BankAccount account, BigDecimal money) {
        checkAmount(money, "deposit");
        checkClientAndAccount(client,account,"deposit");

        SGBankAccount sgBankAccount = (SGBankAccount) account;
        sgBankAccount.deposit(money);
    }


    @Override
    public void withdrawal(BankClient client, BankAccount account, BigDecimal money) {
        checkAmount(money, "withdrawal");
        checkClientAndAccount(client,account,"withdrawal");

        SGBankAccount sgBankAccount = (SGBankAccount) account;

        if (sgBankAccount.getBalance().compareTo(money) < 0) {
            throw new IllegalStateException("Unable to make a withdrawal : Insufficient account amount");
        }

        sgBankAccount.withdrawal(money);

    }

    @Override
    public BankAccount createAccount(BankClient client) {
        // Check that this client is managed in this bank
        if (!(client instanceof SGBankClient) || !clients.contains(client)){
            throw new IllegalArgumentException("Unable to create an account : Unknown client");
        }
        BankAccount account = new SGBankAccount();

        SGBankAccount sgBankAccount = (SGBankAccount) account;
        SGBankClient sgBankClient = (SGBankClient) client;

        sgBankClient.add(sgBankAccount);

        return account;
    }

    @Override
    public BankClient createClient() {
        BankClient client = new SGBankClient();
        clients.add(client);
        return client;
    }

    /**
     * Implement of a bank client, it got a list of its accounts
     */
    private class SGBankClient implements BankClient {
        private Set<SGBankAccount> accounts;

        private SGBankClient(){
            accounts = new HashSet<>();
        }

        private boolean hasAccount(SGBankAccount account){
            return accounts.contains(account);
        }

        private boolean add(SGBankAccount account){
            return accounts.add(account);
        }

        @Override
        public List<BankAccount> getAccounts() {
            return new ArrayList<>(accounts);
        }
    }

    /**
     * Implementation of a bank account
     */
    private class SGBankAccount implements BankAccount {

        private BigDecimal balance;
        private List<String> operations;

        private SGBankAccount() {
            balance = ZERO;
            operations = new ArrayList<>();
        }

        private void deposit(BigDecimal money) {
            BigDecimal newBalance = balance.add(money);
            operations.add(String.format(STATEMENT_FORMAT, DEPOSIT_TYPE, dateFormat.format(dateFactory.getDate()), money, newBalance));

            balance = newBalance;
        }

        private void withdrawal(BigDecimal money) {
            BigDecimal newBalance = balance.subtract(money);
            operations.add(String.format(STATEMENT_FORMAT, WITHDRAWAL_TYPE, dateFormat.format(dateFactory.getDate()), money, newBalance));

            balance = newBalance;
        }

        @Override
        public BigDecimal getBalance() {
            return balance;
        }

        @Override
        public String getOperations() {
            return String.join("\n", operations);
        }
    }
}
