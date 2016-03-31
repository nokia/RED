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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

/**
 * @author Michal Anglart
 *
 */
public class ProjectProvider implements TestRule {

    private IProject project;

    public IProject create(final String name) throws CoreException {
        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        project.create(null);
        project.open(null);
        return project;
    }
    
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
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
        directory.create(true, false, null);
        return directory;
    }

    public IFile createFile(final IPath filePath, final String... lines) throws IOException, CoreException {
        final IFile file = project.getFile(filePath);
        try (InputStream source = new ByteArrayInputStream(Joiner.on('\n').join(lines).getBytes(Charsets.UTF_8))) {
            file.create(source, true, null);
        }
        return file;
    }

    public IFile getFile(final IPath filePath) {
        return project.getFile(filePath);
    }

    public IResource getDir(final IPath dirPath) {
        return project.getFolder(dirPath);
    }

}
