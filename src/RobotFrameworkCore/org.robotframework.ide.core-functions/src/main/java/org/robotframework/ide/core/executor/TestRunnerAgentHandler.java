package org.robotframework.ide.core.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    
    private int port;
    
    public TestRunnerAgentHandler(int port) {
        listeners = new ArrayList<>();
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            Socket client = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                for (IRobotOutputListener listener : listeners) {
                    listener.handleLine(line);
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
