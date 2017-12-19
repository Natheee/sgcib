import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

public class SGBank implements Bank {

    private static final String STATEMENT_FORMAT = "%s - %s - %s - %s";
    private static final String WITHDRAWAL_TYPE = "W";
    private static final String DEPOSIT_TYPE = "D";

    private final DateFactory dateFactory;

    private SimpleDateFormat dateFormat;

    public SGBank(DateFactory dateFactory){
        this.dateFactory = dateFactory;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public void deposit(Account account, BigDecimal money) {
        if (money == null || money.compareTo(ZERO) == 0){
            throw new IllegalArgumentException("Unable to deposit : Null amount");
        }else if (money.compareTo(ZERO) < 0){
            throw new IllegalArgumentException("Unable to deposit : Negative amount");
        }

        SGBankAccount SGBankAccount = (SGBankAccount) account;
        SGBankAccount.deposit(money);
    }

    @Override
    public void withdrawal(Account account, BigDecimal money) {

        SGBankAccount SGBankAccount = (SGBankAccount) account;

        if (money == null || money.compareTo(ZERO) == 0){
            throw new IllegalArgumentException("Unable to make a withdrawal : Null amount");
        }else if (money.compareTo(ZERO) < 0){
            throw new IllegalArgumentException("Unable to make a withdrawal : Negative amount");
        } else if (SGBankAccount.getBalance().compareTo(money) < 0){
            throw new IllegalStateException("Unable to make a withdrawal : Insufficient account amount");
        }

        SGBankAccount.withdrawal(money);

    }

    @Override
    public Account createAccount(){
        return new SGBankAccount();
    }

    /**
     *
     */
    private class SGBankAccount implements Account{

        private BigDecimal balance;
        private List<String> operations;

        private SGBankAccount(){
            balance = ZERO;
            operations = new ArrayList<>();
        }

        private void deposit(BigDecimal money){
            BigDecimal newBalance = balance.add(money);
            operations.add(String.format(STATEMENT_FORMAT,DEPOSIT_TYPE,dateFormat.format(dateFactory.getDate()),money,newBalance));

            balance = newBalance;
        }

        private void withdrawal(BigDecimal money){
            BigDecimal newBalance = balance.subtract(money);
            operations.add(String.format(STATEMENT_FORMAT,WITHDRAWAL_TYPE,dateFormat.format(dateFactory.getDate()),money,newBalance));

            balance = newBalance;
        }

        @Override
        public BigDecimal getBalance() {
            return balance;
        }

        @Override
        public String getOperations() {
            return String.join("\n",operations);
        }
    }
}
