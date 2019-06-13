/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;

class NewRedPyDevConfigWizardData {

    static final String DEFAULT_ADDRESS = "127.0.0.1";
    static final int DEFAULT_PORT = 5678;

    private IRuntimeEnvironment redEnvironment = null;

    private boolean isUsingPydevdFromInterpreter = false;
    private File pydevdLocation = null;
    private String clientAddress = null;
    private Integer clientPort = null;

    private boolean isUsingRedpydevdFromInterpreter;
    private File redpydevdLocation;
    private boolean requiresInstallation;
    private boolean requiresExport;

    private boolean geventSupport;

    boolean isInterpreterChosen() {
        return redEnvironment != null;
    }

    void setRedEnvironment(final IRuntimeEnvironment redEnvironment) {
        if (!Objects.equals(this.redEnvironment, redEnvironment)) {
            this.isUsingPydevdFromInterpreter = false;
            this.pydevdLocation = null;
            this.isUsingRedpydevdFromInterpreter = false;
            this.redpydevdLocation = null;
            this.requiresInstallation = false;
            this.requiresExport = false;
        }
        this.redEnvironment = redEnvironment;
    }

    IRuntimeEnvironment getRedEnvironment() {
        return redEnvironment;
    }

    void setPydevdLocation(final File pydevdFile) {
        this.isUsingPydevdFromInterpreter = false;
        this.pydevdLocation = pydevdFile;
    }

    void setUsePydevdFromInterpreter() {
        this.isUsingPydevdFromInterpreter = true;
        this.pydevdLocation = null;
    }

    void setAddress(final String address) {
        this.clientAddress = address;
    }

    void setPort(final Integer port) {
        this.clientPort = port;
    }

    void setUseRedpydevdFromInterpreter() {
        this.isUsingRedpydevdFromInterpreter = true;
        this.redpydevdLocation = null;
    }

    void setRedpydevdLocation(final File redpydevdLocation) {
        this.isUsingRedpydevdFromInterpreter = false;
        this.redpydevdLocation = redpydevdLocation;
    }

    void setRedpydevdRequiresInstallation(final boolean requiresInstallation) {
        this.requiresInstallation = requiresInstallation;
        this.requiresExport = false;
    }

    void setRedpydevdRequiresExport(final boolean requiresExport) {
        this.requiresInstallation = false;
        this.requiresExport = requiresExport;
    }

    void setGeventSupport(final boolean geventSupport) {
        this.geventSupport = geventSupport;
    }

    File getPydevdLocation() {
        return pydevdLocation;
    }

    boolean isUsingPydevdFromInterpreter() {
        return isUsingPydevdFromInterpreter;
    }

    File getRedpydevdLocation() {
        return redpydevdLocation;
    }

    boolean isUsingRedpydevdFromInterpreter() {
        return isUsingRedpydevdFromInterpreter;
    }

    boolean requiresRedpydevdInstallation() {
        return requiresInstallation;
    }

    boolean requiresRedpydevdExport() {
        return requiresExport;
    }

    boolean getGeventSupport() {
        return geventSupport;
    }

    String createPythonExecutablePath() {
        final SuiteExecutor interpreter = Optional.of(redEnvironment)
                .map(IRuntimeEnvironment::getInterpreter)
                .orElse(SuiteExecutor.Python);

        return Optional.of(redEnvironment)
                .map(IRuntimeEnvironment::getFile)
                .map(File::listFiles)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .filter(f -> interpreter.executableName().equals(f.getName()))
                .map(File::getAbsolutePath)
                .findFirst()
                .orElseGet(interpreter::executableName);
    }

    List<String> createArguments() {
        final List<String> arguments = new ArrayList<>();
        if (isUsingRedpydevdFromInterpreter) {
            arguments.add("-m");
            arguments.add("redpydevd");
        } else {
            arguments.add(redpydevdLocation.getAbsolutePath());
        }
        if (!isUsingPydevdFromInterpreter) {
            arguments.add("--pydevd");
            arguments.add(pydevdLocation.getAbsolutePath());
        }
        if (clientAddress != null && !clientAddress.equals(DEFAULT_ADDRESS)) {
            arguments.add("--client");
            arguments.add(clientAddress);
        }
        if (clientPort != null && !clientPort.equals(Integer.valueOf(DEFAULT_PORT))) {
            arguments.add("--port");
            arguments.add(clientPort.toString());
        }
        return arguments;
    }
}
