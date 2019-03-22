/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public class ArchiveStructureBuilder implements ILibraryStructureBuilder {

    private final IRuntimeEnvironment environment;

    private final RobotProjectConfig config;

    private final IProject project;

    public ArchiveStructureBuilder(final IRuntimeEnvironment environment, final RobotProjectConfig config,
            final IProject project) {
        this.environment = environment;
        this.config = config;
        this.project = project;

    }

    @Override
    public Collection<ILibraryClass> provideEntriesFromFile(final URI uri) {
        if (uri.getPath().toLowerCase().endsWith(".jar") || uri.getPath().toLowerCase().endsWith(".zip")) {
            return provideEntriesFromArchiveFile(uri);
        } else {
            return new ArrayList<>();
        }
    }

    private Collection<ILibraryClass> provideEntriesFromArchiveFile(final URI uri) {
        final List<ILibraryClass> classes = new ArrayList<>();
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(new File(uri)))) {
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                if (isJavaClass(entry.getName())) {
                    classes.add(JavaClass.createFromZipJavaEntry(entry.getName()));
                }
                entry = zipStream.getNextEntry();
            }
        } catch (final IOException e) {
            // nothing to do
        }

        classes.addAll(providePythonEntriesFromArchiveFile(uri));

        return classes;
    }

    private Collection<ILibraryClass> providePythonEntriesFromArchiveFile(final URI uri) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(environment, config,
                project);

        return pythonLibStructureBuilder.provideEntriesFromFile(uri);
    }

    private boolean isJavaClass(final String entryName) {
        return entryName.endsWith(".class");
    }

    public static final class JavaClass implements ILibraryClass {

        private final String qualifiedName;

        @VisibleForTesting
        JavaClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        @VisibleForTesting
        static JavaClass createFromZipJavaEntry(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JavaClass(qualifiedName);
        }

        @Override
        public String getQualifiedName() {
            return qualifiedName;
        }

        @Override
        public LibraryType getType() {
            return LibraryType.JAVA;
        }

        @Override
        public ReferencedLibrary toReferencedLibrary(final String fullLibraryPath) {
            return ReferencedLibrary.create(getType(), qualifiedName,
                    RedWorkspace.Paths.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath)).toPortableString());
        }

        @Override
        public boolean equals(final Object obj) {
            return obj != null && JavaClass.class == obj.getClass()
                    && Objects.equal(this.qualifiedName, ((JavaClass) obj).qualifiedName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(qualifiedName);
        }
    }
}
