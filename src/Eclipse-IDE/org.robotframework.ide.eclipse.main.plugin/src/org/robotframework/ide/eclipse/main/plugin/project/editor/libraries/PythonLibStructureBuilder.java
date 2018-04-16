/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

public class PythonLibStructureBuilder implements ILibraryStructureBuilder {

    private final RobotRuntimeEnvironment environment;

    private final EnvironmentSearchPaths additionalSearchPaths;

    public PythonLibStructureBuilder(final RobotRuntimeEnvironment environment, final RobotProjectConfig config,
            final IProject project) {
        this.environment = environment;
        this.additionalSearchPaths = new RedEclipseProjectConfig(config).createEnvironmentSearchPaths(project);
    }

    @Override
    public Collection<ILibraryClass> provideEntriesFromFile(final URI path) throws RobotEnvironmentException {
        return provideEntriesFromFile(path, PythonClass::createWithoutDuplicationOfFileAndClassName);
    }

    public Collection<ILibraryClass> provideAllEntriesFromFile(final URI path) throws RobotEnvironmentException {
        return provideEntriesFromFile(path, PythonClass::new);
    }

    private Collection<ILibraryClass> provideEntriesFromFile(final URI path,
            final Function<String, ILibraryClass> classNameMapper) throws RobotEnvironmentException {
        final List<String> classes = environment.getClassesFromModule(new File(path), additionalSearchPaths);
        return classes.stream().map(classNameMapper).distinct().collect(Collectors.toList());
    }

    public static final class PythonClass implements ILibraryClass {

        private final String qualifiedName;

        @VisibleForTesting
        PythonClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        @VisibleForTesting
        static PythonClass createWithoutDuplicationOfFileAndClassName(final String name) {
            final List<String> splitted = new ArrayList<>(Splitter.on('.').splitToList(name));
            if (splitted.size() > 1) {
                final String last = splitted.get(splitted.size() - 1);
                final String beforeLast = splitted.get(splitted.size() - 2);
                if (last.equals(beforeLast)) {
                    splitted.remove(splitted.size() - 1);
                }
                return new PythonClass(String.join(".", splitted));
            } else {
                return new PythonClass(name);
            }
        }

        @Override
        public String getQualifiedName() {
            return qualifiedName;
        }

        @Override
        public ReferencedLibrary toReferencedLibrary(final String fullLibraryPath) {
            IPath pathWithoutModuleName = new Path(fullLibraryPath).removeLastSegments(1);
            final String[] nameParts = qualifiedName.split("\\.");
            if (fullLibraryPath.endsWith("__init__.py") || nameParts.length > 1) {
                final int segmentsToRemove = countModuleNameSegments(pathWithoutModuleName.segments(), nameParts);
                pathWithoutModuleName = pathWithoutModuleName.removeLastSegments(segmentsToRemove);
            }
            return ReferencedLibrary.create(LibraryType.PYTHON, qualifiedName,
                    RedWorkspace.Paths.toWorkspaceRelativeIfPossible(pathWithoutModuleName).toPortableString());
        }

        private int countModuleNameSegments(final String[] pathSegments, final String[] nameParts) {
            int moduleNameSegments = 0;
            int currentMatch = 1;
            while (currentMatch <= pathSegments.length && currentMatch <= nameParts.length) {
                boolean currentPatsMatch = true;
                for (int i = 0; i < currentMatch; i++) {
                    final String currentNamePart = nameParts[i];
                    final String currentPathSegment = pathSegments[pathSegments.length - currentMatch + i];
                    if (!currentNamePart.equals(currentPathSegment)) {
                        currentPatsMatch = false;
                        break;
                    }
                }
                if (currentPatsMatch) {
                    moduleNameSegments = currentMatch;
                }
                currentMatch++;
            }
            return moduleNameSegments;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj != null && PythonClass.class == obj.getClass()
                    && Objects.equal(this.qualifiedName, ((PythonClass) obj).qualifiedName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(qualifiedName);
        }
    }
}
