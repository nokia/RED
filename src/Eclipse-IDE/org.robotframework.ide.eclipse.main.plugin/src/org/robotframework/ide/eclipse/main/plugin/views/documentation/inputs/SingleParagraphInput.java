/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.Documentation.DocFormat;


public class SingleParagraphInput implements DocumentationViewInput {

    private final Supplier<String> paragraphSupplier;

    public SingleParagraphInput(final Supplier<String> paragraphSupplier) {
        this.paragraphSupplier = paragraphSupplier;
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        return false;
    }

    @Override
    public String provideHtml() throws DocumentationInputGenerationException {
        return provideHtml(null);
    }

    @Override
    public String provideHtml(final RobotRuntimeEnvironment environment) throws DocumentationInputGenerationException {
        final Documentation doc = new Documentation(DocFormat.ROBOT, paragraphSupplier.get());
        return new DocumentationsFormatter(environment).format(doc);
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        return paragraphSupplier.get();
    }

    @Override
    public void showInput(final IWorkbenchPage page) throws DocumentationInputOpenException {
        throw new IllegalStateException();
    }

    @Override
    public IFile generateHtmlLibdoc() {
        throw new IllegalStateException();
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        throw new IllegalStateException();
    }

}
