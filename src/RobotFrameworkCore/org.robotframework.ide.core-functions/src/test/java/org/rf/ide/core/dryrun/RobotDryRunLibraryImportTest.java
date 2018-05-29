/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Sets.newHashSet;
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

    private final URI source;

    private final DryRunLibraryType expectedType;

    @Parameters(name = "${0}")
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] { { "UnknownLibrary", null, DryRunLibraryType.UNKNOWN },
                { "UnsupportedLibrary", new URI("file:///source.txt"), DryRunLibraryType.UNKNOWN },
                { "PythonSourceLibrary", new URI("file:///source.py"), DryRunLibraryType.PYTHON },
                { "JavaSourceLibrary", new URI("file:///source.java"), DryRunLibraryType.JAVA },
                { "JarSourceLibrary", new URI("file:///source.jar"), DryRunLibraryType.JAVA },
                { "ClassSourceLibrary", new URI("file:///source.class"), DryRunLibraryType.JAVA },
                { "Remote", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE },
                { "Remote http://9.8.7.6:1234", new URI("http://9.8.7.6:1234"), DryRunLibraryType.REMOTE } });
    }

    public RobotDryRunLibraryImportTest(final String libName, final URI source, final DryRunLibraryType expectedType) {
        this.libName = libName;
        this.source = source;
        this.expectedType = expectedType;
    }

    @Test
    public void testCreatingKnown() throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(libName, source);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, source, expectedType,
                new HashSet<>(), new ArrayList<>(), DryRunLibraryImportStatus.ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    @Test
    public void testCreatingUnknown() throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createUnknown(libName);
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, null,
                DryRunLibraryType.UNKNOWN, new HashSet<>(), new ArrayList<>(), DryRunLibraryImportStatus.NOT_ADDED, "");
        assertThat(libImport).has(sameFieldsAs(expectedLibImport));
    }

    @Test
    public void testSettingImportersStatusAndAdditionalInfo() throws Exception {
        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown(libName, source);
        libImport.setImporters(newHashSet(new URI("file:///suite.robot"), new URI("file:///res.robot")));
        libImport.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);
        libImport.setAdditionalInfo("abc");
        final RobotDryRunLibraryImport expectedLibImport = new RobotDryRunLibraryImport(libName, source, expectedType,
                newHashSet(new URI("file:///suite.robot"), new URI("file:///res.robot")), new ArrayList<>(),
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
                        && Objects.equals(library.getArgs(), toMatch.getArgs())
                        && Objects.equals(library.getStatus(), toMatch.getStatus())
                        && Objects.equals(library.getAdditionalInfo(), toMatch.getAdditionalInfo());
            }
        };
    }
}
