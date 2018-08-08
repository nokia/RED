/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static java.util.stream.Collectors.toList;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsDataProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;

class KeywordsDataProvider extends CodeElementsDataProvider<RobotKeywordsSection> {

    KeywordsDataProvider(final KeywordsColumnsPropertyAccessor propertyAccessor, final RobotKeywordsSection section) {
        super(section, propertyAccessor, KeywordsAdderState.KEYWORD, KeywordsAdderState.CALL);
    }

    @Override
    protected boolean shouldAddSetting(final RobotDefinitionSetting setting) {
        if (setting.isArguments()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        final AModelElement<UserKeyword> linkedSetting = (AModelElement<UserKeyword>) setting.getLinkedElement();
        final UserKeyword userKeyword = linkedSetting.getParent();
        return !userKeyword.isDuplicatedSetting(linkedSetting);
    }

    @Override
    protected int numberOfColumns(final Object element) {
        if (element instanceof RobotKeywordDefinition) {
            final RobotDefinitionSetting argumentsSetting = ((RobotKeywordDefinition) element).getArgumentsSetting();
            if (argumentsSetting != null) {
                final LocalSetting<?> arguments = (LocalSetting<?>) argumentsSetting.getLinkedElement();
                // add 2 for keyword definition and empty cell
                return arguments.tokensOf(RobotTokenType.KEYWORD_SETTING_ARGUMENT).collect(toList()).size() + 2;
            }

        } else if (element instanceof RobotKeywordCall && !(element instanceof RobotDefinitionSetting
                && ((RobotDefinitionSetting) element).isDocumentation())) {
            // add 1 for empty cell
            return ExecutablesRowHolderCommentService.execRowView((RobotKeywordCall) element).size() + 1;
        }
        return 0;
    }
}