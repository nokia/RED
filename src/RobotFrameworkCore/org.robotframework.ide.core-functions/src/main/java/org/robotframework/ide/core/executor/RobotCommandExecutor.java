/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * @author mmarzec
 */
public class RobotCommandExecutor {

    private Map<String, PythonProcessContext> processesMap;

    private int port;
    
    private RobotCommandExecutor() {
        processesMap = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (String pythonLocation : processesMap.keySet()) {
                    if (processesMap.get(pythonLocation).getServerProcess() != null) {
                        processesMap.get(pythonLocation).getServerProcess().destroy();
                    }
                }
            }
        });
    }

    private static class InstanceHolder {

        private static final RobotCommandExecutor instance = new RobotCommandExecutor();
    }

    public static RobotCommandExecutor getInstance() {
        return InstanceHolder.instance;
    }

    public synchronized void setupPythonProcess(final String pythonFolderLocation, final String pythonFileLocation,
            final String scriptLocation) {
        if (!isPythonProcessStarted(pythonFolderLocation)) {
            if (new File(pythonFileLocation).exists() && new File(scriptLocation).exists()) {
                createPythonServerProcess(pythonFolderLocation, pythonFileLocation, scriptLocation);
                createClient(pythonFolderLocation);
            } else {
                throw new RuntimeException("Could not setup python server on file: " + pythonFileLocation);
            }
        }
    }

    private void createPythonServerProcess(final String pythonFolderLocation, final String pythonFileLocation,
            final String scriptLocation) {

        port = findFreePort();
        final List<String> command = new ArrayList<>();
        command.add(pythonFileLocation);
        command.add(scriptLocation);
        command.add(String.valueOf(port));
        try {
            final Process serverProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            readFromProcessInputStream(serverProcess, pythonFolderLocation);
            processesMap.put(pythonFolderLocation, new PythonProcessContext(serverProcess, pythonFileLocation,
                    scriptLocation));
            waitForProcessTermination(pythonFolderLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void readFromProcessInputStream(final Process serverProcess, final String pythonFolderLocation) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final InputStream inputStream = serverProcess.getInputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        processesMap.get(pythonFolderLocation).setCurrentServerProcessMessage(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void waitForProcessTermination(final String pythonFolderLocation) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    processesMap.get(pythonFolderLocation).getServerProcess().waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    processesMap.get(pythonFolderLocation).setServerProcess(null);
                }
            }
        }).start();
    }

    private void createClient(final String pythonFolderLocation) {
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://127.0.0.1:" + port));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        processesMap.get(pythonFolderLocation).setClient(client);
        waitForConnectionToServer(pythonFolderLocation);
    }

    private void waitForConnectionToServer(final String pythonFolderLocation) {
        int retryCounter = 0;
        while (retryCounter < 50) {
            try {
                if (processesMap.get(pythonFolderLocation).getServerProcess() != null) {
                    processesMap.get(pythonFolderLocation)
                            .getClient()
                            .execute("checkServerAvailability", new Object[] {});
                }
                break;
            } catch (XmlRpcException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                retryCounter++;
            }
        }
    }

    public String getVariables(final String pythonFolderLocation, final String filePath, final String fileArguments) {
        Object result = invokeMethod(pythonFolderLocation, "getVariables", new Object[] { filePath, fileArguments });
        return getStringResult(result);
    }

    public String getGlobalVariables(final String pythonFolderLocation) {
        Object result = invokeMethod(pythonFolderLocation, "getGlobalVariables", new Object[] {});
        return getStringResult(result);
    }

    public String getStandardLibrariesNames(final String pythonFolderLocation) {
        Object result = invokeMethod(pythonFolderLocation, "getStandardLibrariesNames", new Object[] {});
        return getStringResult(result);
    }

    public String getStandardLibraryPath(final String pythonFolderLocation, final String libName) {
        Object result = invokeMethod(pythonFolderLocation, "getStandardLibraryPath", new Object[] { libName });
        return getStringResult(result);
    }

    public String getRobotVersion(final String pythonFolderLocation) {
        Object result = invokeMethod(pythonFolderLocation, "getRobotVersion", new Object[] {});
        return getStringResult(result);
    }

    public String getRunModulePath(final String pythonFolderLocation) {
        Object result = invokeMethod(pythonFolderLocation, "getRunModulePath", new Object[] {});
        return getStringResult(result);
    }

    public String createLibdoc(final String pythonFolderLocation, final String resultFilePath, final String libName,
            final String libPath) {
        Object result = invokeMethod(pythonFolderLocation, "createLibdoc", new Object[] { resultFilePath, libName,
                libPath });
        return getStringResult(result);
    }

    private synchronized Object invokeMethod(final String pythonFolderLocation, final String methodName,
            final Object[] params) {

        setupPythonProcess(pythonFolderLocation, processesMap.get(pythonFolderLocation).getPythonFileLocation(),
                processesMap.get(pythonFolderLocation).getScriptLocation());

        Object result = null;
        try {
            final PythonProcessContext processContext = processesMap.get(pythonFolderLocation);
            if (processContext != null && processContext.getClient() != null) {
                result = processContext.getClient().execute(methodName, params);
            }
        } catch (XmlRpcException e) {
            try {
                throw new RobotCommandExecutorException("Could not invoke server method \"" + methodName
                        + "\" on python installation: "
                        + processesMap.get(pythonFolderLocation).getPythonFileLocation() + "\nProcess message: "
                        + processesMap.get(pythonFolderLocation).getCurrentServerProcessMessage());
            } catch (RobotCommandExecutorException e1) {
                e1.printStackTrace();
            }
        }
        
        return result;
    }
    
    private String getStringResult(Object result) {
        return result != null ? result.toString() : null;
    }

    public boolean isPythonProcessStarted(final String pythonFolderLocation) {
        return processesMap.containsKey(pythonFolderLocation)
                && processesMap.get(pythonFolderLocation).getServerProcess() != null;
    }

    public void close() {
        for (String pythonLocation : processesMap.keySet()) {
            if (processesMap.get(pythonLocation).getServerProcess() != null) {
                try {
                    processesMap.get(pythonLocation).getClient().execute("close", new Object[] {});
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            return -1;
        }
    }

    private class PythonProcessContext {

        private Process serverProcess;

        private XmlRpcClient client;

        private String pythonFileLocation;

        private String scriptLocation;
        
        private String currentServerProcessMessage = "";

        public PythonProcessContext(final Process serverProcess, final String pythonFileLocation,
                final String scriptLocation) {
            this.serverProcess = serverProcess;
            this.pythonFileLocation = pythonFileLocation;
            this.scriptLocation = scriptLocation;
        }

        public Process getServerProcess() {
            return serverProcess;
        }

        public void setServerProcess(final Process serverProcess) {
            this.serverProcess = serverProcess;
        }

        public XmlRpcClient getClient() {
            return client;
        }

        public void setClient(final XmlRpcClient client) {
            this.client = client;
        }

        public String getPythonFileLocation() {
            return pythonFileLocation;
        }

        public String getScriptLocation() {
            return scriptLocation;
        }

        public String getCurrentServerProcessMessage() {
            return currentServerProcessMessage;
        }

        public void setCurrentServerProcessMessage(final String currentServerProcessMessage) {
            this.currentServerProcessMessage = currentServerProcessMessage;
        }

    }
    
    @SuppressWarnings("serial")
    public static class RobotCommandExecutorException extends Exception {

        public RobotCommandExecutorException(final String message) {
            super(message);
        }


        public RobotCommandExecutorException(final String message,
                final Throwable cause) {
            super(message, cause);
        }
    }
}
