/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.rf.ide.core.RedTemporaryDirectory;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.ExternalRobotCommandRpcExecutor;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.InternalRobotCommandRpcExecutor;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.InternalRobotCommandRpcExecutor.XmlRpcServer;
import org.rf.ide.core.environment.RobotCommandRpcExecutor.InternalRobotCommandRpcExecutor.XmlRpcServerException;

public class RobotCommandRpcExecutorTest {

    @Nested
    class ExternalRobotCommandRpcExecutorTest {

        @Test
        void serverIsStartedForPythonInterpreter() throws Exception {
            final RobotCommandRpcExecutor executor = spy(
                    new ExternalRobotCommandRpcExecutor(SuiteExecutor.Python, "a.b.c.d:12345"));
            doNothing().when(executor).connectToServer(anyString(), anyString());

            executor.establishConnection();

            verify(executor).connectToServer("http://a.b.c.d:12345", "");
        }

    }

    @Nested
    class InternalRobotCommandRpcExecutorTest {

        @BeforeEach
        void beforeTest() throws Exception {
            createSessionServerFiles();
        }

        @AfterEach
        void afterTest() throws Exception {
            deleteSessionServerFiles();
        }

        @Test
        void serverIsNotStartedWhenFreePortCannotBeFound() throws Exception {
            final XmlRpcServer server = mock(XmlRpcServer.class);
            doThrow(IOException.class).when(server).findFreePort();

            final RobotCommandRpcExecutor executor = new InternalRobotCommandRpcExecutor(SuiteExecutor.Python,
                    "path/to/python", server);

            assertThatExceptionOfType(XmlRpcServerException.class).isThrownBy(executor::establishConnection)
                    .withMessage("Unable to find free port for XML-RPC server")
                    .withCauseExactlyInstanceOf(IOException.class);
        }

        @Test
        void serverIsNotStartedWhenServerFileCannotBeFound() throws Exception {
            deleteSessionServerFiles();

            final XmlRpcServer server = mock(XmlRpcServer.class);
            doReturn(12345).when(server).findFreePort();

            final RobotCommandRpcExecutor executor = new InternalRobotCommandRpcExecutor(SuiteExecutor.Python,
                    "path/to/python", server);

            assertThatExceptionOfType(XmlRpcServerException.class).isThrownBy(executor::establishConnection)
                    .withMessage("Unable to find XML-RPC server file with name '%s'",
                            RedTemporaryDirectory.ROBOT_SESSION_SERVER)
                    .withNoCause();
        }

        @Test
        void serverIsNotStartedWhenProcessCannotBeStarted() throws Exception {
            final XmlRpcServer server = mock(XmlRpcServer.class);
            doReturn(12345).when(server).findFreePort();
            doThrow(IOException.class).when(server).start(any());

            final RobotCommandRpcExecutor executor = new InternalRobotCommandRpcExecutor(SuiteExecutor.Python,
                    "path/to/python", server);

            assertThatExceptionOfType(XmlRpcServerException.class).isThrownBy(executor::establishConnection)
                    .withMessage("Unable to start XML-RPC server using command: %s %s %s", "path/to/python",
                            getServerPath(), "12345")
                    .withCauseExactlyInstanceOf(IOException.class);
        }

        @Test
        void serverIsStartedForPythonInterpreter() throws Exception {
            final XmlRpcServer server = mock(XmlRpcServer.class);
            doReturn(12345).when(server).findFreePort();

            final RobotCommandRpcExecutor executor = spy(
                    new InternalRobotCommandRpcExecutor(SuiteExecutor.Python, "path/to/python", server));
            doNothing().when(executor).connectToServer(anyString(), anyString());

            executor.establishConnection();

            final InOrder inOrder = inOrder(server, executor);
            inOrder.verify(server).start("path/to/python", getServerPath(), "12345");
            inOrder.verify(executor).connectToServer("http://127.0.0.1:12345", "path/to/python");
            inOrder.verify(server).verifyStart();
        }

        @Test
        void serverIsStartedForJythonInterpreter() throws Exception {
            final XmlRpcServer server = mock(XmlRpcServer.class);
            doReturn(12345).when(server).findFreePort();

            final RobotCommandRpcExecutor executor = spy(
                    new InternalRobotCommandRpcExecutor(SuiteExecutor.Jython, "path/to/jython", server));
            doNothing().when(executor).connectToServer(anyString(), anyString());

            executor.establishConnection();

            final InOrder inOrder = inOrder(server, executor);
            inOrder.verify(server).start("path/to/jython", "-J-javaagent:" + getAgentPath(), getServerPath(), "12345");
            inOrder.verify(executor).connectToServer("http://127.0.0.1:12345", "path/to/jython");
            inOrder.verify(server).verifyStart();
        }

        private void createSessionServerFiles() throws IOException {
            RedTemporaryDirectory.createSessionServerFiles();
        }

        private void deleteSessionServerFiles() throws IOException {
            Arrays.stream(RedTemporaryDirectory.createTemporaryDirectoryIfNotExists().toFile().listFiles())
                    .forEach(File::delete);
        }

        private String getServerPath() throws IOException {
            return RedTemporaryDirectory.getTemporaryFile(RedTemporaryDirectory.ROBOT_SESSION_SERVER).getPath();
        }

        private String getAgentPath() throws IOException {
            return RedTemporaryDirectory.getTemporaryFile(RedTemporaryDirectory.CLASS_PATH_UPDATER).getPath();
        }
    }
}
