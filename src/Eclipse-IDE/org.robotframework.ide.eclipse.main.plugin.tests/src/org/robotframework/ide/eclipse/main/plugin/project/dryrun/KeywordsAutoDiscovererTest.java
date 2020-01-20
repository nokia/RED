/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.execution.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;

@ExtendWith(ProjectExtension.class)
public class KeywordsAutoDiscovererTest {

    @Project(dirs = { "libs" })
    static IProject project;

    private static RobotProject robotProject;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotProject = new RobotModel().createRobotProject(project);
    }

    @Test
    public void testKeywordFromStandardLibrary() throws Exception {
        robotProject.setStandardLibraries(Libraries.createStdLib("BuiltIn", "Log"));

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("BuiltIn.Log")).hasValueSatisfying(kwSource -> {
            assertThat(kwSource.getFilePath()).endsWith("BuiltIn.py");
            assertThat(kwSource.getLine()).isPositive();
            assertThat(kwSource.getOffset()).isPositive();
            assertThat(kwSource.getLength()).isPositive();
        });
    }

    @Test
    public void testDifferentKeywordsFromSameLibrary() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("TestLib",
                "#comment",
                "def kw_x():",
                "  return 0",
                "def   kw_abc ():",
                "  pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("TestLib.Kw X"))
                .hasValueSatisfying(equalSource("libs/TestLib.py", 1, 4, 4));
        assertThat(robotProject.getKeywordSource("TestLib.Kw Abc"))
                .hasValueSatisfying(equalSource("libs/TestLib.py", 3, 6, 6));
    }

    @Test
    public void testSameKeywordsFromDifferentLibraries() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("First",
                "#comment",
                "def kw_x():",
                "  pass"));
        libraries.putAll(createLibrary("Second",
                "def kw_x():",
                "  pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("First.Kw X"))
                .hasValueSatisfying(equalSource("libs/First.py", 1, 4, 4));
        assertThat(robotProject.getKeywordSource("Second.Kw X"))
                .hasValueSatisfying(equalSource("libs/Second.py", 0, 4, 4));
    }

    @Test
    public void testKeywordsDefinedWithSignatureDecorator() throws Exception {
        try (InputStream inputStream = this.getClass().getResourceAsStream("Decorator.py")) {
            createFile(project, "libs/Decorator.py", inputStream);
        }
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("SignatureDecorated",
                "from Decorator import decorator",
                "",
                "def precheck(**expected_kwargs):",
                "    @decorator",
                "    def wrapper(method, *args, **kwargs):",
                "        return method(*args, **kwargs)",
                "    return wrapper",
                "",
                "class SignatureDecorated(object):",
                "    @precheck()",
                "    def decorated_kw(self):",
                "        pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("SignatureDecorated.Decorated Kw"))
                .hasValueSatisfying(equalSource("libs/SignatureDecorated.py", 10, 8, 12));
    }

    @Test
    public void testKeywordsDefinedWithRobotDecorator() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("RobotDecorated",
                "import robot.api.deco",
                "from robot.api.deco import keyword",
                "@robot.api.deco.keyword('Add ${x:\\d+}')",
                "def add(x):",
                "  pass",
                "@keyword(name='Deco')",
                "def decorated_method():",
                "  pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("RobotDecorated.Add ${x:\\d+}"))
                .hasValueSatisfying(equalSource("libs/RobotDecorated.py", 3, 4, 3));
        assertThat(robotProject.getKeywordSource("RobotDecorated.Deco"))
                .hasValueSatisfying(equalSource("libs/RobotDecorated.py", 6, 4, 16));
    }

    @Test
    public void testKeywordsFromDynamicLibrary() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("DynaLib",
                "class DynaLib:",
                "  def get_keyword_names(self):",
                "    return ['Dyna Kw', 'Other Kw']",
                "  def run_keyword(self, name, args):",
                "    pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("DynaLib.Dyna Kw"))
                .hasValueSatisfying(equalSource("libs/DynaLib.py", 3, 6, 11));
        assertThat(robotProject.getKeywordSource("DynaLib.Other Kw"))
                .hasValueSatisfying(equalSource("libs/DynaLib.py", 3, 6, 11));
    }

    @Test
    public void testKeywordsFromClassHierarchy() throws Exception {
        createFile(project, "libs/Parent.py",
                "class Parent:",
                "  def parent_kw(self, arg):",
                "    pass");
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("Child",
                "import Parent",
                "class Child(Parent.Parent):",
                "  def child_kw(self, arg):",
                "    pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("Child.Parent Kw"))
                .hasValueSatisfying(equalSource("libs/Parent.py", 1, 6, 9));
        assertThat(robotProject.getKeywordSource("Child.Child Kw"))
                .hasValueSatisfying(equalSource("libs/Child.py", 2, 6, 8));
    }

    @Test
    public void testKeywordsFromImportedLibraries() throws Exception {
        createFile(project, "libs/External.py",
                "def ex_kw(args):",
                "  pass");
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("Internal",
                "from External import ex_kw",
                "def int_kw(args):",
                "  pass"));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        assertThat(robotProject.getKeywordSource("Internal.Ex Kw"))
                .hasValueSatisfying(equalSource("libs/External.py", 0, 4, 5));
        assertThat(robotProject.getKeywordSource("Internal.Int Kw"))
                .hasValueSatisfying(equalSource("libs/Internal.py", 1, 4, 6));
    }

    private Map<ReferencedLibrary, LibrarySpecification> createLibrary(final String name, final String... lines)
            throws IOException, CoreException {
        final IFile sourceFile = createFile(project, "libs/" + name + ".py", lines);

        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, name,
                sourceFile.getFullPath().makeRelative().toPortableString());

        return ImmutableMap.of(library, LibrarySpecification.create(name));
    }

    private void configureProject(final Map<ReferencedLibrary, LibrarySpecification> libraries)
            throws IOException, CoreException {
        final RobotProjectConfig config = new RobotProjectConfig();
        for (final ReferencedLibrary referencedLibrary : libraries.keySet()) {
            config.addReferencedLibrary(referencedLibrary);
        }
        configure(project, config);
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(libraries.entrySet().stream().collect(
                        toMap(entry -> LibraryDescriptor.ofReferencedLibrary(entry.getKey(),
                                ReferencedLibraryArgumentsVariant.create()), entry -> entry.getValue())));
    }

    private Consumer<RobotDryRunKeywordSource> equalSource(final String expectedFilePath, final int expectedLine,
            final int expectedOffset, final int expectedLength) {
        return kwSource -> {
            assertThat(kwSource.getFilePath()).isEqualTo(getFile(project, expectedFilePath).getLocation().toOSString());
            assertThat(kwSource.getLine()).isEqualTo(expectedLine);
            assertThat(kwSource.getOffset()).isEqualTo(expectedOffset);
            assertThat(kwSource.getLength()).isEqualTo(expectedLength);
        };
    }

}
