/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;

public interface DocumentationViewInput {

    public boolean contains(final Object wrappedInput);

    public String provideHtml() throws DocumentationInputGenerationException;

    public String provideRawText() throws DocumentationInputGenerationException;

    public void showInput(IWorkbenchPage page) throws DocumentationInputOpenException;

    public IFile generateHtmlLibdoc();

    public URI getInputUri() throws URISyntaxException;
}