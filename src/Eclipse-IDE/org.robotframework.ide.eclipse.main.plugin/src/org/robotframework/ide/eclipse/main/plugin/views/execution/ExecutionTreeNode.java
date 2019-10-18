/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.execution.agent.Status;

import com.google.common.base.Strings;

public class ExecutionTreeNode {

    public static ExecutionTreeNode newSuiteNode(final ExecutionTreeNode parent, final String name, final URI path) {
        return new ExecutionTreeNode(parent, ElementKind.SUITE, name, path, DynamicFlag.NONE, 0);
    }

    public static ExecutionTreeNode newTestNode(final ExecutionTreeNode parent, final String name, final URI path) {
        return new ExecutionTreeNode(parent, ElementKind.TEST, name, path, DynamicFlag.NONE, 1);
    }

    private final ExecutionTreeNode parent;

    private final List<ExecutionTreeNode> suites;
    private final List<ExecutionTreeNode> tests;

    private DynamicFlag dynamic;

    private final String name;
    private URI path;
    private final ElementKind kind;
    private int numberOfTests;

    private Status status;
    private String message;
    private int elapsedTime;


    private ExecutionTreeNode(final ExecutionTreeNode parent, final ElementKind kind, final String name,
            final URI path, final DynamicFlag dynamic, final int numberOfTests) {
        this.parent = parent;
        this.suites = new ArrayList<>();
        this.tests = new ArrayList<>();
        this.dynamic = dynamic;

        this.name = name;
        this.path = path;
        this.kind = kind;
        this.numberOfTests = numberOfTests;

        this.status = null;
        this.message = null;
        this.elapsedTime = -1;
    }

    ExecutionTreeNode getParent() {
        return parent;
    }

    public DynamicFlag getDynamic() {
        return dynamic;
    }

    void setDynamic(final DynamicFlag flag) {
        this.dynamic = flag;
    }

    public int getNumberOfTests() {
        return numberOfTests;
    }

    void setNumberOfTests(final int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public List<ExecutionTreeNode> getChildren() {
        return Stream.concat(suites.stream(), tests.stream()).collect(toList());
    }

    public void addChildren(final ExecutionTreeNode... children) {
        for (final ExecutionTreeNode child : children) {
            if (child.getKind() == ElementKind.SUITE) {
                suites.add(child);
            } else {
                tests.add(child);
            }
        }
    }

    public boolean hasSuites() {
        return !suites.isEmpty();
    }

    public boolean hasTests() {
        return !tests.isEmpty();
    }

    public ExecutionTreeNode getSuiteOrCreateIfMissing(final String name, final int totalTestsInSuite) {
        final ExecutionTreeNode suiteNode = suites.stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (suiteNode != null) {
            ensureIsProperlyOrdered(suites, suiteNode);
            return suiteNode;
        }
        final ExecutionTreeNode newNode = new ExecutionTreeNode(this, ElementKind.SUITE, name, null, DynamicFlag.ADDED,
                totalTestsInSuite);
        addChildren(newNode);
        ensureIsProperlyOrdered(suites, newNode);
        updateNumberOfTests(totalTestsInSuite);
        this.dynamic = DynamicFlag.OTHER;
        return newNode;
    }

    public ExecutionTreeNode getTestOrCreateIfMissing(final String name) {
        final ExecutionTreeNode testNode = tests.stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (testNode != null) {
            ensureIsProperlyOrdered(tests, testNode);
            return testNode;
        }
        final ExecutionTreeNode newNode = new ExecutionTreeNode(this, ElementKind.TEST, name, path, DynamicFlag.ADDED,
                1);
        addChildren(newNode);
        ensureIsProperlyOrdered(tests, newNode);
        updateNumberOfTests(1);
        this.dynamic = DynamicFlag.OTHER;
        return newNode;
    }

    private void ensureIsProperlyOrdered(final List<ExecutionTreeNode> children, final ExecutionTreeNode child) {
        final int index = children.indexOf(child);
        int firstWithoutStatus = 0;
        for (int i = 0; i < children.size(); i++) {
            if (!children.get(i).getStatus().isPresent()) {
                firstWithoutStatus = i;
                break;
            }
        }
        if (index != firstWithoutStatus) {
            children.add(firstWithoutStatus, children.remove(index));
            this.dynamic = DynamicFlag.OTHER;
        }
    }

    public int updateNumberOfTestsBalanceOnSuiteEnd() {
        final List<ExecutionTreeNode> executedNodes = new ArrayList<>();
        final List<ExecutionTreeNode> skippedNodes = new ArrayList<>();

        for (final ExecutionTreeNode child : getChildren()) {
            if (child.getStatus().isPresent()) {
                executedNodes.add(child);
            } else {
                skippedNodes.add(child);
            }
        }
        if (!skippedNodes.isEmpty()) {
            this.dynamic = DynamicFlag.OTHER;
            skippedNodes.forEach(n -> n.dynamic = DynamicFlag.REMOVED);
        }
        final int delta = getNumberOfTests()
                - executedNodes.stream().collect(summingInt(ExecutionTreeNode::getNumberOfTests));
        updateNumberOfTests(-delta);
        return delta;
    }

    private void updateNumberOfTests(final int delta) {
        ExecutionTreeNode current = this;
        current.numberOfTests += delta;
        while (current.getParent() != null) {
            current = current.getParent();
            current.numberOfTests += delta;
        }
    }

    public String getName() {
        return name;
    }

    public void setPath(final URI path) {
        this.path = path;
    }

    public URI getPath() {
        return path;
    }

    public ElementKind getKind() {
        return kind;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Optional<Status> getStatus() {
        return Optional.ofNullable(status);
    }

    public boolean isRunning() {
        return status != null && status == Status.RUNNING;
    }

    public boolean isPassed() {
        return status != null && status == Status.PASS;
    }

    public boolean isFailed() {
        return status != null && status == Status.FAIL;
    }

    public boolean isExecuted() {
        return status != null && status != Status.RUNNING;
    }

    void setMessage(final String message) {
        this.message = message;
    }

    String getMessage() {
        return Strings.nullToEmpty(message);
    }

    void setElapsedTime(final int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    int getElapsedTime() {
        return elapsedTime;
    }

    public static enum ElementKind {
        SUITE, TEST
    }
    
    public static enum DynamicFlag {
        NONE, ADDED, REMOVED, OTHER
    }
}
