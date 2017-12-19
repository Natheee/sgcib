import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * Bank client user stories testing
 */
public class BankClientTests {

    /**
     * Format used by the statement printing
     */
    private static final String STATEMENT_PRINTING_FORMAT = "(W|D) - (\\d\\d/\\d\\d/\\d\\d\\d\\d) - ([\\d.]+) - ([\\d.]+)";
    private static final Date DATE = new Date(0);
    private Bank bank;
    private Account account;

    /**
     * Setting up a bank and an empty account
     */
    @Before
    public void setUp() {
        bank = new SGBank(() -> DATE);
        account = bank.createAccount();
    }

    /**
     * Trying to make a deposit
     */
    @Test
    public void depositMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        BigDecimal finalBalance = account.getBalance().add(moneyToAdd);

        bank.deposit(account, moneyToAdd);

        // Check balance after deposit
        assertThat(account.getBalance()).isEqualTo(finalBalance);
    }

    /**
     * Trying to make a deposit with a negative amount
     */
    @Test
    public void depositNegativeMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(-123.4);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to deposit : Negative amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a deposit without amount
     */
    @Test
    public void depositNoMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(0);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to deposit : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a deposit without amount
     */
    @Test
    public void depositNullMoney() {
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(account, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to deposit : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal
     */
    @Test
    public void withdrawalMoney() {
        Account accountWithMoney = getAccountWithTenThousand();

        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);

        // Check default balance
        assertThat(accountWithMoney.getBalance()).usingDefaultComparator().isGreaterThanOrEqualTo(moneyToRetrieve);

        BigDecimal finalBalance = accountWithMoney.getBalance().subtract(moneyToRetrieve);

        bank.withdrawal(accountWithMoney, moneyToRetrieve);

        // Check balance after deposit
        assertThat(accountWithMoney.getBalance()).isEqualTo(finalBalance);
    }

    private Account getAccountWithTenThousand() {
        Account account = bank.createAccount();
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
        bank.deposit(account,BigDecimal.valueOf(10000));
        return account;
    }

    /**
     * Trying to make a withdrawal but the client hasn't enough money
     */
    @Test
    public void withdrawalNotEnoughMoney() {
        // Check default balance
        BigDecimal originalAmount = account.getBalance();
        BigDecimal moneyToRetrieve = originalAmount.add(BigDecimal.valueOf(1));

        assertThat(account.getBalance()).usingDefaultComparator().isLessThan(moneyToRetrieve);

        assertThatThrownBy(() -> bank.withdrawal(account, moneyToRetrieve))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to make a withdrawal : Insufficient account amount");

        // Check balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal with a negative amount
     */
    @Test
    public void withdrawalNegativeMoney() {
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(-123.4);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Negative amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal without amount
     */
    @Test
    public void withdrawalNoMoney() {
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(0);
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal without amount
     */
    @Test
    public void withdrawalNullMoney() {
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(account, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Verification of the printing of the statements
     */
    @Test
    public void checkOperations() {
        Account accountWithMoney = getAccountWithTenThousand();

        BigDecimal moneyToAdd = BigDecimal.valueOf(756.12);
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(156);
        BigDecimal originalAmount = accountWithMoney.getBalance();

        bank.deposit(accountWithMoney, moneyToAdd);
        bank.withdrawal(accountWithMoney, moneyToRetrieve);

        String operationString = accountWithMoney.getOperations();
        String[] operations = operationString.split("\n");
        assertThat(operations).hasSize(3).allMatch(operation -> operation.matches(STATEMENT_PRINTING_FORMAT));

        String date = new SimpleDateFormat("dd/MM/yyyy").format(DATE);
        checkOperation(operations[0], "D", date, BigDecimal.valueOf(10000), originalAmount);
        checkOperation(operations[1], "D", date, moneyToAdd, originalAmount.add(moneyToAdd));
        checkOperation(operations[2], "W", date, moneyToRetrieve, originalAmount.add(moneyToAdd).subtract(moneyToRetrieve));

    }

    /**
     * Check one printed statement
     *
     * @param operation The statement
     * @param d         The operation type : W for withdrawal, D for deposit
     * @param date      The date, french style format
     * @param amount    The amount of the statement
     * @param balance   The balance of the account after the statement
     */
    private void checkOperation(String operation, String d, String date, BigDecimal amount, BigDecimal balance) {
        Matcher match = Pattern.compile(STATEMENT_PRINTING_FORMAT).matcher(operation);
        if (!match.matches()) {
            fail("Operation doesn't match");
        }
        assertThat(match.group(1)).isEqualTo(d);
        assertThat(match.group(2)).isEqualTo(date);
        assertThat(match.group(3)).isEqualTo(amount.toString());
        assertThat(match.group(4)).isEqualTo(balance.toString());
    }

}
