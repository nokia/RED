/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.HashMap;
import java.util.Map;

import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.collect.ImmutableMap;

public class Libraries {

    public static Map<String, LibrarySpecification> createStdLibs(final String... libNames) {
        final Map<String, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createStdLib(libName));
        }
        return libs;
    }

    public static Map<String, LibrarySpecification> createStdLib(final String libName, final String... kwNames) {
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName(libName);
        for (final String kwName : kwNames) {
            final KeywordSpecification kwSpec = new KeywordSpecification();
            kwSpec.setName(kwName);
            libSpec.getKeywords().add(kwSpec);
        }
        return ImmutableMap.of(libName, libSpec);
    }

    public static Map<String, LibrarySpecification> createRemoteLib(final String path, final String... kwNames) {
        final Map<String, LibrarySpecification> lib = createStdLib("Remote", kwNames);
        final RemoteLocation location = RemoteLocation.create(path);
        final LibrarySpecification spec = lib.get("Remote");
        spec.setRemoteLocation(location);
        return ImmutableMap.of("Remote " + location.getUri(), spec);
    }

    public static Map<ReferencedLibrary, LibrarySpecification> createRefLibs(final String... libNames) {
        final Map<ReferencedLibrary, LibrarySpecification> libs = new HashMap<>();
        for (final String libName : libNames) {
            libs.putAll(createRefLib(libName));
        }
        return libs;
    }

    public static Map<ReferencedLibrary, LibrarySpecification> createRefLib(final String libName,
            final String... kwNames) {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, libName, "");
        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName(libName);
        libSpec.setReferenced(library);
        for (final String kwName : kwNames) {
            final KeywordSpecification kwSpec = new KeywordSpecification();
            kwSpec.setName(kwName);
            libSpec.getKeywords().add(kwSpec);
        }
        return ImmutableMap.of(library, libSpec);
    }

}
