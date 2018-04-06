/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.Documentation;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsFormatter;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.LibraryUri;

import com.google.common.html.HtmlEscapers;

public class LibrarySpecificationInput implements DocumentationViewInput {

    private final RobotProject project;

    private final LibrarySpecification specification;

    public LibrarySpecificationInput(final RobotProject project, final LibrarySpecification specification) {
        this.project = project;
        this.specification = specification;
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return LibraryUri.createShowLibraryDocUri(project.getName(), specification.getName());
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        if (wrappedInput instanceof IProject) {
            return project.getProject().equals(wrappedInput);
        }
        return specification == wrappedInput;
    }

    @Override
    public String provideHtml() {
        final RobotRuntimeEnvironment environment = project.getRuntimeEnvironment();
        final String header = createHeader(project.getProject(), specification);
        final Documentation doc = specification.createDocumentation();
        final String footer = createFooter(specification, environment);

        return new DocumentationsFormatter(environment).format(header, doc, footer, this::localKeywordsLinker);
    }

    static String createHeader(final IProject project, final LibrarySpecification specification) {
        final Optional<URI> imgUri = RedImages.getBookImageUri();

        final String srcHref = createShowSrcUri(project, specification);
        final String source = Formatters.hyperlink(srcHref, specification.getName());

        final ArgumentsDescriptor descriptor = getDescriptor(specification);
        final String args = HtmlEscapers.htmlEscaper().escape(descriptor.getDescription());

        final String header = Formatters.simpleHeader(imgUri, specification.getName(),
                newArrayList("Source", source),
                newArrayList("Version", specification.getVersion()),
                newArrayList("Scope", specification.getScope()),
                newArrayList("Arguments", args));
        return header + Formatters.title("Introduction", 2);
    }

    private static String createShowSrcUri(final IProject project, final LibrarySpecification specification) {
        try {
            return LibraryUri.createShowLibrarySourceUri(project.getName(), specification.getName()).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    static String createFooter(final LibrarySpecification specification, final RobotRuntimeEnvironment env) {
        final String shortcuts = specification.getKeywordsStream()
                .map(KeywordSpecification::getName)
                .map(name -> "`" + name + "`")
                .collect(joining(" &middot; "));
        
        final StringBuilder builder = new StringBuilder();
        if (specification.getConstructor() != null) {
            builder.append(Formatters.title("Importing", 2));
            builder.append(specification.createConstructorDocumentation().provideFormattedDocumentation(env));
        }
        builder.append(Formatters.title("Shortcuts", 2));
        builder.append(Formatters.paragraph(shortcuts));
        return builder.toString();
    }

    private static ArgumentsDescriptor getDescriptor(final LibrarySpecification specification) {
        return specification.getConstructor() == null ? ArgumentsDescriptor.createDescriptor()
                : specification.getConstructor().createArgumentsDescriptor();
    }

    private String localKeywordsLinker(final String name) {
        try {
            return LibraryUri.createShowKeywordDocUri(project.getName(), specification.getName(), name).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        return provideRawText(specification);
    }

    static String provideRawText(final LibrarySpecification specification)
            throws DocumentationInputGenerationException {
        final StringBuilder builder = new StringBuilder();
        builder.append("Version: ").append(specification.getVersion()).append("\n");
        builder.append("Scope: ").append(specification.getScope()).append("\n");
        builder.append("Arguments: ").append(getDescriptor(specification).getDescription()).append("\n\n");
        builder.append(specification.getDocumentation());
        return builder.toString();
    }

    @Override
    public void showInput(final IWorkbenchPage page) {
        // TODO : where should we open specification input? should we at all...?
    }

    @Override
    public IFile generateHtmlLibdoc() {
        return new LibrariesBuilder(new BuildLogger()).buildHtmlLibraryDoc(project, specification);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            final LibrarySpecificationInput that = (LibrarySpecificationInput) obj;
            return this.project.equals(that.project) && Objects.equals(this.specification, that.specification)
                    && Objects.equals(this.specification.getDocumentation(), that.specification.getDocumentation());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, specification);
    }
}
