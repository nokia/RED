/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.stream.Stream;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedFileLocationProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedLibraryProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public abstract class ImportsProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile model;

    private final IRowDataProvider<?> dataProvider;

    private final SettingsGroup importType;

    protected ImportsProposalsProvider(final RobotSuiteFile model, final IRowDataProvider<?> dataProvider,
            final SettingsGroup importType) {
        this.model = model;
        this.importType = importType;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        if (!areApplicable((NatTableAssistantContext) context)) {
            return new RedContentProposal[0];
        }

        final String prefix = contents.substring(0, position);
        final Stream<? extends AssistProposal> librariesProposals = importType == SettingsGroup.LIBRARIES
                ? new RedLibraryProposals(model).getLibrariesProposals(prefix).stream()
                : Stream.empty();
        final Stream<? extends AssistProposal> fileLocationProposals = RedFileLocationProposals
                .create(importType, model)
                .getFilesLocationsProposals(prefix)
                .stream();
        return Stream.concat(librariesProposals, fileLocationProposals)
                .map(proposal -> new AssistProposalAdapter(proposal, p -> true))
                .toArray(RedContentProposal[]::new);
    }

    private boolean areApplicable(final NatTableAssistantContext tableContext) {
        return tableContext.getColumn() == 1 && isValidImportSetting(tableContext.getRow());
    }

    private boolean isValidImportSetting(final int row) {
        final Object element = dataProvider.getRowObject(row);
        return element instanceof RobotSetting && ((RobotSetting) element).getGroup() == importType;
    }

    public static class VariableFileLocationsProposalsProvider extends ImportsProposalsProvider {

        public VariableFileLocationsProposalsProvider(final RobotSuiteFile model,
                final IRowDataProvider<?> dataProvider) {
            super(model, dataProvider, SettingsGroup.VARIABLES);
        }
    }

    public static class ResourceFileLocationsProposalsProvider extends ImportsProposalsProvider {

        public ResourceFileLocationsProposalsProvider(final RobotSuiteFile model,
                final IRowDataProvider<?> dataProvider) {
            super(model, dataProvider, SettingsGroup.RESOURCES);
        }
    }

    public static class LibrariesProposalsProvider extends ImportsProposalsProvider {

        public LibrariesProposalsProvider(final RobotSuiteFile model, final IRowDataProvider<?> dataProvider) {
            super(model, dataProvider, SettingsGroup.LIBRARIES);
        }
    }
}
