/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;

public class RobotProjectConfigWriterTest {

    @Test
    public void javaLibraryIsProperlyWrittenAsFragment() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.JAVA, "java_lib", "path/to/lib");
        
        final String fragment = new RobotProjectConfigWriter().writeFragment(library);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<referencedLibrary")
                .contains("type=\"JAVA\"")
                .contains("name=\"java_lib\"")
                .contains("path=\"path/to/lib\"");
    }

    @Test
    public void virtualLibraryIsProperlyWrittenAsFragment() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.VIRTUAL, "my_lib",
                "path/to/libspec.xml");
        
        final String fragment = new RobotProjectConfigWriter().writeFragment(library);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<referencedLibrary")
                .contains("type=\"VIRTUAL\"")
                .contains("name=\"my_lib\"")
                .contains("path=\"path/to/libspec.xml\"");
    }

    @Test
    public void pythonLibraryIsProperlyWrittenAsFragment() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "module.class", "path/to/lib");
        
        final String fragment = new RobotProjectConfigWriter().writeFragment(library);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<referencedLibrary")
                .contains("type=\"PYTHON\"")
                .contains("name=\"module.class\"")
                .contains("path=\"path/to/lib\"");
    }

    @Test
    public void libraryIsProperlyWrittenAsFragment_withNonAsciiCharacters() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "module.ąęółżźćń", "path/ą/ę");

        final String fragment = new RobotProjectConfigWriter().writeFragment(library);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<referencedLibrary")
                .contains("type=\"PYTHON\"")
                .contains("name=\"module.ąęółżźćń\"")
                .contains("path=\"path/ą/ę\"");
    }

    @Test
    public void excludedPathIsProperlyWrittenAsFragment() {
        final ExcludedFolderPath path = ExcludedFolderPath.create("some/excluded/path");

        final String fragment = new RobotProjectConfigWriter().writeFragment(path);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<excludedPath")
                .contains("path=\"some/excluded/path\"");
    }

    @Test
    public void excludedPathIsProperlyWrittenAsFragment_withNonAsciiCharacters() {
        final ExcludedFolderPath path = ExcludedFolderPath.create("ą/ę");

        final String fragment = new RobotProjectConfigWriter().writeFragment(path);
        assertThat(fragment).doesNotContain("<%xml")
                .contains("<excludedPath")
                .contains("path=\"ą/ę\"");
    }

}
