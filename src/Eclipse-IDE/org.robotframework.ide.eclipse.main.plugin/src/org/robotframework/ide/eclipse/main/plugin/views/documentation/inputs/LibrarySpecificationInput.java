/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsFormatter;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.LibraryUri;

public class LibrarySpecificationInput extends DocumentationViewInput {

    private final RobotProject project;

    private final LibrarySpecification specification;

    public LibrarySpecificationInput(final RobotProject project, final LibrarySpecification specification) {
        this.project = project;
        this.specification = specification;
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        return specification == wrappedInput;
    }

    @Override
    public void prepare() {
        // nothing to prepare
    }

    @Override
    public String provideHtml() {
        final RobotRuntimeEnvironment environment = project.getRuntimeEnvironment();
        final String header = createHeader(specification);
        final Documentation doc = specification.createDocumentation();

        return new DocumentationsFormatter(environment).format(header, doc, this::localKeywordsLinker);
    }

    static String createHeader(final LibrarySpecification specification) {
        final Optional<URI> imgUri = RedImages.getBookImageUri();
        final ArgumentsDescriptor descriptor = specification.getConstructor() == null
                ? ArgumentsDescriptor.createDescriptor()
                : specification.getConstructor().createArgumentsDescriptor();

        return Formatters.formatSimpleHeader(imgUri, specification.getName(),
                newArrayList("Version", specification.getVersion()),
                newArrayList("Scope", specification.getScope()),
                newArrayList("Arguments", descriptor.getDescription()));
    }

    private String localKeywordsLinker(final String name) {
        try {
            return LibraryUri.createShowKeywordDocUri(project.getName(), specification.getName(), name).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    @Override
    public void showInput(final IWorkbenchPage page) {
        // TODO : where should we open specification input? should we at all...?
    }

}
