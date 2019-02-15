/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;

public class RobotProjectConfigTest {

    @Test
    public void referenceLibraryIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedLibrary> libs = newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
        config.setReferencedLibraries(libs);

        final boolean result = config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "Lib3", "ghi"));

        assertThat(result).isTrue();
        assertThat(config.getReferencedLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib3", "ghi"));
    }

    @Test
    public void referenceLibraryIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedLibrary> libs = newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
        config.setReferencedLibraries(libs);

        final boolean result = config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));

        assertThat(result).isFalse();
        assertThat(config.getReferencedLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
    }

    @Test
    public void referenceLibraryIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedLibrary> libs = newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
        config.setReferencedLibraries(libs);

        final boolean result = config
                .removeReferencedLibraries(newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def")));

        assertThat(result).isTrue();
        assertThat(config.getReferencedLibraries())
                .containsExactly(ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"));
    }

    @Test
    public void referenceLibraryIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedLibrary> libs = newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
        config.setReferencedLibraries(libs);

        final boolean result = config
                .removeReferencedLibraries(newArrayList(ReferencedLibrary.create(LibraryType.PYTHON, "Lib3", "ghi")));

        assertThat(result).isFalse();
        assertThat(config.getReferencedLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib1", "abc"),
                ReferencedLibrary.create(LibraryType.PYTHON, "Lib2", "def"));
    }

    @Test
    public void remoteLocationIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<RemoteLocation> locations = newArrayList(RemoteLocation.create("abc"), RemoteLocation.create("def"));
        config.setRemoteLocations(locations);

        final boolean result = config.addRemoteLocation(RemoteLocation.create("ghi"));

        assertThat(result).isTrue();
        assertThat(config.getRemoteLocations()).containsExactly(RemoteLocation.create("abc"),
                RemoteLocation.create("def"), RemoteLocation.create("ghi"));
    }

    @Test
    public void remoteLocationIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<RemoteLocation> locations = newArrayList(RemoteLocation.create("abc"), RemoteLocation.create("def"));
        config.setRemoteLocations(locations);

        final boolean result = config.addRemoteLocation(RemoteLocation.create("def"));

        assertThat(result).isFalse();
        assertThat(config.getRemoteLocations()).containsExactly(RemoteLocation.create("abc"),
                RemoteLocation.create("def"));
    }

    @Test
    public void remoteLocationIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<RemoteLocation> locations = newArrayList(RemoteLocation.create("abc"), RemoteLocation.create("def"));
        config.setRemoteLocations(locations);

        final boolean result = config.removeRemoteLocations(newArrayList(RemoteLocation.create("def")));

        assertThat(result).isTrue();
        assertThat(config.getRemoteLocations()).containsExactly(RemoteLocation.create("abc"));
    }

    @Test
    public void remoteLocationIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<RemoteLocation> locations = newArrayList(RemoteLocation.create("abc"), RemoteLocation.create("def"));
        config.setRemoteLocations(locations);

        final boolean result = config.removeRemoteLocations(newArrayList(RemoteLocation.create("ghi")));

        assertThat(result).isFalse();
        assertThat(config.getRemoteLocations()).containsExactly(RemoteLocation.create("abc"),
                RemoteLocation.create("def"));
    }

    @Test
    public void pythonPathIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setPythonPaths(paths);

        final boolean result = config.addPythonPath(SearchPath.create("ghi"));

        assertThat(result).isTrue();
        assertThat(config.getPythonPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"),
                SearchPath.create("ghi"));
    }

    @Test
    public void pythonPathIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setPythonPaths(paths);

        final boolean result = config.addPythonPath(SearchPath.create("def"));

        assertThat(result).isFalse();
        assertThat(config.getPythonPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"));
    }

    @Test
    public void pythonPathIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setPythonPaths(paths);

        final boolean result = config.removePythonPaths(newArrayList(SearchPath.create("def")));

        assertThat(result).isTrue();
        assertThat(config.getPythonPaths()).containsExactly(SearchPath.create("abc"));
    }

    @Test
    public void pythonPathIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setPythonPaths(paths);

        final boolean result = config.removePythonPaths(newArrayList(SearchPath.create("ghi")));

        assertThat(result).isFalse();
        assertThat(config.getPythonPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"));
    }

    @Test
    public void classPathIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setClassPaths(paths);

        final boolean result = config.addClassPath(SearchPath.create("ghi"));

        assertThat(result).isTrue();
        assertThat(config.getClassPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"),
                SearchPath.create("ghi"));
    }

    @Test
    public void classPathIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setClassPaths(paths);

        final boolean result = config.addClassPath(SearchPath.create("def"));

        assertThat(result).isFalse();
        assertThat(config.getClassPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"));
    }

    @Test
    public void classPathIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setClassPaths(paths);

        final boolean result = config.removeClassPaths(newArrayList(SearchPath.create("def")));

        assertThat(result).isTrue();
        assertThat(config.getClassPaths()).containsExactly(SearchPath.create("abc"));
    }

    @Test
    public void classPathIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<SearchPath> paths = newArrayList(SearchPath.create("abc"), SearchPath.create("def"));
        config.setClassPaths(paths);

        final boolean result = config.removeClassPaths(newArrayList(SearchPath.create("ghi")));

        assertThat(result).isFalse();
        assertThat(config.getClassPaths()).containsExactly(SearchPath.create("abc"), SearchPath.create("def"));
    }

    @Test
    public void variableFileIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedVariableFile> files = newArrayList(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
        config.setReferencedVariableFiles(files);

        final boolean result = config.addReferencedVariableFile(ReferencedVariableFile.create("ghi"));

        assertThat(result).isTrue();
        assertThat(config.getReferencedVariableFiles()).containsExactly(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"), ReferencedVariableFile.create("ghi"));
    }

    @Test
    public void variableFileIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedVariableFile> files = newArrayList(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
        config.setReferencedVariableFiles(files);

        final boolean result = config.addReferencedVariableFile(ReferencedVariableFile.create("def"));

        assertThat(result).isFalse();
        assertThat(config.getReferencedVariableFiles()).containsExactly(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
    }

    @Test
    public void variableFileIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedVariableFile> files = newArrayList(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
        config.setReferencedVariableFiles(files);

        final boolean result = config.removeReferencedVariableFiles(newArrayList(ReferencedVariableFile.create("def")));

        assertThat(result).isTrue();
        assertThat(config.getReferencedVariableFiles()).containsExactly(ReferencedVariableFile.create("abc"));
    }

    @Test
    public void variableFileIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ReferencedVariableFile> files = newArrayList(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
        config.setReferencedVariableFiles(files);

        final boolean result = config.removeReferencedVariableFiles(newArrayList(ReferencedVariableFile.create("ghi")));

        assertThat(result).isFalse();
        assertThat(config.getReferencedVariableFiles()).containsExactly(ReferencedVariableFile.create("abc"),
                ReferencedVariableFile.create("def"));
    }

    @Test
    public void variableMappingIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<VariableMapping> mappings = newArrayList(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
        config.setVariableMappings(mappings);

        final boolean result = config.addVariableMapping(VariableMapping.create("${ghi}", "3"));

        assertThat(result).isTrue();
        assertThat(config.getVariableMappings()).containsExactly(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"), VariableMapping.create("${ghi}", "3"));
    }

    @Test
    public void variableMappingIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<VariableMapping> mappings = newArrayList(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
        config.setVariableMappings(mappings);

        final boolean result = config.addVariableMapping(VariableMapping.create("${def}", "2"));

        assertThat(result).isFalse();
        assertThat(config.getVariableMappings()).containsExactly(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
    }

    @Test
    public void variableMappingIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<VariableMapping> mappings = newArrayList(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
        config.setVariableMappings(mappings);

        final boolean result = config.removeVariableMappings(newArrayList(VariableMapping.create("${def}", "2")));

        assertThat(result).isTrue();
        assertThat(config.getVariableMappings()).containsExactly(VariableMapping.create("${abc}", "1"));
    }

    @Test
    public void variableMappingIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<VariableMapping> mappings = newArrayList(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
        config.setVariableMappings(mappings);

        final boolean result = config.removeVariableMappings(newArrayList(VariableMapping.create("${ghi}", "3")));

        assertThat(result).isFalse();
        assertThat(config.getVariableMappings()).containsExactly(VariableMapping.create("${abc}", "1"),
                VariableMapping.create("${def}", "2"));
    }

    @Test
    public void excludedPathIsAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ExcludedPath> paths = newArrayList(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
        config.setExcludedPaths(paths);

        final boolean result = config.addExcludedPath("ghi");

        assertThat(result).isTrue();
        assertThat(config.getExcludedPaths()).containsExactly(ExcludedPath.create("abc"),
                ExcludedPath.create("def"), ExcludedPath.create("ghi"));
    }

    @Test
    public void excludedPathIsNotAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ExcludedPath> paths = newArrayList(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
        config.setExcludedPaths(paths);

        final boolean result = config.addExcludedPath("def");

        assertThat(result).isFalse();
        assertThat(config.getExcludedPaths()).containsExactly(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
    }

    @Test
    public void excludedPathIsRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ExcludedPath> paths = newArrayList(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
        config.setExcludedPaths(paths);

        final boolean result = config.removeExcludedPath("def");

        assertThat(result).isTrue();
        assertThat(config.getExcludedPaths()).containsExactly(ExcludedPath.create("abc"));
    }

    @Test
    public void excludedPathIsNotRemoved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ExcludedPath> paths = newArrayList(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
        config.setExcludedPaths(paths);

        final boolean result = config.removeExcludedPath("ghi");

        assertThat(result).isFalse();
        assertThat(config.getExcludedPaths()).containsExactly(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
    }

    @Test
    public void excludedPathIsChecked() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final List<ExcludedPath> paths = newArrayList(ExcludedPath.create("abc"),
                ExcludedPath.create("def"));
        config.setExcludedPaths(paths);

        assertThat(config.isExcludedPath("abc")).isTrue();
        assertThat(config.isExcludedPath("ghi")).isFalse();
    }

}
