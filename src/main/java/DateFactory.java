import java.util.Date;

@FunctionalInterface
public interface DateFactory {

    /**
     * get a date
     * @return a date
     */
    Date getDate();
}
