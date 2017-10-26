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
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Semaphore;
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
import org.rf.ide.core.jvmutils.process.OSProcessHelper;
import org.rf.ide.core.jvmutils.process.OSProcessHelper.ProcessHelperException;
import org.rf.ide.core.rflint.RfLintRule;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
@SuppressWarnings("PMD.GodClass")
class RobotCommandRpcExecutor implements RobotCommandExecutor {

    private static final int CONNECTION_TIMEOUT = 30;

    private final String interpreterPath;

    private final SuiteExecutor interpreterType;

    private final File scriptFile;

    private Process serverProcess;

    private boolean isExternal = false;

    private XmlRpcClient client;

    RobotCommandRpcExecutor(final String interpreterPath, final SuiteExecutor interpreterType, final File scriptFile) {
        this.interpreterPath = interpreterPath;
        this.interpreterType = interpreterType;
        this.scriptFile = scriptFile;
    }

    void waitForEstablishedConnection() {
        if (new File(interpreterPath).exists() && scriptFile.exists()) {
            isExternal = RedSystemProperties.shouldConnectToRunningServer();

            if (isExternal) {
                client = createClient(RedSystemProperties.getSessionServerAddress());
            } else {
                final int port = findFreePort();
                serverProcess = createPythonServerProcess(interpreterPath, scriptFile, port);
                client = createClient("127.0.0.1:" + port);
            }
            waitForConnectionToServer(CONNECTION_TIMEOUT);
        } else {
            throw new RobotCommandExecutorException("Could not setup python server on file: " + interpreterPath);
        }
    }

    private Process createPythonServerProcess(final String interpreterPath, final File scriptFile, final int port) {
        try {
            final List<String> command = newArrayList(interpreterPath, scriptFile.getPath(), String.valueOf(port));
            final Process process = new ProcessBuilder(command).start();

            final Semaphore semaphore = new Semaphore(0);
            startStdOutReadingThread(process, semaphore);
            startStdErrReadingThread(process, semaphore);
            return process;
        } catch (final IOException e) {
            throw new RobotCommandExecutorException("Could not setup python server on file: " + interpreterPath, e);
        }
    }

    private void startStdOutReadingThread(final Process process, final Semaphore semaphore) {
        new Thread(() -> {
            for (final PythonProcessListener listener : getListeners()) {
                listener.processStarted(interpreterPath, process);
            }
            semaphore.release();
            final InputStream inputStream = process.getInputStream();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = reader.readLine();
                while (line != null) {
                    for (final PythonProcessListener listener : getListeners()) {
                        listener.lineRead(serverProcess, line);
                    }
                    line = reader.readLine();
                }
            } catch (final IOException e) {
                // that fine
            } finally {
                for (final PythonProcessListener listener : getListeners()) {
                    listener.processEnded(serverProcess);
                }
            }
        }).start();
    }

    private List<PythonProcessListener> getListeners() {
        // copy for avoiding concurrent modification
        return newArrayList(PythonInterpretersCommandExecutors.getInstance().getListeners());
    }

    private void startStdErrReadingThread(final Process process, final Semaphore semaphore) {
        new Thread(() -> {
            try {
                semaphore.acquire();
            } catch (final InterruptedException e) {
                // that fine
            }
            final InputStream inputStream = process.getErrorStream();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = reader.readLine();
                while (line != null) {
                    for (final PythonProcessListener listener : getListeners()) {
                        listener.errorLineRead(serverProcess, line);
                    }
                    line = reader.readLine();
                }
            } catch (final IOException e) {
                // that fine
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

    private XmlRpcClient createClient(final String hostAndPort) {
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        try {
            config.setServerURL(new URL("http://" + hostAndPort));
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

    private void waitForConnectionToServer(final int timeoutInSec) {
        final long start = System.currentTimeMillis();
        while (true) {
            try {
                callRpcFunction("checkServerAvailability");
                break;
            } catch (final XmlRpcException e) {
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException ie) {
                    // we'll try once again
                }
            }
            if (System.currentTimeMillis() - start > (timeoutInSec * 1000)) {
                kill();
                break;
            }
        }
    }

    boolean isAlive() {
        return !isExternal() && serverProcess.isAlive();
    }

    boolean isExternal() {
        return isExternal;
    }

    void kill() {
        if (isAlive()) {
            try {
                new OSProcessHelper().destroyProcessTree(serverProcess);
            } catch (final ProcessHelperException e) {
                e.printStackTrace();
            }
            serverProcess.destroyForcibly();
            try {
                serverProcess.waitFor();
            } catch (final InterruptedException e) {
                throw new RobotCommandExecutorException("Unable to kill rpc server", e);
            }
        }
    }

    @Override
    public Map<String, Object> getVariables(final String filePath, final List<String> fileArguments) {
        try {
            final Map<String, Object> variables = new LinkedHashMap<>();
            final Map<?, ?> varToValueMapping = (Map<?, ?>) callRpcFunction("getVariables", filePath, fileArguments);
            for (final Entry<?, ?> entry : varToValueMapping.entrySet()) {
                variables.put((String) entry.getKey(), entry.getValue());
            }
            return variables;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public Map<String, Object> getGlobalVariables() {
        try {
            final Map<String, Object> variables = new LinkedHashMap<>();
            final Map<?, ?> varToValueMapping = (Map<?, ?>) callRpcFunction("getGlobalVariables");
            for (final Entry<?, ?> entry : varToValueMapping.entrySet()) {
                variables.put((String) entry.getKey(), entry.getValue());
            }
            return variables;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final List<String> libraries = newArrayList();
            final Object[] libs = (Object[]) callRpcFunction("getStandardLibrariesNames");
            for (final Object o : libs) {
                libraries.add((String) o);
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getStandardLibraryPath(final String libName) {
        try {
            return (String) callRpcFunction("getStandardLibraryPath", libName);
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getRobotVersion() {
        try {
            return (String) callRpcFunction("getRobotVersion");
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void createLibdocForStdLibrary(final String resultFilePath, final String libName, final String libPath) {
        createLibdoc(resultFilePath, libName, libPath, new EnvironmentSearchPaths());
    }

    @Override
    public void createLibdocForThirdPartyLibrary(final String resultFilePath, final String libName,
            final String libPath, final EnvironmentSearchPaths additionalPaths) {
        createLibdoc(resultFilePath, libName, libPath, additionalPaths);
    }

    private void createLibdoc(final String resultFilePath, final String libName, final String libPath,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            final String base64EncodedLibFileContent = (String) callRpcFunction("createLibdoc", libName,
                    newArrayList(additionalPaths.getExtendedPythonPaths(interpreterType)),
                    newArrayList(additionalPaths.getClassPaths()));
            final byte[] bytes = Base64.getDecoder().decode(base64EncodedLibFileContent);
            if (bytes.length > 0) {
                final File libdocFile = new File(resultFilePath);
                if (!libdocFile.exists()) {
                    libdocFile.createNewFile();
                }
                Files.write(bytes, libdocFile);
            }

        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        } catch (final IOException e) {
            final String additional = libPath.isEmpty() ? ""
                    : ". Library path '" + libPath + "', result file '" + resultFilePath + "'";
            throw new RobotEnvironmentException(
                    "Unable to generate library specification file for library '" + libName + "'" + additional, e);
        }
    }

    @Override
    public List<File> getModulesSearchPaths() {
        try {
            final List<File> libraries = newArrayList();
            final Object[] paths = (Object[]) callRpcFunction("getModulesSearchPaths");
            for (final Object o : paths) {
                if (!"".equals(o)) {
                    libraries.add(new File((String) o));
                }
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public Optional<File> getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        try {
            final String path = (String) callRpcFunction("getModulePath", moduleName,
                    newArrayList(additionalPaths.getExtendedPythonPaths(interpreterType)),
                    newArrayList(additionalPaths.getClassPaths()));
            return Optional.of(new File(path));
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<String> getClassesFromModule(final File moduleLocation, final String moduleName,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            final List<String> classes = newArrayList();
            final Object[] libs = (Object[]) callRpcFunction("getClassesFromModule", moduleLocation.getAbsolutePath(),
                    moduleName, newArrayList(additionalPaths.getExtendedPythonPaths(interpreterType)),
                    newArrayList(additionalPaths.getClassPaths()));
            for (final Object o : libs) {
                classes.add((String) o);
            }
            return classes;
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public boolean isVirtualenv() {
        try {
            return (boolean) callRpcFunction("isVirtualenv");
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final List<String> suiteNames,
            final List<String> variableMappings, final List<String> dataSourcePaths,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            callRpcFunction("startLibraryAutoDiscovering", port, suiteNames, variableMappings, dataSourcePaths,
                    newArrayList(additionalPaths.getExtendedPythonPaths(interpreterType)),
                    newArrayList(additionalPaths.getClassPaths()));
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void stopLibraryAutoDiscovering() {
        try {
            callRpcFunction("stopLibraryAutoDiscovering");
        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void runRfLint(final String host, final int port, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles) {
        try {
            final List<String> additionalArgs = new ArrayList<>();
            for (final String path : rulesFiles) {
                additionalArgs.add("-R");
                additionalArgs.add(path);
            }
            for (final RfLintRule rule : rules) {
                if (rule.hasChangedSeverity()) {
                    additionalArgs.add("-" + rule.getSeverity().severitySwitch());
                    additionalArgs.add(rule.getRuleName());
                }
                if (rule.hasConfigurationArguments()) {
                    additionalArgs.add("-c");
                    additionalArgs.add(rule.getRuleName() + ":" + rule.getConfiguration());
                }
            }
            callRpcFunction("runRfLint", host, port, filepath.getAbsolutePath(), additionalArgs);

        } catch (final XmlRpcException e) {
            throw new RobotEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    private Object callRpcFunction(final String functionName, final Object... arguments) throws XmlRpcException {
        final Object rpcResult = client.execute(functionName, arguments);
        return resultOrException(rpcResult);
    }

    private static Object resultOrException(final Object rpcCallResult) {
        final Map<?, ?> result = (Map<?, ?>) rpcCallResult;
        Preconditions.checkArgument(result.size() == 2);
        Preconditions.checkArgument(result.containsKey("result"));
        Preconditions.checkArgument(result.containsKey("exception"));

        if (result.get("exception") != null) {
            final String exception = (String) result.get("exception");
            final String indent = Strings.repeat(" ", 12);
            final String indentedException = indent + exception.replaceAll("\n", "\n" + indent);
            throw new RobotEnvironmentException("RED python session problem. Following exception has been thrown by "
                    + "python service:\n" + indentedException);
        }
        return result.get("result");
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
