import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.List;

/**
 * @author shiming.zhao
 * @date 2019/04/24
 */
public class AttachTest {

    public static void main(String[] args) throws IOException, AttachNotSupportedException {
        String userDir = System.getProperty("user.dir");
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : list) {
            if (vmd.displayName().endsWith("AtmClient")) {
                VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                try {
                    virtualMachine.loadAgent(userDir + "/DynamicAgent/target/agent.jar", null);
                    System.out.println("Attach success.");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    virtualMachine.detach();
                }
            }
        }

    }

}
