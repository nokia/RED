/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class MockRedPreferences extends RedPreferences {

    private boolean isKeywordPrefixAutoAdditionEnabled;

    private String separatorToUseInRobot;

    private String separatorToUseInTsv;

    private char[] assistantAutoActivationChars;

    MockRedPreferences(final boolean isKeywordPrefixAutoAdditionEnabled, final String separatorToUse) {
        this(isKeywordPrefixAutoAdditionEnabled, separatorToUse, separatorToUse, new char[0]);
    }

    MockRedPreferences(final boolean isKeywordPrefixAutoAdditionEnabled, final String separatorToUse,
            final char[] assistantAutoActivationChars) {
        this(isKeywordPrefixAutoAdditionEnabled, separatorToUse, separatorToUse, assistantAutoActivationChars);
    }

    MockRedPreferences(final boolean isKeywordPrefixAutoAdditionEnabled, final String separatorToUseInRobot,
            final String separatorToUseInTsv, final char[] assistantAutoActivationChars) {
        super(null);
        this.isKeywordPrefixAutoAdditionEnabled = isKeywordPrefixAutoAdditionEnabled;
        this.separatorToUseInRobot = separatorToUseInRobot;
        this.separatorToUseInTsv = separatorToUseInTsv;
        this.assistantAutoActivationChars = assistantAutoActivationChars;
    }

    @Override
    public boolean isAssistantKeywordPrefixAutoAdditionEnabled() {
        return isKeywordPrefixAutoAdditionEnabled;
    }

    void setAssistantKeywordPrefixAutoAdditionEnabled(final boolean enabled) {
        this.isKeywordPrefixAutoAdditionEnabled = enabled;
    }

    @Override
    public String getSeparatorToUse(final boolean isTsvFile) {
        return isTsvFile ? separatorToUseInTsv : separatorToUseInRobot;
    }

    void setSeparatorToUseInRobot(final String separator) {
        this.separatorToUseInRobot = separator;
    }

    void setSeparatorToUseInTsv(final String separator) {
        this.separatorToUseInTsv = separator;
    }

    @Override
    public char[] getAssistantAutoActivationChars() {
        return assistantAutoActivationChars;
    }

    void setAssistatntAutoActivationChars(final char[] assistantAutoActivationChars) {
        this.assistantAutoActivationChars = assistantAutoActivationChars;
    }
}
