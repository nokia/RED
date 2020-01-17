/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ImportedFilesTest {

    @Project(dirs = { "dir1", "dir1_1", "dir2" }, files = { "res.robot", "dir1/res1.robot", "dir1_1/lib.py",
            "dir1_1/vars.py", "dir1_1/vars1.yml", "dir1_1/vars2.yaml", "dir2/res2.robot" })
    static IProject project;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "dir2/tests.robot", "*** Test Cases ***");
    }

    @Test
    public void pythonFilesAreProperlyProvided() {
        final List<IFile> pythonFiles = ImportedFiles.getPythonFiles();
        assertThat(pythonFiles).extracting(IFile::getName).containsOnly("lib.py", "vars.py");
    }

    @Test
    public void resourceFilesAreProperlyProvided_1() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(getFile(project, "res.robot"));
        assertThat(resourceFiles).extracting(IFile::getName).containsOnly("res1.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_2() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(getFile(project, "dir1/res1.robot"));
        assertThat(resourceFiles).extracting(IFile::getName).containsOnly("res.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_3() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(getFile(project, "dir2/res2.robot"));
        assertThat(resourceFiles).extracting(IFile::getName).containsOnly("res.robot", "res1.robot");
    }

    @Test
    public void variableFilesAreProperlyProvided() {
        final List<IFile> variablesFiles = ImportedFiles.getVariableFiles();
        assertThat(variablesFiles).extracting(IFile::getName).containsOnly("lib.py", "vars.py", "vars1.yml",
                "vars2.yaml");
    }

    @Test
    public void pathsComparatorGivesPrecedenceForPathsInGivenProjectOverPathsFromDifferentProjects() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(project, "");

        assertThat(compare(comparator, "/ImportedFilesTest/file.txt", "/Other/file.txt")).isNegative();
        assertThat(compare(comparator, "/Other/file.txt", "/ImportedFilesTest/file.txt")).isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecedenceForShorterPathsInSameProject() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(project, "");

        assertThat(compare(comparator, "/ImportedFilesTest/file.txt", "/ImportedFilesTest/dir/file.txt")).isNegative();
        assertThat(compare(comparator, "/ImportedFilesTest/dir/file.txt", "/ImportedFilesTest/file.txt")).isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecedenceForPathWithSecondSegmentStartingFromPrefixInSameProject() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(project, "xyz");

        assertThat(compare(comparator, "ImportedFilesTest/xyz_file.txt", "ImportedFilesTest/file_xyz.txt"))
                .isNegative();
        assertThat(compare(comparator, "ImportedFilesTest/file_xyz.txt", "ImportedFilesTest/xyz_file.txt"))
                .isPositive();
        assertThat(compare(comparator, "ImportedFilesTest/xyz/file.txt", "ImportedFilesTest/dir/file.txt"))
                .isNegative();
        assertThat(compare(comparator, "ImportedFilesTest/dir/file.txt", "ImportedFilesTest/xyz/file.txt"))
                .isPositive();
    }

    @Test
    public void pathsComparatorGivesPrecedenceForPathWhichFirstDifferentSegmentIsLexicographicallySmaller() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(project, "");

        assertThat(compare(comparator, "/ImportedFilesTest/bc/d.txt", "/ImportedFilesTest/bd/d.txt")).isNegative();
        assertThat(compare(comparator, "/ImportedFilesTest/bd/d.txt", "/ImportedFilesTest/bc/d.txt")).isPositive();
    }

    @Test
    public void pathsComparatorReturnZeroForSamePaths() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator(project, "");

        assertThat(compare(comparator, "/ImportedFilesTest", "/ImportedFilesTest")).isZero();
        assertThat(compare(comparator, "/ImportedFilesTest/bc", "/ImportedFilesTest/bc")).isZero();
        assertThat(compare(comparator, "/ImportedFilesTest/bc/d.txt", "/ImportedFilesTest/bc/d.txt")).isZero();
    }

    private static int compare(final Comparator<IPath> comparator, final String p1, final String p2) {
        return comparator.compare(new Path(p1), new Path(p2));
    }
}
