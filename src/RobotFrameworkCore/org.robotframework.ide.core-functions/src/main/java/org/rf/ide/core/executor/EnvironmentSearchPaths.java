/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * @author Michal Anglart
 */
public final class EnvironmentSearchPaths {

    private final Set<String> classPaths = new LinkedHashSet<>();

    private final Set<String> pythonPaths = new LinkedHashSet<>();

    public EnvironmentSearchPaths() {
        this(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    public EnvironmentSearchPaths(final Collection<String> classPaths, final Collection<String> pythonPaths) {
        this.classPaths.addAll(classPaths);
        this.pythonPaths.addAll(pythonPaths);
    }

    public Collection<String> getClassPaths() {
        return ImmutableSet.copyOf(classPaths);
    }

    public boolean hasClassPaths() {
        return !classPaths.isEmpty();
    }

    public void addClassPath(final String path) {
        classPaths.add(path);
    }

    public void removeClassPath(final String path) {
        classPaths.remove(path);
    }

    public Collection<String> getPythonPaths() {
        return ImmutableSet.copyOf(pythonPaths);
    }

    public boolean hasPythonPaths() {
        return !pythonPaths.isEmpty();
    }

    public void addPythonPath(final String path) {
        pythonPaths.add(path);
    }

    public void removePythonPath(final String path) {
        pythonPaths.remove(path);
    }

    public Collection<String> getExtendedPythonPaths(final SuiteExecutor interpreter) {
        final Set<String> extendedPythonPaths = new LinkedHashSet<>(pythonPaths);
        if (interpreter == SuiteExecutor.Jython || interpreter == SuiteExecutor.IronPython) {
            // Both Jython and IronPython does not include paths from PYTHONPATH into sys.path list
            extendedPythonPaths.addAll(RedSystemProperties.getPythonPaths());
        }
        return ImmutableSet.copyOf(extendedPythonPaths);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof EnvironmentSearchPaths) {
            final EnvironmentSearchPaths that = (EnvironmentSearchPaths) obj;
            return this.classPaths.equals(that.classPaths) && this.pythonPaths.equals(that.pythonPaths);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(classPaths, pythonPaths);
    }
}
