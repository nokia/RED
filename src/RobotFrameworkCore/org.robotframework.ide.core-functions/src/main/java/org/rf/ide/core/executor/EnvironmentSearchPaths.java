/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * @author Michal Anglart
 *
 */
public final class EnvironmentSearchPaths {

    private final Set<String> additionalClassPaths = new HashSet<>();

    private final Set<String> additionalPythonPaths = new HashSet<>();

    public EnvironmentSearchPaths() {
        this(new ArrayList<String>(), new ArrayList<String>());
    }

    public EnvironmentSearchPaths(final Collection<String> classPaths, final Collection<String> pythonPaths) {
        this.additionalClassPaths.addAll(classPaths);
        this.additionalPythonPaths.addAll(pythonPaths);
    }

    public Collection<String> getClassPaths() {
        return ImmutableSet.copyOf(additionalClassPaths);
    }

    public boolean hasClassPaths() {
        return !additionalClassPaths.isEmpty();
    }

    public void addClassPath(final String path) {
        additionalClassPaths.add(path);
    }

    public void removeClassPath(final String path) {
        additionalClassPaths.remove(path);
    }

    public Collection<String> getPythonPaths() {
        return ImmutableSet.copyOf(additionalPythonPaths);
    }

    public boolean hasPythonPaths() {
        return !additionalPythonPaths.isEmpty();
    }

    public void addPythonPath(final String path) {
        additionalPythonPaths.add(path);
    }

    public void removePythonPath(final String path) {
        additionalPythonPaths.remove(path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof EnvironmentSearchPaths) {
            final EnvironmentSearchPaths that = (EnvironmentSearchPaths) obj;
            return this.additionalClassPaths.equals(that.additionalClassPaths)
                    && this.additionalPythonPaths.equals(that.additionalPythonPaths);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(additionalClassPaths, additionalPythonPaths);
    }
}
