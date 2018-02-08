/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ImportLibraryTableFixer;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class KeywordProposalsInSettingsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    public KeywordProposalsInSettingsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        if (!areApplicable((NatTableAssistantContext) context)) {
            return new RedContentProposal[0];
        }

        final String prefix = contents.substring(0, position);
        final List<? extends AssistProposal> keywordsProposals = new RedKeywordProposals(suiteFile)
                .getKeywordProposals(prefix);

        return keywordsProposals.stream()
                .map(proposal -> new AssistProposalAdapter(proposal,
                        () -> createOperationsToPerformAfterAccepting((RedKeywordProposal) proposal)))
                .toArray(RedContentProposal[]::new);
    }

    private boolean areApplicable(final NatTableAssistantContext tableContext) {
        return tableContext.getColumn() == 1 && isKeywordBasedSetting(dataProvider, tableContext.getRow());
    }

    private List<Runnable> createOperationsToPerformAfterAccepting(final RedKeywordProposal proposedKeyword) {
        final List<Runnable> operations = new ArrayList<>();
        if (!proposedKeyword.isAccessible()) {
            operations.add(() -> new ImportLibraryTableFixer(proposedKeyword.getSourceName()).apply(suiteFile));
        }
        return operations;
    }

    static boolean isKeywordBasedSetting(final IRowDataProvider<?> dataProvider, final int row) {
        final Entry<?, ?> entry = (Entry<?, ?>) dataProvider.getRowObject(row);
        final String settingName = (String) entry.getKey();
        final RobotTokenType actualType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);

        return EnumSet
                .of(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
                        RobotTokenType.SETTING_TEST_SETUP_DECLARATION, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION,
                        RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION)
                .contains(actualType);
    }
}
