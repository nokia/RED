/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PythonLibStructureBuilder.PythonClass;

import com.google.common.base.Optional;

public class JarStructureBuilder {
    
    private final RobotRuntimeEnvironment environment;

    private final RobotProjectConfig config;

    private final IProject project;
    
    public JarStructureBuilder(final RobotRuntimeEnvironment environment, final RobotProjectConfig config,
            final IProject project) {
        this.environment = environment;
        this.config = config;
        this.project = project;

    }

    public List<JarClass> provideEntriesFromFile(final String path) {
        return provideEntriesFromFile(new File(path));
    }

    public List<JarClass> provideEntriesFromFile(final File file) {
        if (file.getName().endsWith(".jar")) {
            return provideEntriesFromJarFile(file);
        } else {
            return newArrayList();
        }
    }

    private List<JarClass> provideEntriesFromJarFile(final File file) {
        final List<JarClass> jarClasses = newArrayList();
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
    
    private List<JarClass> providePythonEntriesFromJarFile(final File file) {
        final PythonLibStructureBuilder pythonLibStructureBuilder = new PythonLibStructureBuilder(environment, config,
                project);
        final Collection<PythonClass> entriesFromFile = pythonLibStructureBuilder
                .provideEntriesFromFile(file.getPath(), Optional. <String>absent());

        final List<JarClass> jarClasses = newArrayList();
        for (final PythonClass pythonClass : entriesFromFile) {
            jarClasses.add(JarClass.createFromZipPythonEntry(pythonClass.getQualifiedName()));
        }

        return jarClasses;
    }

    private boolean isJavaClass(final String entryName) {
        return entryName.endsWith(".class");
    }

    public static class JarClass {
        private final String qualifiedName;

        private JarClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        private static JarClass createFromZipJavaEntry(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JarClass(qualifiedName);
        }
        
        private static JarClass createFromZipPythonEntry(final String name) {
            return new JarClass(name);
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public ReferencedLibrary toReferencedLibrary(final String fullLibraryPath) {
            final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
            referencedLibrary.setType(LibraryType.JAVA.toString());
            referencedLibrary.setName(qualifiedName);
            referencedLibrary.setPath(
                    PathsConverter.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath)).toPortableString());
            return referencedLibrary;
        }
    }
}
