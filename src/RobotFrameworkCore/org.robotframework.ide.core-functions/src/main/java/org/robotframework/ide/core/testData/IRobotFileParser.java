/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData;

import java.io.File;
import java.io.InputStream;

import org.robotframework.ide.core.testData.model.RobotFileOutput;


public interface IRobotFileParser {

    boolean canParseFile(final File file);


    IRobotFileParser newInstance();


    void parse(final RobotFileOutput output, final File robotFile);


    void parse(final RobotFileOutput output, final InputStream inputStream,
            final File robotFile);
}
