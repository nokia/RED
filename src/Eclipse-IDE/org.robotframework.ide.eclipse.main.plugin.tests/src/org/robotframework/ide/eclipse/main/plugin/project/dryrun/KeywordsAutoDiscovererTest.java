/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class KeywordsAutoDiscovererTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordsAutoDiscovererTest.class);

    private static RobotProject robotProject;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        projectProvider.createDir("libs");
    }

    @Test
    public void testDifferentKeywordsFromSameLibrary() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("TestLib",
                new String[] { "#comment", "def kw_x():", "  return 0", "def   kw_abc ():", "  pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("TestLib.Kw X", "libs/TestLib.py", 1, 4, 4);
        verifyKwSource("TestLib.Kw Abc", "libs/TestLib.py", 3, 6, 6);
    }

    @Test
    public void testSameKeywordsFromDifferentLibraries() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("First", new String[] { "#comment", "def kw_x():", "  pass" }));
        libraries.putAll(createLibrary("Second", new String[] { "def kw_x():", "  pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("First.Kw X", "libs/First.py", 1, 4, 4);
        verifyKwSource("Second.Kw X", "libs/Second.py", 0, 4, 4);
    }

    @Test
    public void testKeywordsDefinedWithDecorator() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("Decorated",
                new String[] { "import robot.api.deco", "from robot.api.deco import keyword",
                        "@robot.api.deco.keyword('Add ${x:\\d+}')", "def add(x):", "  pass", "@keyword(name='Deco')",
                        "def decorated_method():", "  pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("Decorated.Add ${x:\\d+}", "libs/Decorated.py", 3, 4, 3);
        verifyKwSource("Decorated.Deco", "libs/Decorated.py", 6, 4, 16);
    }

    @Test
    public void testKeywordsFromDynamicLibrary() throws Exception {
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("DynaLib", new String[] { "class DynaLib:", "  def get_keyword_names(self):",
                "    return ['Dyna Kw', 'Other Kw']", "  def run_keyword(self, name, args):", "    pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("DynaLib.Dyna Kw", "libs/DynaLib.py", 3, 6, 11);
        verifyKwSource("DynaLib.Other Kw", "libs/DynaLib.py", 3, 6, 11);
    }

    @Test
    public void testKeywordsFromClassHierarchy() throws Exception {
        projectProvider.createFile("libs/Parent.py", "class Parent:", "  def parent_kw(self, arg):", "    pass");
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("Child", new String[] { "import Parent", "class Child(Parent.Parent):",
                "  def child_kw(self, arg):", "    pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("Child.Parent Kw", "libs/Parent.py", 1, 6, 9);
        verifyKwSource("Child.Child Kw", "libs/Child.py", 2, 6, 8);
    }

    @Test
    public void testKeywordsFromImportedLibraries() throws Exception {
        projectProvider.createFile("libs/External.py", "def ex_kw(args):", "  pass");
        final Map<ReferencedLibrary, LibrarySpecification> libraries = new HashMap<>();
        libraries.putAll(createLibrary("Internal",
                new String[] { "from External import ex_kw", "def int_kw(args):", "  pass" }));
        configureProject(libraries);

        new KeywordsAutoDiscoverer(robotProject).start();

        verifyKwSource("Internal.Ex Kw", "libs/External.py", 0, 4, 5);
        verifyKwSource("Internal.Int Kw", "libs/Internal.py", 1, 4, 6);
    }

    private Map<ReferencedLibrary, LibrarySpecification> createLibrary(final String name, final String[] lines)
            throws IOException, CoreException {
        final IFile sourceFile = projectProvider.createFile("libs/" + name + ".py", lines);

        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, name,
                sourceFile.getFullPath().makeRelative().removeLastSegments(1).toPortableString());

        final LibrarySpecification libSpec = new LibrarySpecification();
        libSpec.setName(name);
        libSpec.setSourceFile(sourceFile);

        return ImmutableMap.of(library, libSpec);
    }

    private void configureProject(final Map<ReferencedLibrary, LibrarySpecification> libraries)
            throws IOException, CoreException {
        final RobotProjectConfig config = new RobotProjectConfig();
        for (final ReferencedLibrary referencedLibrary : libraries.keySet()) {
            config.addReferencedLibrary(referencedLibrary);
        }
        projectProvider.configure(config);
        robotProject.setReferencedLibraries(libraries);
    }

    private void verifyKwSource(final String qualifiedKwName, final String expectedFilePath, final int expectedLine,
            final int expectedOffset, final int expectedLength) {
        final Optional<RobotDryRunKeywordSource> kwSource = robotProject.getKeywordSource(qualifiedKwName);
        assertThat(kwSource).isPresent();
        assertThat(kwSource.get().getFilePath())
                .isEqualTo(projectProvider.getFile(expectedFilePath).getLocation().toOSString());
        assertThat(kwSource.get().getLine()).isEqualTo(expectedLine);
        assertThat(kwSource.get().getOffset()).isEqualTo(expectedOffset);
        assertThat(kwSource.get().getLength()).isEqualTo(expectedLength);
    }

}
