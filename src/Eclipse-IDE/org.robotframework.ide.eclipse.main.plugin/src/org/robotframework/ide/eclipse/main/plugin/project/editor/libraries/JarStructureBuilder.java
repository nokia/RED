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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;

public class JarStructureBuilder {

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
                } else if(isPythonClass(entry.getName())) {
                    jarClasses.add(JarClass.createFromZipPythonEntry(entry.getName()));
                }
                entry = zipStream.getNextEntry();
            }
            return jarClasses;
        } catch (final IOException e) {
            return jarClasses;
        }
    }

    private boolean isJavaClass(final String entryName) {
        return entryName.endsWith(".class");
    }
    
    private boolean isPythonClass(final String entryName) {
        return entryName.endsWith(".py");
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
            final String nameWithoutExtension = name.substring(0, name.length() - ".py".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JarClass(qualifiedName);
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        ReferencedLibrary toReferencedLibrary(final String fullLibraryPath) {
            final ReferencedLibrary referencedLibrary = new ReferencedLibrary();
            referencedLibrary.setType(LibraryType.JAVA.toString());
            referencedLibrary.setName(qualifiedName);
            referencedLibrary.setPath(
                    PathsConverter.toWorkspaceRelativeIfPossible(new Path(fullLibraryPath)).toPortableString());
            return referencedLibrary;
        }
    }
}
