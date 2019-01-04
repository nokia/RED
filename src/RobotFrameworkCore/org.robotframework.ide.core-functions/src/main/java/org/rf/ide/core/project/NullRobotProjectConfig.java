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
    public void setLibraries(final List<ReferencedLibrary> libraries) {
        // nothing to do
    }

    @Override
    public List<ReferencedLibrary> getLibraries() {
        return new ArrayList<>();
    }

    @Override
    public void setPythonPath(final List<SearchPath> pythonPaths) {
        // nothing to do
    }

    @Override
    public List<SearchPath> getPythonPath() {
        return new ArrayList<>();
    }

    @Override
    public boolean addPythonPath(final SearchPath searchPath) {
        return false;
    }

    @Override
    public void removePythonPath(final List<SearchPath> paths) {
        // nothing to do
    }

    @Override
    public void setClassPath(final List<SearchPath> classPaths) {
        // nothing to do
    }

    @Override
    public boolean addClassPath(final SearchPath searchPath) {
        return false;
    }

    @Override
    public void removeClassPath(final List<SearchPath> paths) {
        // nothing to do
    }

    @Override
    public List<SearchPath> getClassPath() {
        return new ArrayList<>();
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
    public void setVariableMappings(final List<VariableMapping> variableMappings) {
        // nothing to do
    }

    @Override
    public List<VariableMapping> getVariableMappings() {
        return new ArrayList<>();
    }

    @Override
    public void setExcludedPath(final List<ExcludedFolderPath> excludedPaths) {
        // nothing to do
    }

    @Override
    public List<ExcludedFolderPath> getExcludedPath() {
        return new ArrayList<>();
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
    public void addExcludedPath(final String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeExcludedPath(final String path) {
        // nothing to do
    }

    @Override
    public boolean isExcludedFromValidation(final String path) {
        return false;
    }

    @Override
    public ExcludedFolderPath getExcludedPath(final String path) {
        return null;
    }

    @Override
    public boolean addReferencedLibrary(final ReferencedLibrary referencedLibrary) {
        return false;
    }

    @Override
    public void removeLibraries(final List<ReferencedLibrary> selectedLibs) {
        // nothing to do
    }

    @Override
    public boolean addRemoteLocation(final RemoteLocation remoteLocation) {
        return false;
    }

    @Override
    public void removeRemoteLocations(final List<RemoteLocation> locations) {
        // nothing to do
    }

    @Override
    public void addVariableMapping(final VariableMapping mapping) {
        // nothing to do
    }

    @Override
    public void removeVariableMappings(final List<VariableMapping> mappings) {
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

    @Override
    public boolean hasReferencedLibraries() {
        return false;
    }

    @Override
    public boolean hasRemoteLibraries() {
        return false;
    }

    @Override
    public List<ReferencedVariableFile> getReferencedVariableFiles() {
        return new ArrayList<>();
    }

    @Override
    public void addReferencedVariableFile(final ReferencedVariableFile variableFile) {
        // nothing to do
    }

    @Override
    public void removeReferencedVariableFiles(final List<ReferencedVariableFile> selectedFiles) {
        // nothing to do
    }
}
