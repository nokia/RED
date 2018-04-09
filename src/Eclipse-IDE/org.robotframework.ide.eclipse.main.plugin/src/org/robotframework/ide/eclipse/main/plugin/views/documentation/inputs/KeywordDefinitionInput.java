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

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;

import com.google.common.html.HtmlEscapers;

public class KeywordDefinitionInput extends InternalElementInput<RobotKeywordDefinition> {

    public KeywordDefinitionInput(final RobotKeywordDefinition keyword) {
        super(keyword);
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return WorkspaceFileUri.createShowKeywordDocUri(element.getSuiteFile().getFile(), element.getName());
    }

    @Override
    protected String createHeader() {
        return createHeader(element);
    }

    private static String createHeader(final RobotKeywordDefinition keyword) {
        final Optional<URI> imgUri = RedImages.getUserKeywordImageUri();
        final IFile file = keyword.getSuiteFile().getFile();

        final String srcHref = createShowKeywordSrcUri(file, keyword.getName());
        final String srcLabel = file.getFullPath().toString();
        final String docHref = createShowSuiteDocUri(file);

        final String source = String.format("%s [%s]", Formatters.hyperlink(srcHref, srcLabel),
                Formatters.hyperlink(docHref, "Documentation"));

        final String args = HtmlEscapers.htmlEscaper().escape(keyword.createArgumentsDescriptor().getDescription());

        return Formatters.simpleHeader(imgUri, keyword.getName(),
                newArrayList("Source", source),
                newArrayList("Arguments", args));
    }

    private static String createShowKeywordSrcUri(final IFile file, final String label) {
        try {
            return WorkspaceFileUri.createShowKeywordSourceUri(file, label).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    private static String createShowSuiteDocUri(final IFile file) {
        try {
            return WorkspaceFileUri.createShowSuiteDocUri(file).toString();
        } catch (final URISyntaxException e) {
            return "#";
        }
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        return provideRawText(element);
    }

    private static String provideRawText(final RobotKeywordDefinition keyword)
            throws DocumentationInputGenerationException {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(keyword.getName()).append("\n");
        builder.append("Source: ").append(keyword.getSuiteFile().getFile().getFullPath().toString()).append("\n");
        builder.append("Arguments: ").append(keyword.createArgumentsDescriptor().getDescription()).append("\n\n");
        builder.append(keyword.getDocumentation());
        return builder.toString();
    }
    
    public static class KeywordDefinitionOnSettingInput extends InternalElementInput<RobotDefinitionSetting> {

        public KeywordDefinitionOnSettingInput(final RobotDefinitionSetting element) {
            super(element);
        }

        @Override
        public URI getInputUri() throws URISyntaxException {
            return WorkspaceFileUri.createShowKeywordDocUri(element.getSuiteFile().getFile(), getKeyword().getName());
        }

        @Override
        protected String createHeader() {
            return KeywordDefinitionInput.createHeader(getKeyword());
        }

        @Override
        protected Documentation createDocumentation() {
            return getKeyword().createDocumentation();
        }

        @Override
        public String provideRawText() throws DocumentationInputGenerationException {
            return KeywordDefinitionInput.provideRawText(getKeyword());
        }

        private RobotKeywordDefinition getKeyword() {
            return (RobotKeywordDefinition) element.getParent();
        }
    }
}
