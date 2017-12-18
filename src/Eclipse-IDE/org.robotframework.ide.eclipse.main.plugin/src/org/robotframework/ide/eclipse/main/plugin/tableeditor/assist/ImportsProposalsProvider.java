/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
        final String prefix = contents.substring(0, position);

        final List<IContentProposal> proposals = newArrayList();

        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        if (tableContext.getColumn() == 1 && isValidImportSetting(tableContext.getRow())) {

            if (importType == SettingsGroup.LIBRARIES) {
                new RedLibraryProposals(model).getLibrariesProposals(prefix)
                        .stream()
                        .map(AssistProposalAdapter::new)
                        .forEach(adapter -> proposals.add(adapter));
            }
            RedFileLocationProposals.create(importType, model)
                    .getFilesLocationsProposals(prefix)
                    .stream()
                    .map(AssistProposalAdapter::new)
                    .forEach(adapter -> proposals.add(adapter));
        }
        return proposals.toArray(new RedContentProposal[0]);
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

        public LibrariesProposalsProvider(final RobotSuiteFile model,
                final IRowDataProvider<?> dataProvider) {
            super(model, dataProvider, SettingsGroup.LIBRARIES);
        }
    }
}
