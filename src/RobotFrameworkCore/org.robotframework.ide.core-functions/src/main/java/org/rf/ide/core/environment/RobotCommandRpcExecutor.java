/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.IRuntimeEnvironment.RuntimeEnvironmentException;
import org.rf.ide.core.jvmutils.process.OSProcessHelper;
import org.rf.ide.core.jvmutils.process.OSProcessHelper.ProcessHelperException;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
abstract class RobotCommandRpcExecutor implements RobotCommandExecutor {

    private static final int CONNECTION_TIMEOUT = 30;

    private final SuiteExecutor interpreterType;

    private XmlRpcClient client;

    RobotCommandRpcExecutor(final SuiteExecutor interpreterType) {
        this.interpreterType = interpreterType;
    }

    abstract void initialize();

    abstract void establishConnection();

    abstract boolean isAlive();

    abstract void kill();

    void connectToServer(final String url, final String interpreterPath) {
        try {
            client = createClient(new URL(url));
        } catch (final MalformedURLException e) {
            // can't happen here
        }
        waitForConnectionToServer(CONNECTION_TIMEOUT, interpreterPath);
    }

    private static XmlRpcClient createClient(final URL serverUrl) {
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(serverUrl);
        config.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
        config.setReplyTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcSun15HttpTransportFactory transportFactory = new XmlRpcSun15HttpTransportFactory(client);
        transportFactory.setProxy(Proxy.NO_PROXY);
        client.setTransportFactory(transportFactory);
        client.setConfig(config);
        client.setTypeFactory(new XmlRpcTypeFactoryWithNil(client));
        return client;
    }

    private void waitForConnectionToServer(final int timeoutInSec, final String interpreterPath) {
        final long start = System.currentTimeMillis();
        while (true) {
            try {
                callRpcFunction("checkServerAvailability", interpreterPath);
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

    @Override
    public Map<String, Object> getVariables(final File source, final List<String> arguments) {
        try {
            final Map<String, Object> variables = new LinkedHashMap<>();
            final Map<?, ?> varToValueMapping = (Map<?, ?>) callRpcFunction("getVariables", source.getAbsolutePath(),
                    arguments);
            for (final Entry<?, ?> entry : varToValueMapping.entrySet()) {
                variables.put((String) entry.getKey(), entry.getValue());
            }
            return variables;
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
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
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<String> getStandardLibrariesNames() {
        try {
            final List<String> libraries = new ArrayList<>();
            final Object[] libs = (Object[]) callRpcFunction("getStandardLibrariesNames");
            for (final Object o : libs) {
                libraries.add((String) o);
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public File getStandardLibraryPath(final String libName) {
        try {
            final String path = (String) callRpcFunction("getStandardLibraryPath", libName);
            return new File(path);
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<List<String>> getSitePackagesLibrariesNames() {
        try {
            final List<List<String>> libraries = new ArrayList<>();
            final Object[] objects = (Object[]) callRpcFunction("getSitePackagesLibrariesNames");
            for (final Object o : objects) {
                final Object[] libArray = (Object[]) o;
                final List<String> libs = new ArrayList<>();
                for (final Object lib : libArray) {
                    libs.add((String) lib);
                }
                libraries.add(libs);
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public String getRobotVersion() {
        try {
            return (String) callRpcFunction("getRobotVersion");
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void createLibdoc(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            final String base64EncodedLibFileContent = (String) callRpcFunction("createLibdoc", libName,
                    format.name().toLowerCase(), additionalPaths.getExtendedPythonPaths(interpreterType),
                    additionalPaths.getClassPaths());
            writeBase64EncodedLibdoc(outputFile, base64EncodedLibFileContent);
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        } catch (final IOException e) {
            throw new RuntimeEnvironmentException(
                    "Unable to generate library specification file for library '" + libName + "'", e);
        }
    }

    @Override
    public void createLibdocInSeparateProcess(final String libName, final File outputFile, final LibdocFormat format,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            final String base64EncodedLibFileContent = (String) callRpcFunction("createLibdocInSeparateProcess",
                    libName, format.name().toLowerCase(), additionalPaths.getExtendedPythonPaths(interpreterType),
                    additionalPaths.getClassPaths());
            if (!base64EncodedLibFileContent.isEmpty()) {
                writeBase64EncodedLibdoc(outputFile, base64EncodedLibFileContent);
            }
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        } catch (final IOException e) {
            throw new RuntimeEnvironmentException(
                    "Unable to generate library specification file for library '" + libName + "'", e);
        }
    }

    private static void writeBase64EncodedLibdoc(final File outputFile, final String encodedFileContent)
            throws IOException {
        final File libspecFolder = outputFile.getParentFile();
        if (!libspecFolder.exists()) {
            libspecFolder.mkdir();
        }
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        final byte[] decodedFileContent = Base64.getDecoder().decode(encodedFileContent);
        Files.write(decodedFileContent, outputFile);
    }

    @Override
    public String createHtmlDoc(final String doc, final DocFormat format) {
        try {
            return (String) callRpcFunction("createHtmlDoc", doc, format.name());
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<File> getModulesSearchPaths() {
        try {
            final List<File> libraries = new ArrayList<>();
            final Object[] paths = (Object[]) callRpcFunction("getModulesSearchPaths");
            for (final Object o : paths) {
                if (!"".equals(o)) {
                    libraries.add(new File((String) o));
                }
            }
            return libraries;
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public File getModulePath(final String moduleName, final EnvironmentSearchPaths additionalPaths) {
        try {
            final String path = (String) callRpcFunction("getModulePath", moduleName,
                    additionalPaths.getExtendedPythonPaths(interpreterType), additionalPaths.getClassPaths());
            return new File(path);
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<String> getClassesFromModule(final File moduleLocation, final EnvironmentSearchPaths additionalPaths) {
        try {
            final List<String> classes = new ArrayList<>();
            final Object[] libs = (Object[]) callRpcFunction("getClassesFromModule", moduleLocation.getAbsolutePath(),
                    additionalPaths.getExtendedPythonPaths(interpreterType), additionalPaths.getClassPaths());
            for (final Object o : libs) {
                classes.add((String) o);
            }
            return classes;
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void startLibraryAutoDiscovering(final int port, final File dataSource, final File projectLocation,
            final boolean supportGevent, final boolean recursiveInVirtualenv, final List<String> excludedPaths,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            callRpcFunction("startLibraryAutoDiscovering", port, dataSource.getAbsolutePath(),
                    projectLocation.getAbsolutePath(), supportGevent, recursiveInVirtualenv, excludedPaths,
                    additionalPaths.getExtendedPythonPaths(interpreterType), additionalPaths.getClassPaths());
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void startKeywordAutoDiscovering(final int port, final File dataSource, final boolean supportGevent,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            callRpcFunction("startKeywordAutoDiscovering", port, dataSource.getAbsolutePath(), supportGevent,
                    additionalPaths.getExtendedPythonPaths(interpreterType), additionalPaths.getClassPaths());
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void stopAutoDiscovering() {
        try {
            callRpcFunction("stopAutoDiscovering");
        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public List<RfLintRule> getRfLintRules(final List<String> rulesFiles) {
        try {
            final List<RfLintRule> rules = new ArrayList<>();
            final Object[] result = (Object[]) callRpcFunction("getRfLintRules", rulesFiles);

            for (final Object resultRule : result) {
                final Object[] ruleArray = (Object[]) resultRule;
                final String severity = (String) ruleArray[0];
                final String name = (String) ruleArray[1];
                final String documentation = (String) ruleArray[2];
                rules.add(new RfLintRule(name, RfLintViolationSeverity.from(severity), documentation));
            }
            return rules;

        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    @Override
    public void runRfLint(final String host, final int port, final File projectLocation,
            final List<String> excludedPaths, final File filepath, final List<RfLintRule> rules,
            final List<String> rulesFiles, final List<String> additionalArguments) {
        try {
            callRpcFunction("runRfLint", host, port, projectLocation.getAbsolutePath(), excludedPaths,
                    filepath.getAbsolutePath(), createRfLintArguments(rules, rulesFiles, additionalArguments));

        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
        }
    }

    private static List<String> createRfLintArguments(final List<RfLintRule> rules, final List<String> rulesFiles,
            final List<String> additionalArguments) {
        final List<String> arguments = new ArrayList<>();
        for (final String path : rulesFiles) {
            arguments.add("-R");
            arguments.add(path);
        }
        for (final RfLintRule rule : rules) {
            arguments.addAll(rule.getConfigurationSwitches());
        }
        arguments.addAll(additionalArguments);
        return arguments;
    }

    @Override
    public String convertRobotDataFile(final File originalFile) {
        try {
            final String b64encodedContent = (String) callRpcFunction("convertRobotDataFile",
                    originalFile.getAbsolutePath());
            final byte[] bytes = Base64.getDecoder().decode(b64encodedContent);
            return new String(bytes, Charsets.UTF_8);

        } catch (final XmlRpcException e) {
            throw new RuntimeEnvironmentException("Unable to communicate with XML-RPC server", e);
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
            throw new RuntimeEnvironmentException("Following exception has been thrown:\n" + indentedException);
        }
        return result.get("result");
    }

    static class InternalRobotCommandRpcExecutor extends RobotCommandRpcExecutor {

        private static final List<String> SCRIPT_FILES = ImmutableList.of("robot_session_server.py",
                "classpath_updater.py", "red_keyword_autodiscover.py", "red_libraries.py",
                "red_library_autodiscover.py", "red_module_classes.py", "red_modules.py", "red_variables.py",
                "rflint_integration.py", "SuiteVisitorImportProxy.py", "TestRunnerAgent.py");

        private final String interpreterPath;

        private final Supplier<List<PythonProcessListener>> processListeners;

        private final List<File> serverScripts = new ArrayList<>();

        private XmlRpcServer server;

        InternalRobotCommandRpcExecutor(final SuiteExecutor interpreterType, final String interpreterPath,
                final Supplier<List<PythonProcessListener>> processListeners) {
            super(interpreterType);
            this.interpreterPath = interpreterPath;
            this.processListeners = processListeners;
        }

        @Override
        void initialize() {
            if (serverScripts.isEmpty()) {
                serverScripts.addAll(copyScripts());
            } else if (!serverScripts.stream().allMatch(File::exists)) {
                serverScripts.clear();
                serverScripts.addAll(copyScripts());
            }
        }

        @VisibleForTesting
        static List<File> copyScripts() {
            try {
                final List<File> files = new ArrayList<>();
                for (final String scriptFile : SCRIPT_FILES) {
                    files.add(RedTemporaryDirectory.copyScriptFile(scriptFile));
                }
                return files;
            } catch (final IOException e) {
                throw new XmlRpcServerException("Unable to create temporary directory for XML-RPC server", e);
            }
        }

        @Override
        void establishConnection() {
            final String serverPath = serverScripts.get(0).getPath();
            final int port = findFreePort();
            server = new XmlRpcServer();
            try {
                server.start(serverPath, port);
            } catch (final IOException e) {
                throw new XmlRpcServerException(
                        String.format("Unable to start XML-RPC server (interpreter: %s, server: %s, port: %d)",
                                interpreterPath, serverPath, port),
                        e);
            }

            final String url = "http://127.0.0.1:" + port;
            connectToServer(url, interpreterPath);

            if (!isAlive()) {
                throw new XmlRpcServerException(
                        String.format("Unable to start XML-RPC server (interpreter: %s, server: %s, port: %d)",
                                interpreterPath, serverPath, port));
            }
        }

        private static int findFreePort() {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            } catch (final IOException e) {
                throw new XmlRpcServerException("Unable to find free port for XML-RPC server", e);
            }
        }

        @Override
        boolean isAlive() {
            return server != null && server.isAlive();
        }

        @Override
        void kill() {
            if (isAlive()) {
                try {
                    server.kill();
                } catch (final InterruptedException e) {
                    throw new XmlRpcServerException("Unable to kill XML-RPC server", e);
                }
            }
        }

        private class XmlRpcServer {

            private Process process;

            private void start(final String serverPath, final int port) throws IOException {
                final ProcessBuilder processBuilder = new ProcessBuilder(interpreterPath, serverPath,
                        String.valueOf(port));
                processBuilder.environment().put("PYTHONIOENCODING", "utf8");
                process = processBuilder.start();

                final Semaphore semaphore = new Semaphore(0);
                startStdOutReadingThread(semaphore);
                startStdErrReadingThread(semaphore);
            }

            private boolean isAlive() {
                return process != null && process.isAlive();
            }

            private void kill() throws InterruptedException {
                if (isAlive()) {
                    try {
                        new OSProcessHelper().destroyProcessTree(process);
                    } catch (final ProcessHelperException e) {
                        e.printStackTrace();
                    }
                    process.destroyForcibly();
                    process.waitFor();
                }
            }

            private void startStdOutReadingThread(final Semaphore semaphore) {
                new Thread(() -> {
                    for (final PythonProcessListener listener : processListeners.get()) {
                        listener.processStarted(interpreterPath, process);
                    }
                    semaphore.release();
                    final InputStream inputStream = process.getInputStream();
                    try (final BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, Charsets.UTF_8))) {
                        String line = reader.readLine();
                        while (line != null) {
                            for (final PythonProcessListener listener : processListeners.get()) {
                                listener.lineRead(process, line);
                            }
                            line = reader.readLine();
                        }
                    } catch (final IOException e) {
                        // ignore it
                    } finally {
                        for (final PythonProcessListener listener : processListeners.get()) {
                            listener.processEnded(process);
                        }
                    }
                }).start();
            }

            private void startStdErrReadingThread(final Semaphore semaphore) {
                new Thread(() -> {
                    try {
                        semaphore.acquire();
                    } catch (final InterruptedException e) {
                        // ignore it
                    }
                    final InputStream inputStream = process.getErrorStream();
                    try (final BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, Charsets.UTF_8))) {
                        String line = reader.readLine();
                        while (line != null) {
                            for (final PythonProcessListener listener : processListeners.get()) {
                                listener.errorLineRead(process, line);
                            }
                            line = reader.readLine();
                        }
                    } catch (final IOException e) {
                        // ignore it
                    }
                }).start();
            }

        }

        @SuppressWarnings("serial")
        private static class XmlRpcServerException extends RuntimeException {

            private XmlRpcServerException(final String message) {
                super(message);
            }

            private XmlRpcServerException(final String message, final Throwable cause) {
                super(message, cause);
            }
        }

    }

    static class ExternalRobotCommandRpcExecutor extends RobotCommandRpcExecutor {

        private final String serverAndPort;

        ExternalRobotCommandRpcExecutor(final SuiteExecutor interpreterType, final String serverAndPort) {
            super(interpreterType);
            this.serverAndPort = serverAndPort;
        }

        @Override
        void initialize() {
            // nothing to do
        }

        @Override
        void establishConnection() {
            connectToServer("http://" + serverAndPort, "");
        }

        @Override
        boolean isAlive() {
            return true;
        }

        @Override
        void kill() {
            // nothing to do
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
