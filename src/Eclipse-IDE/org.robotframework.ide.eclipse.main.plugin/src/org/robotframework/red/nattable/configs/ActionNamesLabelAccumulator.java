/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

/**
 * @author lwlodarc
 *
 */
public class ActionNamesLabelAccumulator implements IConfigLabelAccumulator {

    public static final String ACTION_NAME_CONFIG_LABEL = "ACTION_NAME";

    private final IRowDataProvider<Object> dataProvider;

    public ActionNamesLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {

        final Object rowObject = dataProvider.getRowObject(rowPosition);
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final AModelElement<?> linked = call.getLinkedElement();
            final List<RobotToken> tokens = linked.getElementTokens();
            if (tokens.size() > columnPosition) {
                final ModelType modelType = linked.getModelType();
                if (modelType == ModelType.TEST_CASE_EXECUTABLE_ROW
                        || modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                    final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linked;
                    final RobotToken action = row.buildLineDescription().getAction().getToken();
                    // don't worry about comments with artificial action token, it will be served by
                    // another case
                    final RobotToken actual = tokens.get(columnPosition);
                    if (actual.getText().equals(action.getText())
                            && actual.getStartOffset() == action.getStartOffset()) {
                        configLabels.addLabel(ACTION_NAME_CONFIG_LABEL);

                    } else {
                        for (int i = columnPosition - 1; i >= 0; i--) {
                            final QualifiedKeywordName qualifiedKwName = QualifiedKeywordName
                                    .fromOccurrence(tokens.get(i).getText());
                            if (SpecialKeywords.isNestingKeyword(qualifiedKwName) && SpecialKeywords
                                    .isKeywordNestedInKeyword(qualifiedKwName, i, columnPosition - i, tokens)) {
                                configLabels.addLabel(ACTION_NAME_CONFIG_LABEL);
                                break;
                            }
                        }
                    }
                } else if (call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isKeywordBased()) {
                    final List<IRobotTokenType> types = tokens.get(columnPosition).getTypes();
                    if (types.contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME)) {
                        configLabels.addLabel(ACTION_NAME_CONFIG_LABEL);
                        
                    } else if (types.contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT)
                            || types.contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT)) {
                        final AKeywordBaseSetting<?> kwBasedSetting = (AKeywordBaseSetting<?>) linked;

                        final List<RobotToken> allTokens = new ArrayList<>();
                        allTokens.add(kwBasedSetting.getKeywordName());
                        allTokens.addAll(kwBasedSetting.getArguments());

                        if (columnPosition - 1 < allTokens.size()) {
                            for (int i = columnPosition - 2; i >= 0; i--) {
                                final QualifiedKeywordName qualifiedKwName = QualifiedKeywordName
                                        .fromOccurrence(allTokens.get(i).getText());
                                if (SpecialKeywords.isNestingKeyword(qualifiedKwName)
                                        && SpecialKeywords.isKeywordNestedInKeyword(qualifiedKwName, i,
                                                columnPosition - i - 1, allTokens)) {
                                    configLabels.addLabel(ACTION_NAME_CONFIG_LABEL);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
