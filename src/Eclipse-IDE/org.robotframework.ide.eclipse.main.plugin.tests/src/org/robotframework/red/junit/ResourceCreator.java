/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @deprecated Use {@link ResourceCreatorExtension} due to move to JUnit 5
 * @author anglart
 */
@Deprecated
public class ResourceCreator implements TestRule {

    private final List<IResource> createdResources = new ArrayList<>();

    public void createLink(final URI targetUri, final IFile linkingFile) throws CoreException {
        linkingFile.createLink(targetUri, IResource.REPLACE, null);
        createdResources.add(linkingFile);
    }

    public void createLink(final URI targetUri, final IFolder linkingFolder) throws CoreException {
        linkingFolder.createLink(targetUri, IResource.REPLACE, null);
        createdResources.add(linkingFolder);
    }

    public void createVirtual(final IFolder virtualFolder) throws CoreException {
        virtualFolder.create(IResource.REPLACE | IResource.VIRTUAL, true, null);
        createdResources.add(virtualFolder);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    for (final IResource resource : createdResources) {
                        resource.delete(true, null);
                    }
                }
            }
        };
    }
}
