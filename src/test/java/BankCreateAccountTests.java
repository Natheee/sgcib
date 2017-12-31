import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Creating accounts and clients testing
 */
public class BankCreateAccountTests {

    /**
     * Default date
     */
    private static final Date DATE = new Date(0);
    private Bank bank;

    /**
     * Setting up a bank
     */
    @Before
    public void setUp() {
        bank = new SGBank(() -> DATE);
    }

    /**
     * Trying to create an account with an another client implementation.
     * Expected an IllegalArgumentException
     */
    @Test
    public void createWithFakeClient() {
        // Mocking a client
        BankClient client = Mockito.mock(BankClient.class);
        // Trying to create a client from a mock
        assertThatThrownBy(() -> bank.createAccount(client))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to create an account : Unknown client");
    }

    /**
     * Trying to create an account with a client from an another SGBank.
     * Expected an IllegalArgumentException
     */
    @Test
    public void createFromOtherBank() {
        // Creating another bank
        Bank otherBank = new SGBank(() -> DATE);
        // Creating another client from another bank
        BankClient otherClient = otherBank.createClient();
        // Trying to create an account with a client from another client
        assertThatThrownBy(() -> bank.createAccount(otherClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unable to create an account : Unknown client");

        // No accounts created
        assertThat(otherClient.getAccounts()).isEqualTo(Lists.emptyList());
    }

    /**
     * Trying to create an account with a newly created client
     * Expected a not null account
     */
    @Test
    public void create() {
        BankClient client = bank.createClient();
        BankAccount account = bank.createAccount(client);
        assertThat(account).isNotNull();

        // Checking that the account is rightly created for this client
        assertThat(client.getAccounts()).isEqualTo(Lists.newArrayList(account));
    }
}
