import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * Statement printing testing
 */
public class BankOperationTests {

    /**
     * Format used by the statement printing
     */
    private static final String STATEMENT_PRINTING_FORMAT = "(W|D) - (\\d\\d/\\d\\d/\\d\\d\\d\\d) - ([\\d.]+) - ([\\d.]+)";
    /**
     * Default date
     */
    private static final Date DATE = new Date(0);
    private Bank bank;
    private BankClient client;
    private BankAccount account;

    /**
     * Setting up a bank, a client and an empty account
     */
    @Before
    public void setUp() {
        bank = new SGBank(() -> DATE);
        client = bank.createClient();
        account = bank.createAccount(client);
    }

    /**
     * Verification of the printing of the statements : empty
     */
    @Test
    public void checkEmptyOperations() {
        assertThat(account.getOperations()).isEmpty();
    }


    /**
     * Verification of the printing of the statements : 3 operations :
     * Deposit (10000), Deposit (756.12) , Withdrawal (156)
     */
    @Test
    public void checkOperations() {

        BigDecimal moneyToAdd = BigDecimal.valueOf(10000);
        BigDecimal moneyToAdd2 = BigDecimal.valueOf(756.12);
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(156);

        // Do the operations
        bank.deposit(client, account, moneyToAdd);
        bank.deposit(client, account, moneyToAdd2);
        bank.withdrawal(client, account, moneyToRetrieve);

        // Splitting the result and checking that the pattern matches
        String operationString = account.getOperations();
        String[] operations = operationString.split("\n");
        assertThat(operations).hasSize(3).allMatch(operation -> operation.matches(STATEMENT_PRINTING_FORMAT));

        // Checking each fields
        String date = new SimpleDateFormat("dd/MM/yyyy").format(DATE);
        checkOperation(operations[0], "D", date, moneyToAdd, moneyToAdd);
        checkOperation(operations[1], "D", date, moneyToAdd2, moneyToAdd.add(moneyToAdd2));
        checkOperation(operations[2], "W", date, moneyToRetrieve, moneyToAdd.add(moneyToAdd2).subtract(moneyToRetrieve));

    }

    /**
     * Check one printed statement
     *
     * @param operation The statement
     * @param operationType         The operation type : W for withdrawal, D for deposit
     * @param date      The date, french style format
     * @param amount    The amount of the statement
     * @param balance   The balance of the account after the statement
     */
    private void checkOperation(String operation, String operationType, String date,
                                BigDecimal amount, BigDecimal balance) {
        Matcher match = Pattern.compile(STATEMENT_PRINTING_FORMAT).matcher(operation);
        if (!match.matches()) {
            fail("Operation doesn't match");
        }
        // Checking type
        assertThat(match.group(1)).isEqualTo(operationType);
        // Checking date
        assertThat(match.group(2)).isEqualTo(date);
        // Checking the amount
        assertThat(match.group(3)).isEqualTo(amount.toString());
        // Checking the balance
        assertThat(match.group(4)).isEqualTo(balance.toString());
    }

}
