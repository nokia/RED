/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;


class StringUtils {

    static String toLowerCase(final String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase().replaceAll(" ", "").replaceAll("_", "");
    }

}
