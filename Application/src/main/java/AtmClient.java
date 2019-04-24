import java.util.concurrent.TimeUnit;

/**
 * @author shiming.zhao
 * @date 2019/04/24
 */
public class AtmClient {

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            Atm.withdraw(10);
            TimeUnit.SECONDS.sleep(10);
        }
    }

}
