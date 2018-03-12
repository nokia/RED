/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.views.message;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.message.ClearLogMessageLogHandler.E4ClearLogMessageLogHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ClearLogMessageLogHandler extends DIParameterizedHandler<E4ClearLogMessageLogHandler> {

    public ClearLogMessageLogHandler() {
        super(E4ClearLogMessageLogHandler.class);
    }

    public static class E4ClearLogMessageLogHandler {

        @Execute
        public void clear(final @Named(ISources.ACTIVE_PART_NAME) MessageLogViewWrapper view) {
            @SuppressWarnings("restriction")
            final MessageLogView msgLogView = view.getComponent();

            msgLogView.getCurrentlyShownLaunch()
                    .flatMap(launch -> launch.getExecutionData(ExecutionMessagesStore.class))
                    .ifPresent(store -> {
                        store.clear();
                        msgLogView.clearView();
                    });
        }
    }

}
