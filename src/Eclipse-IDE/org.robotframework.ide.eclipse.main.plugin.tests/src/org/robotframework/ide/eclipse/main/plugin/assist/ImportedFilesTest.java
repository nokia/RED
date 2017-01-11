/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Function;

public class ImportedFilesTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportedFilesTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1_1");
        projectProvider.createDir("dir2");

        projectProvider.createFile("res.robot");
        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1_1/lib.py");
        projectProvider.createFile("dir1_1/vars.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");
    }

    @Test
    public void pythonFilesAreProperlyProvided() {
        final List<IFile> pythonFiles = ImportedFiles.getPythonFiles();
        assertThat(transform(pythonFiles, toNames())).containsOnly("lib.py", "vars.py");
    }

    @Test
    public void resourceFilesAreProperlyProvided_1() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("res.robot"));
        assertThat(transform(resourceFiles, toNames())).containsOnly("res1.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_2() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("dir1/res1.robot"));
        assertThat(transform(resourceFiles, toNames())).containsOnly("res.robot", "res2.robot");
    }

    @Test
    public void resourceFilesAreProperlyProvided_3() {
        final List<IFile> resourceFiles = ImportedFiles.getResourceFiles(projectProvider.getFile("dir2/res2.robot"));
        assertThat(transform(resourceFiles, toNames())).containsOnly("res.robot", "res1.robot");
    }

    @Test
    public void pathsComparatorGivesPrecendenceForPathsInGivenProjectOverPathsFromDifferentProjects() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator("project");

        assertThat(compare(comparator, "/project/file.txt", "/other_project/file.txt")).isEqualTo(-1);
        assertThat(compare(comparator, "/other_project/file.txt", "/project/file.txt")).isEqualTo(1);
    }

    @Test
    public void pathsComparatorGivesPrecendenceForShorterPathsInSameProject() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator("project");

        assertThat(compare(comparator, "/project/file.txt", "/project/dir/file.txt")).isEqualTo(-1);
        assertThat(compare(comparator, "/project/dir/file.txt", "/project/file.txt")).isEqualTo(1);
    }

    @Test
    public void pathsComparatorGivesPrecendenceForPathWhichFirstDifferentSegmentIsLexicographicallySmaller() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator("a");

        assertThat(compare(comparator, "/a/bc/d.txt", "/a/bd/d.txt")).isEqualTo(-1);
        assertThat(compare(comparator, "/a/bd/d.txt", "/a/bc/d.txt")).isEqualTo(1);
    }

    @Test
    public void pathsComparatorReturnZeroForSamePaths() {
        final Comparator<IPath> comparator = ImportedFiles.createPathsComparator("a");

        assertThat(compare(comparator, "/a", "/a")).isEqualTo(0);
        assertThat(compare(comparator, "/a/bc", "/a/bc")).isEqualTo(0);
        assertThat(compare(comparator, "/a/bc/d.txt", "/a/bc/d.txt")).isEqualTo(0);
    }

    private static int compare(final Comparator<IPath> comparator, final String p1, final String p2) {
        return comparator.compare(new Path(p1), new Path(p2));
    }

    private static Function<IFile, String> toNames() {
        return new Function<IFile, String>() {

            @Override
            public String apply(final IFile file) {
                return file.getName();
            }
        };
    }
}
