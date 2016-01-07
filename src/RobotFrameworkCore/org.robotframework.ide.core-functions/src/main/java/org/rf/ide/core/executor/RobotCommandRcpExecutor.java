/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.NullParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.NullSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.apache.xmlrpc.serializer.TypeSerializerImpl;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 */
@SuppressWarnings("PMD.GodClass")
class RobotCommandRcpExecutor implements RobotCommandExecutor {

    private static final int CONNECTION_TIMEOUT = 30;

    private final String interpreterPath;

    private final File scriptFile;

    private Process serverProcess;

    private XmlRpcClient client;

    RobotCommandRcpExecutor(final String interpreterPath, final File scriptFile) {
        this.interpreterPath = interpreterPath;
        this.scriptFile = scriptFile;
    }

    void waitForEstablishedConnection() {
        if (new File(interpreterPath).exists() && scriptFile.exists()) {
            final int port = findFreePort();
            serverProcess = createPythonServerProcess(interpreterPath, scriptFile, port);
            client = createClient(port);
            waitForConnectionToServer(client, CONNECTION_TIMEOUT);
        } else {
            throw new RobotCommandExecutorException("Could not setup python server on file: " + interpreterPath);
        }
    }

    private Process createPythonServerProcess(final String interpreterPath, final File scriptFile, final int port) {
        try {
            final List<String> command = newArrayList(interpreterPath, scriptFile.getPath(), String.valueOf(port));
            final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            startOutputReadingThread(process);
            return process;
        } catch (final IOException e) {
            throw new RobotCommandExecutorException("Could not setup python server on file: " + interpreterPath, e);
        }
    }

    private void startOutputReadingThread(final Process process) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final InputStream inputStream = process.getInputStream();
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line = reader.readLine();
                    while (line != null) {
                        line = reader.readLine();
                    }
                } catch (final IOException e) {
                    // that fine
                } finally {
                    serverProcess = null;
                }
            }
        }).start();
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            throw new RobotCommandExecutorException("Unable to find empty port for XmlRpc server", e);
        }
    }

    private XmlRpcClient createClient(final int port) {
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://127.0.0.1:" + port));
            config.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
            config.setReplyTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
        } catch (final MalformedURLException e) {
            // can't happen here
        }
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcSun15HttpTransportFactory transportFactory = new XmlRpcSun15HttpTransportFactory(client);
        transportFactory.setProxy(Proxy.NO_PROXY);
        client.setTransportFactory(transportFactory);
        client.setConfig(config);
        client.setTypeFactory(new XmlRpcTypeFactoryWithNil(client));
        return client;
    }

    private void waitForConnectionToServer(final XmlRpcClient client, final int timeoutInSec) {
        final long start = System.currentTimeMillis();
        while (true) {
            try {
                client.execute("checkServerAvailability", new Object[] {});
                break;
            } catch (final XmlRpcException e) {
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException ie) {
                    // we'll try once again
                }
            }
            if (System.currentTimeMillis() - start > (timeoutInSec * 1000)) {
                serverProcess = null;
                break;
            }
        }
    }

    boolean isAlive() {
        return serverProcess != null;
    }

    void kill() {
        serverProcess.destroy();
        try {
            serverProcess.waitFor();
        } catch (final InterruptedException e) {
            throw new RobotCommandExecutorException("Unable to kill rcp server", e);
        }
    }

    @Override
    public Map<String, Object> getVariables(final String filePath, final List<String> fileArguments) throws XmlRpcException {
        final Map<String, Object> variables = new LinkedHashMap<>();
        final Map<?, ?> varToValueMapping = (Map<?, ?>) client.execute("getVariables",
                newArrayList(filePath, fileArguments));
        for (final Entry<?, ?> entry : varToValueMapping.entrySet()) {
            variables.put((String) entry.getKey(), entry.getValue());
        }
        return variables;
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        final Map<String, Object> variables = new LinkedHashMap<>();
        try {
            final Map<?, ?> varToValueMapping = (Map<?, ?>) client.execute("getGlobalVariables", newArrayList());
            for (final Entry<?, ?> entry : varToValueMapping.entrySet()) {
                variables.put((String) entry.getKey(), entry.getValue());
            }
            return variables;
        } catch (final XmlRpcException e) {
            throw new RobotCommandExecutorException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final List<String> libraries = newArrayList();
            final Object[] libs = (Object[]) client.execute("getStandardLibrariesNames", newArrayList());
            for (final Object o : libs) {
                libraries.add((String) o);
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RobotCommandExecutorException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getStandardLibraryPath(final String libName) {
        try {
            return (String) client.execute("getStandardLibraryPath", newArrayList(libName));
        } catch (final XmlRpcException e) {
            throw new RobotCommandExecutorException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getRobotVersion() {
        try {
            return (String) client.execute("getRobotVersion", newArrayList());
        } catch (final XmlRpcException e) {
            throw new RobotCommandExecutorException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getRunModulePath() {
        try {
            return (String) client.execute("getRunModulePath", newArrayList());
        } catch (final XmlRpcException e) {
            throw new RobotCommandExecutorException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException {
        createLibdoc(resultFilePath, libName, libPath);
    }

    @Override
    public void createLibdocForPythonLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException {
        createLibdoc(resultFilePath, libName, libPath);
    }

    @Override
    public void createLibdocForJavaLibrary(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException {
        createLibdoc(resultFilePath, libName, libPath);
    }

    private void createLibdoc(final String resultFilePath, final String libName, final String libPath)
            throws RobotEnvironmentException {
        try {
            final Boolean wasGenerated = (Boolean) client.execute("createLibdoc",
                    newArrayList(resultFilePath, libName, libPath));
            if (!wasGenerated) {
                throw new RobotEnvironmentException("Unable to generate library specification file for library "
                        + libName + ", for library path " + libPath + " and result file " + resultFilePath);
            }
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to generate library specification file for library " + libName
                    + ", for library path " + libPath + " and result file " + resultFilePath, e);
        }
    }

    @Override
    public List<File> getModulesSearchPaths() throws RobotEnvironmentException {
        try {
            final List<File> libraries = newArrayList();
            final Object[] paths = (Object[]) client.execute("getModulesSearchPaths", newArrayList());
            for (final Object o : paths) {
                if (!"".equals(o)) {
                    libraries.add(new File((String) o));
                }
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to obtain modules search path", e);
        }
    }

    @Override
    public Optional<File> getModulePath(final String moduleName) throws RobotEnvironmentException {
        try {
            final String path = (String) client.execute("getModulePath", newArrayList(moduleName));
            return Optional.of(new File(path));
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to path of '" + moduleName + "' module", e);
        }
    }

    @SuppressWarnings("serial")
    static class RobotCommandExecutorException extends RuntimeException {

        RobotCommandExecutorException(final String message) {
            super(message);
        }

        RobotCommandExecutorException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private static class XmlRpcTypeFactoryWithNil extends TypeFactoryImpl {

        // Value null is not a part of xml-rpc specification, it is an
        // extension, so apache library
        // handles it with namespace added (<ex:nil>); unfortunately many other
        // libraries does not
        // handle <ex:nil>, but handles <nil> instead, which is not even a part
        // of specification.
        // This is so common that it is de-facto standard. This class is
        // responsible for handling
        // <nil> tags
        public XmlRpcTypeFactoryWithNil(final XmlRpcController controller) {
            super(controller);
        }

        @Override
        public TypeParser getParser(final XmlRpcStreamConfig config, final NamespaceContextImpl context,
                final String uri, final String localName) {
            if (NullSerializer.NIL_TAG.equals(localName) || NullSerializer.EX_NIL_TAG.equals(localName)) {
                return new NullParser();
            } else {
                return super.getParser(config, context, uri, localName);
            }
        }

        @Override
        public TypeSerializer getSerializer(final XmlRpcStreamConfig config, final Object object) throws SAXException {

            if (object == null) {
                return new TypeSerializerImpl() {

                    @Override
                    public void write(final ContentHandler handler, final Object o) throws SAXException {
                        handler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
                        handler.startElement("", NullSerializer.NIL_TAG, NullSerializer.NIL_TAG, ZERO_ATTRIBUTES);
                        handler.endElement("", NullSerializer.NIL_TAG, NullSerializer.NIL_TAG);
                        handler.endElement("", VALUE_TAG, VALUE_TAG);
                    }
                };
            } else {
                return super.getSerializer(config, object);
            }
        }
    }
}
