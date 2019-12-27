/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.dryrun;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Condition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;

public class RobotDryRunLibraryImportTest {

    public static List<Object[]> provideTestData() throws Exception {
        return Arrays.asList(new Object[][] { { "UnknownLibrary", null, DryRunLibraryType.UNKNOWN },
                { "UnsupportedLibrary", new URI("file:///source.txt"), DryRunLibraryType.UNKNOWN },
                { "PythonSourceLibrary", new URI("file:///source.py"), DryRunLibraryType.PYTHON },
                { "JavaSourceLibrary", new URI("file:///source.java"), DryRunLibraryType.JAVA },
                { "JarSourceLibrary", new URI("file:///source.jar"), DryRunLibraryType.JAVA },
                { "JarUpperSourceLibrary", new URI("file:///SOURCE.JAR"), DryRunLibraryType.JAVA },
                { "ClassSourceLibrary", new URI("file:///source.class"), DryRunLibraryType.JAVA },
                { "Remote", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE },
                { "Remote http://9.8.7.6:1234", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE } });
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testCreatingKnown(final String libName, final URI source, final DryRunLibraryType expectedType)
            throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(libName, source);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, source, expectedType,
                new HashSet<>(), DryRunLibraryImportStatus.ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testCreatingUnknown(final String libName, final URI source, final DryRunLibraryType expectedType)
            throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createUnknown(libName);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, null,
                DryRunLibraryType.UNKNOWN, new HashSet<>(), DryRunLibraryImportStatus.NOT_ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void testSettingImportersStatusAndAdditionalInfo(final String libName, final URI source,
            final DryRunLibraryType expectedType) throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(libName, source);
        libImport.setImporters(newHashSet(new URI("file:///suite.robot"), new URI("file:///res.robot")));
        libImport.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);
        libImport.setAdditionalInfo("abc");
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, source, expectedType,
                newHashSet(new URI("file:///suite.robot"), new URI("file:///res.robot")),
                DryRunLibraryImportStatus.ALREADY_EXISTING, "abc");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    private static Condition<? super RobotDryRunLibraryImport> sameFieldsAs(final RobotDryRunLibraryImport library) {
        return new Condition<RobotDryRunLibraryImport>() {

            @Override
            public boolean matches(final RobotDryRunLibraryImport toMatch) {
                return Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getSource(), toMatch.getSource())
                        && Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getImporters(), toMatch.getImporters())
                        && Objects.equals(library.getStatus(), toMatch.getStatus())
                        && Objects.equals(library.getAdditionalInfo(), toMatch.getAdditionalInfo());
            }
        };
    }
}
