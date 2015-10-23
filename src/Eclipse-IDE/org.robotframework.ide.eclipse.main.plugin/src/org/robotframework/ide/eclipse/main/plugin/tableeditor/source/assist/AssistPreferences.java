/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder.AcceptanceMode;

/**
 * @author Michal Anglart
 *
 */
public class AssistPreferences {

    public String getSeparatorToFollow() {
        return "    ";
    }

    public AcceptanceMode getAcceptanceMode() {
        return AcceptanceMode.SUBSTITUTE;
    }
}
