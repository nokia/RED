/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */

package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class PythonLibraryLibdocGeneratorTest {

    @Project(createDefaultRedXml = true, dirs = { "lib", "lib/module", "lib/outerModule" })
    static IProject project;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeAll
    public static void beforeClass() throws Exception {
        createFile(project, "lib/module/ModuleClass.py", "class ModuleClass(object):", "  def kw():", "   pass");
        createFile(project, "lib/outerModule/libFile.py", "class ModuleClass(object):", "  def kw():", "   pass");
    }

    @BeforeEach
    public void before() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(project);
    }

    @AfterEach
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @BooleanPreference(key = RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED, value = true)
    @Test
    public void verifyIfCreateLibdocInSeparateProcessMethodIsCalled_whenLibdocGenerationInSeparateProcessPreferenceIsEnabled()
            throws Exception {
        final String libName = "ModuleClass";
        final IFile libFile = robotProject.getFile("lib/module/" + libName + ".py");
        final String libPath = libFile.getLocation().toString();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        final IFile targetSpecFile = libspecsFolder.getXmlSpecFile("ModuleClass_1234567");
        final LibdocFormat format = LibdocFormat.XML;
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(project,
                robotProject.getRobotProjectConfig()).createAdditionalEnvironmentSearchPaths();

        final PythonLibraryLibdocGenerator pythonGenerator = new PythonLibraryLibdocGenerator(libName,
                new ArrayList<>(), libPath, targetSpecFile, format);

        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        pythonGenerator.generateLibdoc(runtimeEnvironment, additionalPaths);

        verify(runtimeEnvironment).createLibdocInSeparateProcess(libFile.getLocation().toPortableString(),
                targetSpecFile.getLocation().toFile(), format, additionalPaths);
    }

    @BooleanPreference(key = RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED, value = false)
    @Test
    public void verifyIfCreateLibdocMethodIsCalled_whenLibdocGenerationInSeparateProcessPreferenceIsNotEnabled()
            throws Exception {
        final String libName = "ModuleClass";
        final IFile libFile = robotProject.getFile("lib/module/" + libName + ".py");
        final String libPath = libFile.getLocation().toString();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        final IFile targetSpecFile = libspecsFolder.getXmlSpecFile("ModuleClass_1234567");
        final LibdocFormat format = LibdocFormat.XML;
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(project,
                robotProject.getRobotProjectConfig()).createAdditionalEnvironmentSearchPaths();

        final PythonLibraryLibdocGenerator pythonGenerator = new PythonLibraryLibdocGenerator(libName,
                new ArrayList<>(), libPath, targetSpecFile, format);

        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        pythonGenerator.generateLibdoc(runtimeEnvironment, additionalPaths);

        verify(runtimeEnvironment).createLibdoc(libFile.getLocation().toPortableString(),
                targetSpecFile.getLocation().toFile(), format, additionalPaths);
    }

    @BooleanPreference(key = RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED, value = true)
    @Test
    public void verifyIfCreateLibdocMethodInSeparateProcessIsCalled_forLibraryNameWithDots() {
        final String libName = "outerModule.libFile.ModuleClass";
        final String libPath = robotProject.getFile("lib/outerModule/libFile.py").getLocation().toString();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(robotProject.getProject());
        final IFile targetSpecFile = libspecsFolder.getXmlSpecFile("ModuleClass_1234567");
        final LibdocFormat format = LibdocFormat.XML;
        final EnvironmentSearchPaths additionalPaths = new RedEclipseProjectConfig(project,
                robotProject.getRobotProjectConfig()).createAdditionalEnvironmentSearchPaths();

        final PythonLibraryLibdocGenerator pythonGenerator = new PythonLibraryLibdocGenerator(libName,
                new ArrayList<>(), libPath, targetSpecFile, format);

        final IRuntimeEnvironment runtimeEnvironment = mock(IRuntimeEnvironment.class);

        pythonGenerator.generateLibdoc(runtimeEnvironment, additionalPaths);

        verify(runtimeEnvironment).createLibdocInSeparateProcess(libName, targetSpecFile.getLocation().toFile(), format,
                additionalPaths);
    }
}
