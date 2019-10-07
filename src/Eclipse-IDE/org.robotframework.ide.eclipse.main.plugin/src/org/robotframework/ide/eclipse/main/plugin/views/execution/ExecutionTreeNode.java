/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.execution.agent.Status;

import com.google.common.base.Strings;

public class ExecutionTreeNode {

    private final ExecutionTreeNode parent;
    private final List<ExecutionTreeNode> children;
    private List<String> childPaths;

    private final String name;
    private URI path;
    private final ElementKind kind;

    private Status status;
    private String message;
    private int elapsedTime;

    public ExecutionTreeNode(final ExecutionTreeNode parent, final ElementKind kind, final String name) {
        this(parent, kind, name, null, new ArrayList<>());
    }

    public ExecutionTreeNode(final ExecutionTreeNode parent, final ElementKind kind, final String name,
            final URI path, final List<String> childPaths) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.childPaths = childPaths;

        this.name = name;
        this.path = path;
        this.kind = kind;

        this.status = null;
        this.message = null;
        this.elapsedTime = -1;
    }

    ExecutionTreeNode getParent() {
        return parent;
    }

    public List<ExecutionTreeNode> getChildren() {
        return children;
    }

    public void addChildren(final Collection<ExecutionTreeNode> children) {
        this.children.addAll(children);
    }

    public List<String> getChildrenPaths() {
        return childPaths;
    }

    public void setChildrenPaths(final List<String> childPaths) {
        this.childPaths = childPaths;
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
        SUITE,
        TEST
    }

}
