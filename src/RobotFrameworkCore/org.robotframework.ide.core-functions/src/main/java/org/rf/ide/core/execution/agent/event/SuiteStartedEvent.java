/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public final class SuiteStartedEvent {

    private final String name;

    private final URI suiteFilePath;

    private final boolean isDir;

    private final int totalTests;

    private final List<String> childSuites;

    private final List<String> childTests;

    private final List<Map<Variable, VariableTypedValue>> variables;


    public static SuiteStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);

        final URI suiteFilePath = Events.toFileUri((String) attributes.get("source"));
        final boolean isDir = (Boolean) attributes.get("is_dir");
        final List<String> childSuites = Events.ensureListOfStrings((List<?>) attributes.get("suites"));
        final List<String> childTests = Events.ensureListOfStrings((List<?>) attributes.get("tests"));
        final int totalTests = (Integer) attributes.get("totaltests");
        final List<Map<Variable, VariableTypedValue>> variables = Events
                .extractVariableScopes((List<?>) attributes.get("vars_scopes"));

        return new SuiteStartedEvent(name, suiteFilePath, isDir, totalTests, childSuites, childTests, variables);
    }

    public SuiteStartedEvent(final String name, final URI suiteFilePath, final boolean isDir, final int totalTests,
            final List<String> childSuites, final List<String> childTests,
            final List<Map<Variable, VariableTypedValue>> variables) {
        this.name = name;
        this.suiteFilePath = suiteFilePath;
        this.isDir = isDir;
        this.totalTests = totalTests;
        this.childSuites = childSuites;
        this.childTests = childTests;
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public URI getPath() {
        return suiteFilePath;
    }

    public boolean isDirectory() {
        return isDir;
    }

    public int getNumberOfTests() {
        return totalTests;
    }

    public ImmutableList<String> getChildrenSuites() {
        return ImmutableList.copyOf(childSuites);
    }

    public ImmutableList<String> getChildrenTests() {
        return ImmutableList.copyOf(childTests);
    }

    public List<Map<Variable, VariableTypedValue>> getVariables() {
        return variables;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == SuiteStartedEvent.class) {
            final SuiteStartedEvent that = (SuiteStartedEvent) obj;
            return this.name.equals(that.name) && this.suiteFilePath.equals(that.suiteFilePath)
                    && this.isDir == that.isDir && this.totalTests == that.totalTests
                    && this.childSuites.equals(that.childSuites) && this.childTests.equals(that.childTests)
                    && this.variables.equals(that.variables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, suiteFilePath, isDir, totalTests, childSuites, childTests, variables);
    }
}