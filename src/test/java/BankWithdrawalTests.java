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
 * Bank withdrawal tests
 */
public class BankWithdrawalTests {

    /**
     * Default date
     */
    private static final Date DATE = new Date(0);
    private Bank bank;
    private BankClient client;
    private BankAccount account;

    /**
     * Setting up a bank and an empty account
     */
    @Before
    public void setUp() {
        bank = new SGBank(() -> DATE);
        client = bank.createClient();
        account = bank.createAccount(client);
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a right client (client of the bank)
     * and a right account (an account of this client and sufficient balance).
     * Expected that 123.4 is add to the balance account.
     */
    @Test
    public void withdrawalMoney() {
        BankAccount accountWithMoney = getAccountWithTenThousand();

        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);

        // Check default balance
        assertThat(accountWithMoney.getBalance()).usingDefaultComparator().isGreaterThanOrEqualTo(moneyToRetrieve);

        BigDecimal finalBalance = accountWithMoney.getBalance().subtract(moneyToRetrieve);

        bank.withdrawal(client, accountWithMoney, moneyToRetrieve);

        // Check balance after deposit
        assertThat(accountWithMoney.getBalance()).isEqualTo(finalBalance);
    }

    /**
     * Give an account with a 10000 balance
     * @return an account
     */
    private BankAccount getAccountWithTenThousand() {
        BankAccount account = bank.createAccount(client);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
        bank.deposit(client, account,BigDecimal.valueOf(10000));
        return account;
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a right client (client of the bank)
     * and a wrong account (insufficient balance).
     * Expected a IllegalStateException
     */
    @Test
    public void withdrawalNotEnoughMoney() {
        // Check default balance
        BigDecimal originalAmount = account.getBalance();
        BigDecimal moneyToRetrieve = originalAmount.add(BigDecimal.valueOf(1));

        assertThat(account.getBalance()).usingDefaultComparator().isLessThan(moneyToRetrieve);

        assertThatThrownBy(() -> bank.withdrawal(client, account, moneyToRetrieve))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to make a withdrawal : Insufficient account amount");

        // Check balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a wrong client (not a client of the bank)
     * and a right account (an account of this client and sufficient balance).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalOtherBank(){
        BankAccount accountWithMoney = getAccountWithTenThousand();
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);
        // New bank
        Bank otherBank = new SGBank(() -> DATE);
        // Withdrawal with the other bank
        assertThatThrownBy(() -> otherBank.withdrawal(client, accountWithMoney, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Unknown client");
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a wrong client (mocked client)
     * and a right account (sufficient balance).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalFakeClient(){
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);

        BankAccount account = getAccountWithTenThousand();
        // Mock
        BankClient fakeClient = Mockito.mock(BankClient.class);
        // Withdrawal with mock
        assertThatThrownBy(() -> bank.withdrawal(fakeClient, account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Unknown client");
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a right client (client of the bank)
     * and a wrong account (mocked account).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalFakeAccount(){
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);

        //Creating mock
        BankAccount fakeAccount = Mockito.mock(BankAccount.class);
        //Withdrawal withe the mock
        assertThatThrownBy(() -> bank.withdrawal(client, fakeAccount, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Wrong account");
    }

    /**
     * Trying to make a withdrawal with right amount (>0) a right client (client of the bank)
     * and a wrong account (account from another client).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalOtherClient(){
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(123.4);

        BankAccount account = getAccountWithTenThousand();
        // Create another client from this bank
        BankClient otherClient = bank.createClient();
        // Withdrawal with the other client
        assertThatThrownBy(() -> bank.withdrawal(otherClient, account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Wrong account");
    }

    /**
     * Trying to make a withdrawal with wrong amount (<0) a right client (client of the bank)
     * and a right account (account from this client).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalNegativeMoney() {
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(-123.4);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(client, account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Negative amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal with wrong amount (0) a right client (client of the bank)
     * and a right account (account from this client).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalNoMoney() {
        BigDecimal moneyToRetrieve = BigDecimal.valueOf(0);
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(client, account, moneyToRetrieve))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a withdrawal with wrong amount (null) a right client (client of the bank)
     * and a right account (account from this client).
     * Expected a IllegalArgumentException
     */
    @Test
    public void withdrawalNullMoney() {
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.withdrawal(client, account, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a withdrawal : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }
}
