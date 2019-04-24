import java.lang.instrument.Instrumentation;

/**
 * 静态agent，main函数执行前运行premain方法<br>
 *
 * @author shiming.zhao
 * @date 2019/04/24
 */
public class StaticAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Executing premain ...");
        instrumentation.addTransformer(new StaticTransformer());
    }
}
