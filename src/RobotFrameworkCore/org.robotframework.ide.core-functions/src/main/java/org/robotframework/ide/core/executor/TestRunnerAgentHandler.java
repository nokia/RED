package org.robotframework.ide.core.executor;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarzec
 *
 */
public class TestRunnerAgentHandler implements Runnable {

    private List<IRobotOutputListener> listeners;

    public TestRunnerAgentHandler() {
        listeners = new ArrayList<>();
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(54470)) {
            socket.setReuseAddress(true);
            Socket client = socket.accept();
            InputStream is = client.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            while ((i = is.read()) != -1) {
                if ((char) i == '\n') {
                    for (IRobotOutputListener listener : listeners) {
                        listener.handleLine(stringBuilder.toString());
                    }
                    stringBuilder = new StringBuilder();
                } else {
                    stringBuilder.append((char) i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(IRobotOutputListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IRobotOutputListener listener) {
        listeners.remove(listener);
    }
}
