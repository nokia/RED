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

    public static String SOURCE;
}
