/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.osgi.util.NLS;


/**
 * @author Michal Anglart
 *
 */
public class SyntaxHighlightingPreferencePageSource extends NLS {

    static {
        NLS.initializeMessages(SyntaxHighlightingPreferencePageSource.class.getName(),
                SyntaxHighlightingPreferencePageSource.class);
    }

    public static String source;

    public static String sectionHeaderStartIndexes;

    public static String sectionHeaderRangeLengths;

    public static String settingStartIndexes;

    public static String settingRangeLengths;

    public static String definitionStartIndexes;

    public static String definitionRangeLengths;

    public static String variableStartIndexes;

    public static String variableRangeLengths;

    public static String keywordCallStartIndexes;

    public static String keywordCallRangeLengths;

    public static String keywordCallQuoteStartIndexes;

    public static String keywordCallQuoteRangeLengths;

    public static String commentStartIndexes;

    public static String commentRangeLengths;

    public static String gherkinStartIndexes;

    public static String gherkinRangeLengths;

    public static String taskStartIndexes;

    public static String taskRangeLengths;

    public static String specialStartIndexes;

    public static String specialRangeLengths;
}
