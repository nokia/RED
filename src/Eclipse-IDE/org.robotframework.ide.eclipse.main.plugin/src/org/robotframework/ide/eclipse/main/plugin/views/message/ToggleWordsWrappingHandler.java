/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.message.ToggleWordsWrappingHandler.E4ToggleWordsWrappingHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ToggleWordsWrappingHandler extends DIParameterizedHandler<E4ToggleWordsWrappingHandler> {

    public ToggleWordsWrappingHandler() {
        super(E4ToggleWordsWrappingHandler.class);
    }

    public static class E4ToggleWordsWrappingHandler {

        @Execute
        public void wrapWords(final @Named(ISources.ACTIVE_PART_NAME) MessageLogViewWrapper view) {
            @SuppressWarnings("restriction")
            final MessageLogView msgLogView = view.getComponent();
            msgLogView.toggleWordsWrapping();
        }
    }
}
