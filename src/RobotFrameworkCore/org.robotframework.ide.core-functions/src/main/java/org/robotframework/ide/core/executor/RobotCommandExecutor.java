/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
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

    private static String currentPythonFolderLocation;

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

    public static RobotCommandExecutor getInstance(final String pythonFolderLocation) {
        currentPythonFolderLocation = pythonFolderLocation;
        return InstanceHolder.instance;
    }

    public synchronized void setupPythonProcess(final String pythonFileLocation, final String scriptLocation) {
        if (!isPythonProcessStarted(currentPythonFolderLocation)) {
            if (new File(pythonFileLocation).exists() && new File(scriptLocation).exists()) {
                createPythonServerProcess(pythonFileLocation, scriptLocation);
                createClient();
            } else {
                throw new RuntimeException("Could not setup python server.");
            }
        }
    }

    private void createPythonServerProcess(final String pythonFileLocation, final String scriptLocation) {

        port = findFreePort();
        final List<String> command = new ArrayList<>();
        command.add(pythonFileLocation);
        command.add(scriptLocation);
        command.add(String.valueOf(port));
        try {
            final Process serverProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            processesMap.put(currentPythonFolderLocation, new PythonProcessContext(serverProcess, pythonFileLocation,
                    scriptLocation));
            waitForProcessTermination(currentPythonFolderLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void createClient() {
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://127.0.0.1:" + port));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        final XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        processesMap.get(currentPythonFolderLocation).setClient(client);
        waitForConnectionToServer(client);
    }

    private void waitForConnectionToServer(final XmlRpcClient client) {
        int retryCounter = 0;
        while (retryCounter < 50) {
            try {
                client.execute("checkServerAvailability", new Object[] {});
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

    public String getVariables(final String filePath, final String fileArguments) {
        Object result = invokeMethod("getVariables", new Object[] { filePath, fileArguments });
        return getStringResult(result);
    }

    public String getGlobalVariables() {
        Object result = invokeMethod("getGlobalVariables", new Object[] {});
        return getStringResult(result);
    }

    public String getStandardLibrariesNames() {
        Object result = invokeMethod("getStandardLibrariesNames", new Object[] {});
        return getStringResult(result);
    }
    
    public String getStandardLibraryPath(final String libName) {
        Object result = invokeMethod("getStandardLibraryPath", new Object[] { libName });
        return getStringResult(result);
    }

    public String getRobotVersion() {
        Object result = invokeMethod("getRobotVersion", new Object[] {});
        return getStringResult(result);
    }
    
    public String getRunModulePath() {
        Object result = invokeMethod("getRunModulePath", new Object[] {});
        return getStringResult(result);
    }
    
    public String createLibdoc(final String resultFilePath, final String libName, final String libPath) {
        Object result = invokeMethod("createLibdoc", new Object[] { resultFilePath, libName, libPath });
        return getStringResult(result);
    }

    private synchronized Object invokeMethod(final String methodName, final Object[] params) {

        setupPythonProcess(processesMap.get(currentPythonFolderLocation).getPythonFileLocation(),
                processesMap.get(currentPythonFolderLocation).getScriptLocation());

        Object result = null;
        try {
            final PythonProcessContext processContext = processesMap.get(currentPythonFolderLocation);
            if (processContext != null && processContext.getClient() != null) {
                result = processContext.getClient().execute(methodName, params);
            }
        } catch (XmlRpcException e) {
            e.printStackTrace();
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
    }
}
