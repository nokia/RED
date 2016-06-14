/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

public class SettingsMatchesCollection extends HeaderFilterMatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotKeywordCall) {
            collectMatches((RobotKeywordCall) element, filter);
        } else if (element instanceof RobotSettingsSection) {
            for (final RobotKeywordCall setting : ((RobotSettingsSection) element).getChildren()) {
                collectMatches(setting, filter);
            }
        }
    }

    private void collectMatches(final RobotKeywordCall setting, final String filter) {
        boolean isMatching = false; 

        isMatching |= collectMatches(filter, setting.getName());
        
        if (isDocumentationSetting(setting)) {
            isMatching |= collectMatches(filter,
                    DocumentationServiceHandler.toShowConsolidated((IDocumentationHolder) setting.getLinkedElement()));
        } else {
            for (final String argument : setting.getArguments()) {
                isMatching |= collectMatches(filter, argument);
            }
        }
        isMatching |= collectMatches(filter, setting.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }
    
    private boolean isDocumentationSetting(final RobotKeywordCall setting) {
        return ((RobotSetting) setting).getGroup() == SettingsGroup.NO_GROUP
                && setting.getLinkedElement() instanceof SuiteDocumentation;
    }
}
