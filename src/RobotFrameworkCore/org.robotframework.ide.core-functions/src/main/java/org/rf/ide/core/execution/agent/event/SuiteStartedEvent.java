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

    public static SuiteStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);

        final URI suiteFilePath = Events.toFileUri((String) attributes.get("source"));
        final Boolean isDir = (Boolean) attributes.get("is_dir");
        final List<?> childSuites = (List<?>) attributes.get("suites");
        final List<?> childTests = (List<?>) attributes.get("tests");
        final Integer totalTests = (Integer) attributes.get("totaltests");

        if (isDir == null || childSuites == null || childTests == null || totalTests == null) {
            throw new IllegalArgumentException("Suite started event should have path, directory/file flag, children "
                    + "suites and tests as well as number of total tests");
        }

        return new SuiteStartedEvent(name, suiteFilePath, isDir, totalTests, Events.ensureListOfStrings(childSuites),
                Events.ensureListOfStrings(childTests));
    }


    private final String name;

    private final URI suiteFilePath;

    private final boolean isDir;

    private final int totalTests;

    private final List<String> childSuites;

    private final List<String> childTests;

    public SuiteStartedEvent(final String name, final URI suiteFilePath, final boolean isDir, final int totalTests,
            final List<String> childSuites, final List<String> childTests) {
        this.name = name;
        this.suiteFilePath = suiteFilePath;
        this.isDir = isDir;
        this.totalTests = totalTests;
        this.childSuites = childSuites;
        this.childTests = childTests;
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

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == SuiteStartedEvent.class) {
            final SuiteStartedEvent that = (SuiteStartedEvent) obj;
            return this.name.equals(that.name) && this.suiteFilePath.equals(that.suiteFilePath)
                    && this.isDir == that.isDir && this.totalTests == that.totalTests
                    && this.childSuites.equals(that.childSuites) && this.childTests.equals(that.childTests);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, suiteFilePath, isDir, totalTests, childSuites, childTests);
    }
}