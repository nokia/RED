/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit.jupiter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * JUnit Jupiter extension which allows to create resources which will be removed
 * after each test. Should be used directly using {@link RegisterExtension} mechanism.
 * 
 * @author anglart
 */
public class ResourceCreatorExtension implements Extension, AfterEachCallback {

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
    public void afterEach(final ExtensionContext context) throws Exception {
        for (final IResource resource : createdResources) {
            resource.delete(true, null);
        }
        createdResources.clear();
    }
}
