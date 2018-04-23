/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ProtocolVersion;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.google.common.base.Objects;

public final class VersionsEvent {

    public static VersionsEvent from(final AgentClient client, final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("version");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(0);
        final String cmdLine = (String) attributes.get("cmd_line");
        final String pythonVersion = (String) attributes.get("python");
        final String robotVersion = (String) attributes.get("robot");
        final Integer protocolVersion = (Integer) attributes.get("protocol");
        final Optional<Long> pid;
        if (attributes.get("pid") instanceof Integer) {
            final Integer i = (Integer) attributes.get("pid");
            pid = Optional.of(Long.valueOf(i.intValue()));
        } else {
            pid = Optional.ofNullable((Long) attributes.get("pid"));
        }

        if (cmdLine == null || pythonVersion == null || robotVersion == null || protocolVersion == null) {
            throw new IllegalArgumentException(
                    "Versions event should have command line, versions of python, robot and protocol");
        }
        return new VersionsEvent(new VersionsEventResponder(client), cmdLine, pythonVersion, robotVersion,
                protocolVersion, pid);
    }


    private final VersionsEventResponder responder;

    private final String cmdLine;

    private final String pythonVersion;

    private final String robotVersion;

    private final int protocolVersion;

    private final Optional<Long> pid;

    public VersionsEvent(final VersionsEventResponder responder, final String cmdLine, final String pythonVersion,
            final String robotVersion, final int protocolVersion, final Optional<Long> pid) {
        this.responder = responder;
        this.cmdLine = cmdLine;
        this.pythonVersion = pythonVersion;
        this.robotVersion = robotVersion;
        this.protocolVersion = protocolVersion;
        this.pid = pid;
    }

    public String getCommandLine() {
        return cmdLine;
    }

    public String getPythonVersion() {
        return pythonVersion;
    }

    public String getRobotVersion() {
        return robotVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Optional<Long> getPid() {
        return pid;
    }

    public VersionsEventResponder responder() {
        return responder;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == VersionsEvent.class) {
            final VersionsEvent that = (VersionsEvent) obj;
            return this.cmdLine.equals(that.cmdLine) && this.pythonVersion.equals(that.pythonVersion)
                    && this.robotVersion.equals(that.robotVersion) && this.protocolVersion == that.protocolVersion
                    && this.pid.equals(that.pid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cmdLine, pythonVersion, robotVersion, protocolVersion, pid);
    }

    public static class VersionsEventResponder {

        private final AgentClient client;

        private VersionsEventResponder(final AgentClient client) {
            this.client = client;
        }

        public void versionsCorrect() throws ResponseException {
            client.send(new ProtocolVersion());
        }

        public void versionsError(final String error) throws ResponseException {
            client.send(new ProtocolVersion(error));
        }
    }

}
