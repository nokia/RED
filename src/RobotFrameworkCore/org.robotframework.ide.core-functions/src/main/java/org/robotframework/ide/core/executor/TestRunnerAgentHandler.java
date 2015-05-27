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

    private final List<ILineHandler> listeners;
    
    private final int port;
    
    public TestRunnerAgentHandler(final int port) {
        listeners = new ArrayList<>();
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            final Socket client = socket.accept();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                for (final ILineHandler listener : listeners) {
                    listener.processLine(line);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(final ILineHandler listener) {
        listeners.add(listener);
    }

    public void removeListener(final ILineHandler listener) {
        listeners.remove(listener);
    }
    
}
