/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.collect.ImmutableMap;

public class Libraries {

    public static Map<LibraryDescriptor, LibrarySpecification> createStdLibs(final String... libNames) {
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createStdLib(libName, new String[0]));
        }
        return libs;
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createStdLib(final String libName,
            final String... kwNames) {
        return createStdLib(libName,
                Stream.of(kwNames).map(KeywordSpecification::create).toArray(KeywordSpecification[]::new));
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createStdLib(final String libName,
            final KeywordSpecification... kwSpecs) {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardLibrary(libName);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setFormat("ROBOT");
        libSpec.setDescriptor(descriptor);
        libSpec.setName(libName);
        libSpec.setKeywords(newArrayList(kwSpecs));
        return new HashMap<>(ImmutableMap.of(descriptor, libSpec));
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRemoteLib(final String path,
            final String... kwNames) {
        final LibraryDescriptor descriptor = LibraryDescriptor.ofStandardRemoteLibrary(RemoteLocation.create(path));
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setFormat("ROBOT");
        libSpec.setDescriptor(descriptor);
        libSpec.setName("Remote");
        libSpec.setConstructor(LibraryConstructor.create("", newArrayList("uri=default", "timeout=30")));
        libSpec.setKeywords(Stream.of(kwNames).map(KeywordSpecification::create).collect(toList()));
        return ImmutableMap.of(descriptor, libSpec);
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLibs(final String... libNames) {
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createRefLib(libName, new String[0]));
        }
        return libs;
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final String libName,
            final String... kwNames) {
        return createRefLib(libName,
                Stream.of(kwNames).map(KeywordSpecification::create).toArray(KeywordSpecification[]::new));
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final String libName,
            final KeywordSpecification... kwSpecs) {
        return createRefLib(ReferencedLibrary.create(LibraryType.PYTHON, libName, libName + ".py"), kwSpecs);
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final ReferencedLibrary library,
            final String... kwNames) {
        return createRefLib(library,
                Stream.of(kwNames).map(KeywordSpecification::create).toArray(KeywordSpecification[]::new));
    }

    public static Map<LibraryDescriptor, LibrarySpecification> createRefLib(final ReferencedLibrary library,
            final KeywordSpecification... kwSpecs) {
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();
        final LibraryDescriptor descriptor = LibraryDescriptor.ofReferencedLibrary(library, variant);
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setVersion("1.0");
        libSpec.setScope("global");
        libSpec.setFormat("ROBOT");
        libSpec.setName(library.getName());
        libSpec.setDescriptor(descriptor);
        libSpec.setDocumentation("library documentation");
        libSpec.setKeywords(newArrayList(kwSpecs));
        return ImmutableMap.of(descriptor, libSpec);
    }

}
