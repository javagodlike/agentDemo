import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author shiming.zhao
 * @date 2019/04/24
 */
public class DynamicAgent {

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        System.out.println("DynamicAgent invoked.");
        instrumentation.addTransformer(new StaticTransformer(), true);
        instrumentation.retransformClasses(Atm.class);
    }

}
