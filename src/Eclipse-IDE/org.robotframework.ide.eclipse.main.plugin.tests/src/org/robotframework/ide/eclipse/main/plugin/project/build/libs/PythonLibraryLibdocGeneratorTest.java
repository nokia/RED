/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */

package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class PythonLibraryLibdocGeneratorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PythonLibraryLibdocGenerator.class);

    private RobotModel model;

    private RobotProject robotProject;

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createDir("lib");
        projectProvider.createDir("lib/module");
        projectProvider.createFile("lib/module/ModuleClass.py", "class ModuleClass(object):", "  def kw():", "   pass");
    }

    @Before
    public void before() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(projectProvider.getProject());
        projectProvider.configure();
    }

    @After
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void verifyIfCreateLibdocInSeparateProcessMethodIsCalled_whenLibdocGenerationInSeparateProcessPreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                true);

        final String libName = "ModuleClass";
        final IFile libFile = robotProject.getFile("lib/module/" + libName + ".py");
        final String libPath = libFile.getLocation().toString();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        final IFile targetSpecFile = libspecsFolder.getXmlSpecFile("ModuleClass_1234567");
        final LibdocFormat format = LibdocFormat.XML;
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(projectProvider.getProject(),
                robotProject.getRobotProjectConfig()).createAdditionalEnvironmentSearchPaths();

        final PythonLibraryLibdocGenerator pythonGenerator = new PythonLibraryLibdocGenerator(libName, libPath,
                targetSpecFile, format);

        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        pythonGenerator.generateLibdoc(runtimeEnvironment, additionalPaths);

        verify(runtimeEnvironment).createLibdocInSeparateProcess(libFile.getLocation().toPortableString(),
                targetSpecFile.getLocation().toFile(), format, additionalPaths);
    }

    @Test
    public void verifyIfCreateLibdocMethodIsCalled_whenLibdocGenerationInSeparateProcessPreferenceIsNotEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                false);

        final String libName = "ModuleClass";
        final IFile libFile = robotProject.getFile("lib/module/" + libName + ".py");
        final String libPath = libFile.getLocation().toString();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        final IFile targetSpecFile = libspecsFolder.getXmlSpecFile("ModuleClass_1234567");
        final LibdocFormat format = LibdocFormat.XML;
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(projectProvider.getProject(),
                robotProject.getRobotProjectConfig()).createAdditionalEnvironmentSearchPaths();

        final PythonLibraryLibdocGenerator pythonGenerator = new PythonLibraryLibdocGenerator(libName, libPath,
                targetSpecFile, format);

        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        pythonGenerator.generateLibdoc(runtimeEnvironment, additionalPaths);

        verify(runtimeEnvironment).createLibdoc(libFile.getLocation().toPortableString(),
                targetSpecFile.getLocation().toFile(), format, additionalPaths);
    }
}
