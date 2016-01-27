/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;


/**
 * @author Michal Anglart
 *
 */
public class RunTestInDebugDynamicMenuItem extends RunTestDynamicMenuItem {

    public RunTestInDebugDynamicMenuItem() {
        super("org.robotframework.red.menu.dynamic.source.debug");
    }

    @Override
    protected String getModeName() {
        return "DEBUG";
    }
}
