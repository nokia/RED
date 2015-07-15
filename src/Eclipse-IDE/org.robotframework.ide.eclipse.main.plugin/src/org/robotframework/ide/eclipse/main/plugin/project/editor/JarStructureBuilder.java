package org.robotframework.ide.eclipse.main.plugin.project.editor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class JarStructureBuilder {

    List<JarClass> provideEntriesFromJarFile(final String path) {
        final List<JarClass> jarClasses = newArrayList();
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry entry = zipStream.getNextEntry();
            while (entry != null) {
                if (isJavaClass(entry.getName())) {
                    jarClasses.add(JarClass.create(entry.getName()));
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

    static class JarClass {
        private final String qualifiedName;

        private JarClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        private static JarClass create(final String name) {
            final String nameWithoutExtension = name.substring(0, name.length() - ".class".length());
            final String qualifiedName = nameWithoutExtension.replaceAll("/", ".");
            return new JarClass(qualifiedName);
        }

        String getQualifiedName() {
            return qualifiedName;
        }
    }
}
