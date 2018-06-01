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
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

public class JarStructureBuilder implements ILibraryStructureBuilder {

    private final RobotRuntimeEnvironment environment;

    private final RobotProjectConfig config;

    private final IProject project;

    public JarStructureBuilder(final RobotRuntimeEnvironment environment, final RobotProjectConfig config,
            final IProject project) {
        this.environment = environment;
        this.config = config;
        this.project = project;

    }

    @Override
    public Collection<ILibraryClass> provideEntriesFromFile(final URI uri) throws RobotEnvironmentException {
        return provideEntriesFromFile(new File(uri));
    }

    public Collection<ILibraryClass> provideEntriesFromFile(final File file) throws RobotEnvironmentException {
        if (file.getName().endsWith(".jar")) {
            return provideEntriesFromJarFile(file);
        } else {
            return new ArrayList<>();
        }
    }

    private Collection<ILibraryClass> provideEntriesFromJarFile(final File file) throws RobotEnvironmentException {
        final List<ILibraryClass> jarClasses = new ArrayList<>();
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                if (isJavaClass(entry.getName())) {
                    jarClasses.add(JarClass.createFromZipJavaEntry(entry.getName()));
                }
                entry = zipStream.getNextEntry();
            }
        } catch (final IOException e) {
            // nothing to do
        }

        jarClasses.addAll(providePythonEntriesFromJarFile(file));

        return jarClasses;
    }

    private Collection<JarClass> providePythonEntriesFromJarFile(final File file) throws RobotEnvironmentException {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(environment, config,
                project);
        final Collection<ILibraryClass> pythonClasses = pythonLibStructureBuilder.provideEntriesFromFile(file.toURI());

        final List<JarClass> jarClasses = new ArrayList<>();
        for (final ILibraryClass pythonClass : pythonClasses) {
            jarClasses.add(new JarClass(pythonClass.getQualifiedName()));
        }

        return jarClasses;
    }

    private boolean isJavaClass(final String entryName) {
        return entryName.endsWith(".class");
    }

    public static final class JarClass implements ILibraryClass {

        private final String qualifiedName;

        @VisibleForTesting
        JarClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        @VisibleForTesting
        static JarClass createFromZipJavaEntry(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JarClass(qualifiedName);
        }

        @Override
        public String getQualifiedName() {
            return qualifiedName;
        }

        @Override
        public ReferencedLibrary toReferencedLibrary(final String fullLibraryPath) {
            return ReferencedLibrary.create(LibraryType.JAVA, qualifiedName,
                    RedWorkspace.Paths.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath)).toPortableString());
        }

        @Override
        public boolean equals(final Object obj) {
            return obj != null && JarClass.class == obj.getClass()
                    && Objects.equal(this.qualifiedName, ((JarClass) obj).qualifiedName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(qualifiedName);
        }
    }
}
