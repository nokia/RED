/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.HashMap;
import java.util.Map;

import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.collect.ImmutableMap;

public class Libraries {

    public static Map<LibraryDescriptor, LibrarySpecification> createStdLibs(final String... libNames) {
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createStdLib(libName));
        }
        return libs;
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createStdLib(final String libName,
            final String... kwNames) {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary(libName);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setFormat("ROBOT");
        libSpec.setDescriptor(descriptor);
        libSpec.setName(libName);
        for (final String kwName : kwNames) {
            final KeywordSpecification kwSpec = new KeywordSpecification();
            kwSpec.setName(kwName);
            libSpec.getKeywords().add(kwSpec);
        }
        return new HashMap<>(ImmutableMap.of(descriptor, libSpec));
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRemoteLib(final String path,
            final String... kwNames) {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create(path));
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setFormat("ROBOT");
        libSpec.setDescriptor(descriptor);
        libSpec.setName("Remote");
        for (final String kwName : kwNames) {
            final KeywordSpecification kwSpec = new KeywordSpecification();
            kwSpec.setName(kwName);
            libSpec.getKeywords().add(kwSpec);
        }
        return ImmutableMap.of(descriptor, libSpec);
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLibs(final String... libNames) {
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createRefLib(libName));
        }
        return libs;
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final String libName,
            final String... kwNames) {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, libName, "");
        return createRefLib(library, kwNames);
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final ReferencedLibrary library,
            final String... kwNames) {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(library);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setVersion("1.0");
        libSpec.setScope("global");
        libSpec.setFormat("ROBOT");
        libSpec.setName(library.getName());
        libSpec.setDescriptor(descriptor);
        libSpec.setDocumentation("library documentation");
        for (final String kwName : kwNames) {
            final KeywordSpecification kwSpec = new KeywordSpecification();
            kwSpec.setName(kwName);
            libSpec.getKeywords().add(kwSpec);
        }
        return ImmutableMap.of(descriptor, libSpec);
    }

}
