/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
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

public class KeywordSpecificationInput implements DocumentationViewInput {

    private final RobotProject project;

    private final LibrarySpecification libSpec;

    private final KeywordSpecification kwSpec;

    public KeywordSpecificationInput(final RobotProject project, final LibrarySpecification libSpec,
            final KeywordSpecification kwSpec) {
        this.project = project;
        this.libSpec = libSpec;
        this.kwSpec = kwSpec;
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return LibraryUri.createShowKeywordDocUri(project.getName(), libSpec.getName(), kwSpec.getName());
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        return kwSpec == wrappedInput;
    }

    @Override
    public String provideHtml() {
        return provideHtml(project.getRuntimeEnvironment());
    }

    @Override
    public String provideHtml(final RobotRuntimeEnvironment environment) throws DocumentationInputGenerationException {
        final String header = createHeader();
        final Documentation doc = libSpec.createKeywordDocumentation(kwSpec.getName());

        return new DocumentationsFormatter(environment).format(header, doc, this::localKeywordsLinker);
    }

    private String createHeader() {
        final Optional<URI> imgUri = RedImages.getKeywordImageUri();

        final String srcHref = createShowKeywordSrcUri();
        final String srcLabel = libSpec.getName();
        final String docHref = createShowLibDocUri();

        final String source = String.format("%s [%s]", Formatters.hyperlink(srcHref, srcLabel),
                Formatters.hyperlink(docHref, "Documentation"));

        final String args = HtmlEscapers.htmlEscaper().escape(kwSpec.createArgumentsDescriptor().getDescription());

        return Formatters.simpleHeader(imgUri, kwSpec.getName(),
                newArrayList("Source", source),
                newArrayList("Arguments", args));
    }

    private String createShowKeywordSrcUri() {
        try {
            return LibraryUri.createShowKeywordSourceUri(project.getName(), libSpec.getName(), kwSpec.getName())
                    .toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    private String createShowLibDocUri() {
        try {
            return LibraryUri.createShowLibraryDocUri(project.getName(), libSpec.getName()).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    private String localKeywordsLinker(final String name) {
        try {
            return LibraryUri.createShowKeywordDocUri(project.getName(), libSpec.getName(), name).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(kwSpec.getName()).append("\n");
        builder.append("Source: ").append(libSpec.getName()).append("\n");
        builder.append("Arguments: ").append(kwSpec.createArgumentsDescriptor().getDescription()).append("\n\n");
        builder.append(kwSpec.getDocumentation());
        return builder.toString();
    }

    @Override
    public void showInput(final IWorkbenchPage page) {
        // TODO : where should we open specification input? should we at all...?
    }

    @Override
    public IFile generateHtmlLibdoc() {
        return new LibrariesBuilder(new BuildLogger()).buildHtmlLibraryDoc(project, libSpec);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            final KeywordSpecificationInput that = (KeywordSpecificationInput) obj;
            return this.project.equals(that.project) && Objects.equals(this.libSpec, that.libSpec)
                    && Objects.equals(this.libSpec.getDocumentation(), that.libSpec.getDocumentation())
                    && Objects.equals(this.kwSpec, that.kwSpec)
                    && Objects.equals(this.kwSpec.getDocumentation(), that.kwSpec.getDocumentation());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, libSpec, kwSpec);
    }
}
