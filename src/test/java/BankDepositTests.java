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
 * Bank Deposits tests
 */
public class BankDepositTests {

    /**
     * Default Date
     */
    private static final Date DATE = new Date(0);

    private Bank bank;
    private BankClient client;
    private BankAccount account;

    /**
     * Setting up a bank, a client with an empty account
     */
    @Before
    public void setUp() {
        bank = new SGBank(() -> DATE);
        client = bank.createClient();
        account = bank.createAccount(client);
    }

    /**
     * Trying to make a deposit with right amount (>0) a right client (client of the bank)
     * and a right account (an account of this client
     */
    @Test
    public void depositMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        // Estimate of the final balance after the operation
        BigDecimal finalBalance = account.getBalance().add(moneyToAdd);

        // Make the deposit
        bank.deposit(client, account, moneyToAdd);

        // Check balance after deposit
        assertThat(account.getBalance()).isEqualTo(finalBalance);
    }

    /**
     * Trying to make a deposit with right amount (>0) a wrong client
     * (not a client of the bank) and a right account
     * (an account of this client).
     * Expected a IllegalArgumentException because of wrong client
     */
    @Test
    public void depositOtherBank() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        // Creating an another bank
        Bank otherBank = new SGBank(() -> DATE);

        // Deposit with the other bank
        assertThatThrownBy(() -> otherBank.deposit(client, account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Unknown client");
    }

    /**
     * Trying to make a deposit with right amount (>0) a wrong client
     * (mocked client) and a right account
     * (an account of this client).
     * Expected a IllegalArgumentException because of wrong client
     */
    @Test
    public void depositFakeClient() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        // Mocking the client
        BankClient fakeClient = Mockito.mock(BankClient.class);

        // Deposit with the mocked client
        assertThatThrownBy(() -> bank.deposit(fakeClient, account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Unknown client");
    }

    /**
     * Trying to make a deposit with right amount (>0) a right client
     * (client of this bank) and a wrong account (mocked account).
     * Expected a IllegalArgumentException because of wrong account
     */
    @Test
    public void depositFakeAccount() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        // Mocking the account
        BankAccount fakeAccount = Mockito.mock(BankAccount.class);

        // Deposit with the mocked account
        assertThatThrownBy(() -> bank.deposit(client, fakeAccount, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Wrong account");
    }


    /**
     * Trying to make a deposit with right amount (>0) a right client
     * (client of this bank) and a wrong account (account owned by an another client).
     * Expected a IllegalArgumentException because of wrong account
     */
    @Test
    public void depositWrongAccount() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(123.4);

        // Creating another client with the same bank
        BankClient otherClient = bank.createClient();

        // Deposit with the another client
        assertThatThrownBy(() -> bank.deposit(otherClient, account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Wrong account");
    }

    /**
     * Trying to make a deposit with wrong amount (<0) a right client (client of the bank)
     * and a right account (an account of this client).
     * Expected a IllegalArgumentException because of wrong amount
     */
    @Test
    public void depositNegativeMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(-123.4);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(client, account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Negative amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a deposit with wrong amount (0) a right client (client of the bank)
     * and a right account (an account of this client).
     * Expected a IllegalArgumentException because of wrong amount
     */
    @Test
    public void depositNoMoney() {
        BigDecimal moneyToAdd = BigDecimal.valueOf(0);

        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(client, account, moneyToAdd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }

    /**
     * Trying to make a deposit with wrong amount (null) a right client (client of the bank)
     * and a right account (an account of this client).
     * Expected a IllegalArgumentException because of wrong amount
     */
    @Test
    public void depositNullMoney() {
        BigDecimal originalAmount = account.getBalance();

        assertThatThrownBy(() -> bank.deposit(client, account, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to make a deposit : Null amount");

        // Check not modified balance after deposit
        assertThat(account.getBalance()).isEqualTo(originalAmount);
    }
}
