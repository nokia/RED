/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;

@RunWith(Parameterized.class)
public class RobotDryRunLibraryImportTest {

    private final String libName;

    private final URI originalPath;

    private final DryRunLibraryType expectedType;

    private final URI expectedSourcePath;

    @Parameters(name = "${0}")
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] { { "UnknownLibrary", null, DryRunLibraryType.UNKNOWN, null },
                { "PythonSourceLibrary", new URI("file:///source.py"), DryRunLibraryType.PYTHON,
                        new URI("file:///source.py") },
                { "PythonCompiledSourceLibrary", new URI("file:///source.pyc"), DryRunLibraryType.PYTHON,
                        new URI("file:///source.py") },
                { "JythonCompiledSourceLibrary", new URI("file:///source$py.class"), DryRunLibraryType.PYTHON,
                        new URI("file:///source.py") },
                { "JavaSourceLibrary", new URI("file:///source.java"), DryRunLibraryType.JAVA,
                        new URI("file:///source.java") },
                { "JarSourceLibrary", new URI("file:///source.jar"), DryRunLibraryType.JAVA,
                        new URI("file:///source.jar") },
                { "ClassSourceLibrary", new URI("file:///source.class"), DryRunLibraryType.JAVA,
                        new URI("file:///source.class") },
                { "PythonInJarSourceLibrary", new URI("file:///source.jar/module.py"), DryRunLibraryType.JAVA,
                        new URI("file:///source.jar") },
                { "Remote", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE, new URI("http://9.8.7.6:1234") },
                { "Remote http://9.8.7.6:1234", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE,
                        new URI("http://9.8.7.6:1234") } });
    }

    public RobotDryRunLibraryImportTest(final String libName, final URI originalPath,
            final DryRunLibraryType expectedType, final URI expectedSourcePath) {
        this.libName = libName;
        this.originalPath = originalPath;
        this.expectedType = expectedType;
        this.expectedSourcePath = expectedSourcePath;
    }

    @Test
    public void testCreatingKnown() throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(libName, originalPath);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, expectedSourcePath,
                expectedType, new HashSet<>(), new ArrayList<>(), DryRunLibraryImportStatus.ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    @Test
    public void testCreatingUnknown() throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createUnknown(libName);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, null,
                DryRunLibraryType.UNKNOWN, new HashSet<>(), new ArrayList<>(), DryRunLibraryImportStatus.NOT_ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    private static Condition<? super RobotDryRunLibraryImport> sameFieldsAs(final RobotDryRunLibraryImport library) {
        return new Condition<RobotDryRunLibraryImport>() {

            @Override
            public boolean matches(final RobotDryRunLibraryImport toMatch) {
                return Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getSourcePath(), toMatch.getSourcePath())
                        && Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getImportersPaths(), toMatch.getImportersPaths())
                        && Objects.equals(library.getArgs(), toMatch.getArgs())
                        && Objects.equals(library.getStatus(), toMatch.getStatus())
                        && Objects.equals(library.getAdditionalInfo(), toMatch.getAdditionalInfo());
            }
        };
    }
}
