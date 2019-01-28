/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.environment.SuiteExecutor;


public class NullRobotProjectConfig extends RobotProjectConfig {

    @Override
    public boolean isNullConfig() {
        return true;
    }

    @Override
    public void setVersion(final String version) {
        // nothing to do
    }

    @Override
    public ConfigVersion getVersion() {
        return ConfigVersion.create(RobotProjectConfig.CURRENT_VERSION);
    }

    @Override
    public void setExecutionEnvironment(final ExecutionEnvironment executionEnvironment) {
        // nothing to do
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return null;
    }

    @Override
    public void setRelativityPoint(final RelativityPoint relativityPoint) {
        // nothing to do
    }

    @Override
    public RelativityPoint getRelativityPoint() {
        return RelativityPoint.create(RelativeTo.WORKSPACE);
    }

    @Override
    public void setReferencedLibraries(final List<ReferencedLibrary> libraries) {
        // nothing to do
    }

    @Override
    public List<ReferencedLibrary> getReferencedLibraries() {
        return new ArrayList<>();
    }

    @Override
    public boolean addReferencedLibrary(final ReferencedLibrary library) {
        return false;
    }

    @Override
    public boolean removeReferencedLibraries(final List<ReferencedLibrary> libraries) {
        return false;
    }

    @Override
    public void setRemoteLocations(final List<RemoteLocation> remoteLocations) {
        // nothing to do
    }

    @Override
    public List<RemoteLocation> getRemoteLocations() {
        return new ArrayList<>();
    }

    @Override
    public boolean addRemoteLocation(final RemoteLocation remoteLocation) {
        return false;
    }

    @Override
    public boolean removeRemoteLocations(final List<RemoteLocation> remoteLocations) {
        return false;
    }

    @Override
    public void setPythonPaths(final List<SearchPath> pythonPaths) {
        // nothing to do
    }

    @Override
    public List<SearchPath> getPythonPaths() {
        return new ArrayList<>();
    }

    @Override
    public boolean addPythonPath(final SearchPath path) {
        return false;
    }

    @Override
    public boolean removePythonPaths(final List<SearchPath> paths) {
        return false;
    }

    @Override
    public void setClassPaths(final List<SearchPath> classPaths) {
        // nothing to do
    }

    @Override
    public List<SearchPath> getClassPaths() {
        return new ArrayList<>();
    }

    @Override
    public boolean addClassPath(final SearchPath path) {
        return false;
    }

    @Override
    public boolean removeClassPaths(final List<SearchPath> paths) {
        return false;
    }

    @Override
    public void setReferencedVariableFiles(final List<ReferencedVariableFile> referencedVariableFiles) {
        // nothing to do
    }

    @Override
    public List<ReferencedVariableFile> getReferencedVariableFiles() {
        return new ArrayList<>();
    }

    @Override
    public boolean addReferencedVariableFile(final ReferencedVariableFile variableFile) {
        return false;
    }

    @Override
    public boolean removeReferencedVariableFiles(final List<ReferencedVariableFile> referencedVariableFiles) {
        return false;
    }

    @Override
    public void setVariableMappings(final List<VariableMapping> variableMappings) {
        // nothing to do
    }

    @Override
    public List<VariableMapping> getVariableMappings() {
        return new ArrayList<>();
    }

    @Override
    public boolean addVariableMapping(final VariableMapping mapping) {
        return false;
    }

    @Override
    public boolean removeVariableMappings(final List<VariableMapping> variableMappings) {
        return false;
    }

    @Override
    public void setExcludedPaths(final List<ExcludedPath> excludedPaths) {
        // nothing to do
    }

    @Override
    public List<ExcludedPath> getExcludedPaths() {
        return new ArrayList<>();
    }

    @Override
    public boolean addExcludedPath(final String path) {
        return false;
    }

    @Override
    public boolean removeExcludedPath(final String path) {
        return false;
    }

    @Override
    public boolean isExcludedPath(final String path) {
        return false;
    }

    @Override
    public ExcludedPath getExcludedPath(final String path) {
        return null;
    }

    @Override
    public void setIsValidatedFileSizeCheckingEnabled(final boolean isFileSizeCheckingEnabled) {
        // nothing to do
    }

    @Override
    public boolean isValidatedFileSizeCheckingEnabled() {
        return false;
    }

    @Override
    public void setValidatedFileMaxSize(final String validatedFileMaxSize) {
        // nothing to do
    }

    @Override
    public String getValidatedFileMaxSize() {
        return "-1";
    }

    @Override
    public void setIsReferencedLibrariesAutoReloadEnabled(final boolean isReferencedLibrariesAutoReloadEnabled) {
        // nothing to do
    }

    @Override
    public boolean isReferencedLibrariesAutoReloadEnabled() {
        return false;
    }

    @Override
    public boolean isReferencedLibrariesAutoDiscoveringEnabled() {
        return false;
    }

    @Override
    public void setReferencedLibrariesAutoDiscoveringEnabled(
            final boolean isReferencedLibrariesAutoDiscoveringEnabled) {
        // nothing to do
    }

    @Override
    public boolean isLibrariesAutoDiscoveringSummaryWindowEnabled() {
        return false;
    }

    @Override
    public void setLibrariesAutoDiscoveringSummaryWindowEnabled(
            final boolean isLibrariesAutoDiscoveringSummaryWindowEnabled) {
        // nothing to do
    }

    @Override
    public boolean usesPreferences() {
        return true;
    }

    @Override
    public String providePythonLocation() {
        return null;
    }

    @Override
    public SuiteExecutor providePythonInterpreter() {
        return null;
    }

    @Override
    public void assignPythonLocation(final String path, final SuiteExecutor executor) {
        // nothing to do
    }
}
