/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                    jarClasses.add(JarClass.createFromZipEntry(entry.getName()));
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

    public static class JarClass {
        private final String qualifiedName;

        private JarClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        private static JarClass createFromJavaFile(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".java".length());
            return new JarClass(nameWithoutExtension);
        }

        private static JarClass createFromClassFile(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String nameWithoutNested = nameWithoutExtension.contains("$")
                    ? nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf('$')) : nameWithoutExtension;
            return new JarClass(nameWithoutNested);
        }

        private static JarClass createFromZipEntry(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JarClass(qualifiedName);
        }

        public String getQualifiedName() {
            return qualifiedName;
        }
    }
}
