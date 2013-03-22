package es.sidelab.scstack.lib.gerrit;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

public class OverthereOutputHandler implements OverthereProcessOutputHandler {

    StringBuilder out = new StringBuilder();
    StringBuilder err = new StringBuilder();

    @Override
    public void handleErrorLine(String arg0) {
        err.append(arg0);
    }

    @Override
    public void handleOutput(char arg0) {
        // Do nothing!
    }

    @Override
    public void handleOutputLine(String arg0) {
        out.append(arg0);
    }

    public String getErr() {
        return err.toString();
    }

    public String getOut() {
        return out.toString();
    }
}