/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;

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
        this(modelSupplier, new AssistPreferences());
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

    public AcceptanceMode getAcceptanceMode() {
        return assistPreferences.getAcceptanceMode();
    }
    
    public boolean isKeywordPrefixAutoAdditionEnabled() {
        return assistPreferences.isKeywordPrefixAutoAdditionEnabled();
    }

    public static class AssistPreferences {

        private AcceptanceMode acceptanceMode;
        private boolean isKeywordPrefixAutoAdditionEnabled;

        private String separatorToUseInTsv;

        private String separatorToUseInRobot;

        AssistPreferences() {
            this(RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode(),
                    RedPlugin.getDefault().getPreferences().isAssistantKeywordPrefixAutoAdditionEnabled(),
                    RedPlugin.getDefault().getPreferences().getSeparatorToUse(false),
                    RedPlugin.getDefault().getPreferences().getSeparatorToUse(true));
        }

        @VisibleForTesting
        AssistPreferences(final AcceptanceMode acceptanceMode, final boolean isKeywordPrefixAutoAdditionEnabled,
                final String separatorToUse) {
            this(acceptanceMode, isKeywordPrefixAutoAdditionEnabled, separatorToUse, separatorToUse);
        }

        @VisibleForTesting
        AssistPreferences(final AcceptanceMode acceptanceMode, final boolean isKeywordPrefixAutoAdditionEnabled,
                final String separatorToUseInRobot, final String separatorToUseInTsv) {
            this.acceptanceMode = acceptanceMode;
            this.isKeywordPrefixAutoAdditionEnabled = isKeywordPrefixAutoAdditionEnabled;
            this.separatorToUseInRobot = separatorToUseInRobot;
            this.separatorToUseInTsv = separatorToUseInTsv;
        }

        void refresh() {
            acceptanceMode = RedPlugin.getDefault().getPreferences().getAssistantAcceptanceMode();
            isKeywordPrefixAutoAdditionEnabled = RedPlugin.getDefault()
                    .getPreferences()
                    .isAssistantKeywordPrefixAutoAdditionEnabled();
            separatorToUseInRobot = RedPlugin.getDefault().getPreferences().getSeparatorToUse(false);
            separatorToUseInTsv = RedPlugin.getDefault().getPreferences().getSeparatorToUse(true);
        }

        public String getSeparatorToFollow(final boolean isTsvFile) {
            return isTsvFile ? separatorToUseInTsv : separatorToUseInRobot;
        }

        public AcceptanceMode getAcceptanceMode() {
            return acceptanceMode;
        }

        public boolean isKeywordPrefixAutoAdditionEnabled() {
            return isKeywordPrefixAutoAdditionEnabled;
        }
    }
}
