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
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;

import com.google.common.io.Files;

public class SuiteFileInput extends InternalElementInput<RobotSuiteFile> {

    public SuiteFileInput(final RobotSuiteFile suiteFile) {
        super(suiteFile);
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return WorkspaceFileUri.createShowSuiteDocUri(element.getSuiteFile().getFile());
    }

    @Override
    protected String createHeader() {
        return suiteHeader(element);
    }

    private static String suiteHeader(final RobotSuiteFile suiteFile) {
        final Optional<URI> imgUri = RedImages.getRobotFileImageUri();

        final IFile file = suiteFile.getFile();
        final URI srcHref = WorkspaceFileUri.createFileUri(file);
        final String srcLabel = file.getFullPath().toString();

        final String source = Formatters.hyperlink(srcHref, srcLabel);
        final String header = Formatters.simpleHeader(imgUri, suiteFile.getName(),
                newArrayList("Source", source));
        return header + Formatters.title("Introduction", 2);
    }

    @Override
    protected String createFooter() {
        return suiteFooter(element);
    }

    private static String suiteFooter(final RobotSuiteFile suiteFile) {
        final String shortcuts = suiteFile.getUserDefinedKeywords()
                .stream()
                .map(RobotKeywordDefinition::getName)
                .map(name -> "`" + name + "`")
                .collect(joining(" &middot; "));

        final StringBuilder builder = new StringBuilder();
        builder.append(Formatters.title("Shortcuts", 2));
        builder.append(Formatters.paragraph(shortcuts));
        return builder.toString();
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        return provideRawText(element);
    }

    private static String provideRawText(final RobotSuiteFile suiteFile) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(Files.getNameWithoutExtension(suiteFile.getName())).append("\n");
        builder.append("Source: ").append(suiteFile.getFile().getFullPath().toString()).append("\n");
        builder.append(suiteFile.getDocumentation());
        return builder.toString();
    }

    public static class SuiteFileOnSettingInput extends InternalElementInput<RobotSetting> {

        public SuiteFileOnSettingInput(final RobotSetting docSetting) {
            super(docSetting);
        }

        @Override
        public URI getInputUri() throws URISyntaxException {
            return WorkspaceFileUri.createShowSuiteDocUri(element.getSuiteFile().getFile());
        }

        @Override
        protected String createHeader() {
            return SuiteFileInput.suiteHeader(element.getSuiteFile());
        }

        @Override
        protected Documentation createDocumentation() {
            return element.getSuiteFile().createDocumentation();
        }

        @Override
        protected String createFooter() {
            return SuiteFileInput.suiteFooter(element.getSuiteFile());
        }

        @Override
        public String provideRawText() throws DocumentationInputGenerationException {
            return SuiteFileInput.provideRawText(element.getSuiteFile());
        }
    }
}
