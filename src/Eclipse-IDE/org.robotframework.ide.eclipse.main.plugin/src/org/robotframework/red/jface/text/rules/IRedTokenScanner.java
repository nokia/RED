/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.jface.text.rules;

import org.eclipse.jface.text.rules.ITokenScanner;

public interface IRedTokenScanner extends ITokenScanner {

    void resetPosition();
}
