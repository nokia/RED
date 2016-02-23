/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.osgi.util.NLS;


/**
 * @author Michal Anglart
 *
 */
public class PythonTemplates extends NLS {

    static {
        NLS.initializeMessages(PythonTemplates.class.getName(), PythonTemplates.class);
    }

    public static String variables;
    public static String variablesWithClass;
    public static String library;
    public static String dynamicLibrary;
}
