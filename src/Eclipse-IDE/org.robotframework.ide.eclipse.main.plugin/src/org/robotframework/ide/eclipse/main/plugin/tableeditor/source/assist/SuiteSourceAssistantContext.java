/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceAssistantContext {

    private final Supplier<RobotSuiteFile> modelSupplier;

    private final AssistPreferences assistPreferences;

    public SuiteSourceAssistantContext(final Supplier<RobotSuiteFile> modelSupplier) {
        this(modelSupplier, new AssistPreferences(RedPlugin.getDefault().getPreferences()));
    }

    @VisibleForTesting
    public SuiteSourceAssistantContext(final Supplier<RobotSuiteFile> modelSupplier,
            final AssistPreferences assistPreferences) {
        this.modelSupplier = modelSupplier;
        this.assistPreferences = assistPreferences;
    }

    public void refreshPreferences() {
        assistPreferences.refresh();
    }

    public RobotSuiteFile getModel() {
        return modelSupplier.get();
    }

    public IFile getFile() {
        return getModel().getFile();
    }

    public boolean isTsvFile() {
        return getModel().isTsvFile();
    }

    public String getSeparatorToFollow() {
        return assistPreferences.getSeparatorToFollow(isTsvFile());
    }

    public boolean isKeywordPrefixAutoAdditionEnabled() {
        return assistPreferences.isKeywordPrefixAutoAdditionEnabled();
    }

    static class AssistPreferences {

        // caches interesting preferences

        private final RedPreferences redPreferences;

        private boolean isKeywordPrefixAutoAdditionEnabled;
        private String separatorToUseInTsv;
        private String separatorToUseInRobot;

        AssistPreferences(final RedPreferences redPreferences) {
            this.redPreferences = redPreferences;
            refresh();
        }

        void refresh() {
            isKeywordPrefixAutoAdditionEnabled = redPreferences.isAssistantKeywordPrefixAutoAdditionEnabled();
            separatorToUseInRobot = redPreferences.getSeparatorToUse(false);
            separatorToUseInTsv = redPreferences.getSeparatorToUse(true);
        }

        public String getSeparatorToFollow(final boolean isTsvFile) {
            return isTsvFile ? separatorToUseInTsv : separatorToUseInRobot;
        }

        public boolean isKeywordPrefixAutoAdditionEnabled() {
            return isKeywordPrefixAutoAdditionEnabled;
        }
    }
}
