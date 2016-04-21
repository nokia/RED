/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigWriter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

/**
 * @author Michal Anglart
 *
 */
public class ProjectProvider implements TestRule {

    private final String projectName;

    private IProject project;

    public ProjectProvider(final String projectName) {
        this.projectName = projectName;
    }

    public IProject getProject() {
        return project;
    }

    public void addRobotNature() throws CoreException {
        RobotProjectNature.addRobotNature(project, null);
    }

    public void removeRobotNature() throws CoreException {
        RobotProjectNature.removeRobotNature(project, null);
    }

    public void configure() throws IOException, CoreException {
        configure(new RobotProjectConfig());
    }

    public void configure(final RobotProjectConfig config) throws IOException, CoreException {
        createFile(Path.fromPortableString("red.xml"), "");
        final RobotProjectConfigWriter configWriter = new RobotProjectConfigWriter();
        configWriter.writeConfiguration(config, project);
    }

    public void deconfigure() throws CoreException {
        project.findMember("red.xml").delete(true, null);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    if (project == null) {
                        project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
                        project.create(null);
                        project.open(null);
                    }
                    base.evaluate();
                } finally {
                    if (project != null && project.exists()) {
                        project.delete(true, null);
                    }
                }
            }
        };
    }

    public IFolder createDir(final IPath dirPath) throws CoreException {
        final IFolder directory = project.getFolder(dirPath);
        directory.create(true, true, null);
        return directory;
    }

    public IFile createFile(final IPath filePath, final String... lines) throws IOException, CoreException {
        final IFile file = project.getFile(filePath);
        try (InputStream source = new ByteArrayInputStream(Joiner.on('\n').join(lines).getBytes(Charsets.UTF_8))) {
            if (file.exists()) {
                file.setContents(source, true, false, null);
            } else {
                file.create(source, true, null);
            }
        }
        return file;
    }

    public IFile getFile(final IPath filePath) {
        return project.getFile(filePath);
    }

    public IFolder getDir(final IPath dirPath) {
        return project.getFolder(dirPath);
    }

}
