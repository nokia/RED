/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

public final class SuiteStartedEvent {

    public static SuiteStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);

        final URI suiteFilePath = Events.toFileUri((String) attributes.get("source"));
        final Boolean isDir = (Boolean) attributes.get("is_dir");
        final List<?> childTests = (List<?>) attributes.get("tests");
        final Integer totalTests = (Integer) attributes.get("totaltests");
        final List<?> childSuites = (List<?>) attributes.get("suites");
        final List<?> childPaths = (List<?>) attributes.get("child_paths");
        ExecutionMode mode;
        if (attributes.get("is_rpa") instanceof Boolean) {
            final boolean isRpa = ((Boolean) attributes.get("is_rpa")).booleanValue();
            mode = isRpa ? ExecutionMode.TASKS : ExecutionMode.TESTS;
        } else {
            mode = ExecutionMode.TESTS;
        }

        if (isDir == null || childSuites == null || childTests == null || totalTests == null) {
            throw new IllegalArgumentException("Suite started event should have directory/file flag, children "
                    + "suites and tests as well as number of total tests");
        }
        final List<Optional<URI>> paths;
        if (!childPaths.isEmpty()) {
            paths = Events.ensureListOfStrings(childPaths)
                    .stream()
                    .map(Optional::ofNullable)
                    .map(o -> o.map(Events::toFileUri))
                    .collect(toList());
        } else {
            paths = Stream.generate(Optional::<URI> empty).limit(childSuites.size()).collect(toList());
        }
        return new SuiteStartedEvent(name, suiteFilePath, isDir, mode, totalTests,
                Events.ensureListOfStrings(childSuites), Events.ensureListOfStrings(childTests), paths);
    }


    private final String name;

    private final URI suiteFilePath;

    private final boolean isDir;

    private final ExecutionMode mode;

    private final int totalTests;

    private final List<String> childSuites;

    private final List<String> childTests;

    private final List<Optional<URI>> childPaths;

    public SuiteStartedEvent(final String name, final URI suiteFilePath, final boolean isDir, final ExecutionMode mode,
            final int totalTests, final List<String> childSuites, final List<String> childTests,
            final List<Optional<URI>> childPaths) {
        this.name = name;
        this.suiteFilePath = suiteFilePath;
        this.isDir = isDir;
        this.mode = mode;
        this.totalTests = totalTests;
        this.childSuites = childSuites;
        this.childTests = childTests;
        this.childPaths = childPaths;
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

    public ExecutionMode getMode() {
        return mode;
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

    public List<Optional<URI>> getChildrenPaths() {
        return childPaths;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == SuiteStartedEvent.class) {
            final SuiteStartedEvent that = (SuiteStartedEvent) obj;
            return this.name.equals(that.name) && this.suiteFilePath.equals(that.suiteFilePath)
                    && this.isDir == that.isDir && this.mode == that.mode && this.totalTests == that.totalTests
                    && this.childSuites.equals(that.childSuites) && this.childTests.equals(that.childTests)
                    && this.childPaths.equals(that.childPaths);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, suiteFilePath, isDir, mode, totalTests, childSuites, childTests, childPaths);
    }

    public static enum ExecutionMode {
        TESTS, TASKS
    }
}