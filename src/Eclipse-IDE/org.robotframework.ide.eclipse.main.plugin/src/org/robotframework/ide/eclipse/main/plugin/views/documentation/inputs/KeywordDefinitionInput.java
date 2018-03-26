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

public class KeywordDefinitionInput extends InternalElementInput<RobotKeywordDefinition> {

    public KeywordDefinitionInput(final RobotKeywordDefinition keyword) {
        super(keyword);
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

        final String source = String.format("%s [%s]", Formatters.formatHyperlink(srcHref, srcLabel),
                Formatters.formatHyperlink(docHref, "Documentation"));

        return Formatters.formatSimpleHeader(imgUri, keyword.getName(),
                newArrayList("Source", source),
                newArrayList("Arguments", keyword.createArgumentsDescriptor().getDescription()));
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
    
    public static class KeywordDefinitionOnSettingInput extends InternalElementInput<RobotDefinitionSetting> {

        public KeywordDefinitionOnSettingInput(final RobotDefinitionSetting element) {
            super(element);
        }

        @Override
        protected String createHeader() {
            return KeywordDefinitionInput.createHeader((RobotKeywordDefinition) element.getParent());
        }

        @Override
        protected Documentation createDocumentation() {
            return ((RobotKeywordDefinition) element.getParent()).createDocumentation();
        }
    }
}
