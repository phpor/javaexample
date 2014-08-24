package phpor.signal;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Java Signal Test
 * @author Ken Wu
 *
 */
@SuppressWarnings("restriction")
public class TestSignal implements SignalHandler {

    private void signalCallback(Signal sn) {
        System.out.println(sn.getName() + "is recevied.");
    }

    @Override
    public void handle(Signal signalName) {
        signalCallback(signalName);
    }

    public static void main(String[] args) throws InterruptedException {
        TestSignal testSignalHandler = new TestSignal();
        // install signals
        Signal sig = new Signal("USR2");
        Signal.handle(sig, testSignalHandler);
        Thread.sleep(15000);
    }

}