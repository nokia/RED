/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule which handles creation of new shell, its opening, closing and disposing.
 * 
 * @author Michal Anglart
 */
public class ShellProvider implements TestRule {

    private Shell shell;

    public Shell getShell() {
        return shell;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    shell = new Shell(Display.getDefault());
                    shell.open();
                    base.evaluate();
                } finally {
                    if (shell != null && !shell.isDisposed()) {
                        shell.close();
                        shell.dispose();
                    }
                    shell = null;
                }
            }
        };
    }

}
