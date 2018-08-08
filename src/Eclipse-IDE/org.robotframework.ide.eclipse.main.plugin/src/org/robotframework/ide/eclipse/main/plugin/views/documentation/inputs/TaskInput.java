/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;

public class TaskInput extends InternalElementInput<RobotTask> {

    public TaskInput(final RobotTask task) {
        super(task);
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return WorkspaceFileUri.createShowTaskDocUri(element.getSuiteFile().getFile(), element.getName());
    }

    @Override
    protected String createHeader() {
        return createHeader(element);
    }

    private static String createHeader(final RobotTask task) {
        final Optional<String> templateInUse = task.getTemplateInUse();
        final Optional<URI> imgUri = templateInUse.map(t -> RedImages.getTemplatedRpaTaskImageUri())
                .orElseGet(() -> RedImages.getRpaTaskImageUri());
        
        final IFile file = task.getSuiteFile().getFile();

        final String srcHref = createShowTaskSrcUri(file, task.getName());
        final String srcLabel = file.getFullPath().toString();
        final String docHref = createShowSuiteDocUri(file);

        final String source = String.format("%s [%s]", Formatters.hyperlink(srcHref, srcLabel),
                Formatters.hyperlink(docHref, "Documentation"));

        final List<List<String>> table = new ArrayList<>();
        table.add(newArrayList("Source", source));
        templateInUse.ifPresent(template -> table.add(newArrayList("Template", template)));

        return Formatters.simpleHeader(imgUri, task.getName(), table);
    }

    private static String createShowTaskSrcUri(final IFile file, final String label) {
        try {
            return WorkspaceFileUri.createShowTaskSourceUri(file, label).toString();
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

    private static String provideRawText(final RobotTask element) throws DocumentationInputGenerationException {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(element.getName()).append("\n");
        builder.append("Source: ").append(element.getSuiteFile().getFile().getFullPath().toString()).append("\n");
        element.getTemplateInUse().ifPresent(template -> builder.append("Template: " + template + "\n\n"));
        builder.append(element.getDocumentation());
        return builder.toString();
    }

    public static class TaskOnSettingInput extends InternalElementInput<RobotDefinitionSetting> {

        public TaskOnSettingInput(final RobotDefinitionSetting element) {
            super(element);
        }

        @Override
        public URI getInputUri() throws URISyntaxException {
            return WorkspaceFileUri.createShowTaskDocUri(element.getSuiteFile().getFile(), getTask().getName());
        }

        @Override
        protected String createHeader() {
            return TaskInput.createHeader(getTask());
        }

        @Override
        protected Documentation createDocumentation() {
            return getTask().createDocumentation();
        }

        @Override
        public String provideRawText() throws DocumentationInputGenerationException {
            return TaskInput.provideRawText(getTask());
        }

        private RobotTask getTask() {
            return (RobotTask) element.getParent();
        }
    }
}
