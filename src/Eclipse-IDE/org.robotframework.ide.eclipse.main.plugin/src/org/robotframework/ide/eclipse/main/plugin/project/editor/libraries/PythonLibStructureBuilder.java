/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.io.File;
import java.util.Collection;
import java.util.List;

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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

public class PythonLibStructureBuilder {

    private final RobotRuntimeEnvironment environment;

    private final EnvironmentSearchPaths additionalSearchPaths;

    public PythonLibStructureBuilder(final RobotRuntimeEnvironment environment, final RobotProjectConfig config,
            final IProject project) {
        this.environment = environment;
        this.additionalSearchPaths = new RedEclipseProjectConfig(config).createEnvironmentSearchPaths(project);
    }

    public Collection<ILibraryClass> provideEntriesFromFile(final String path, final Optional<String> moduleName,
            final boolean allowDuplicationOfFileAndClassName) throws RobotEnvironmentException {

        final List<String> classes = environment.getClassesDefinedInModule(new File(path), moduleName,
                additionalSearchPaths);
        return newLinkedHashSet(transform(classes, new Function<String, ILibraryClass>() {

            @Override
            public PythonClass apply(final String name) {
                return PythonClass.create(name, allowDuplicationOfFileAndClassName);
            }
        }));
    }

    public static final class PythonClass implements ILibraryClass {

        private final String qualifiedName;

        private PythonClass(final String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }

        static PythonClass create(final String name, final boolean allowDuplicationOfFileAndClassName) {
            final List<String> splitted = newArrayList(Splitter.on('.').splitToList(name));
            if (splitted.size() > 1) {
                final String last = splitted.get(splitted.size() - 1);
                final String beforeLast = splitted.get(splitted.size() - 2);

                // ROBOT requires whole qualified name of class if it is defined with different name
                // than module
                // containing it in module
                // FIXME check the comment above if its still apply
                if (last.equals(beforeLast) && !allowDuplicationOfFileAndClassName) {
                    splitted.remove(splitted.size() - 1);
                }
                return new PythonClass(Joiner.on('.').join(splitted));
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
            final IPath path = new Path(fullLibraryPath);
            final IPath pathWithoutModuleName = fullLibraryPath.endsWith("__init__.py") ? path.removeLastSegments(2)
                    : path.removeLastSegments(1);

            return ReferencedLibrary.create(LibraryType.PYTHON, qualifiedName,
                    RedWorkspace.Paths.toWorkspaceRelativeIfPossible(pathWithoutModuleName).toPortableString());
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
