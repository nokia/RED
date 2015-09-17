/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.robotframework.ide.core.testData.RobotParser;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;


public class RobotModelTestProvider {
    public static RobotModelTestProvider getInstance() {
        return new RobotModelTestProvider();
    }
    
    public static RobotFile getModelFile(String filename) throws URISyntaxException {
        RobotProjectHolder robotProject = new RobotProjectHolder();
        RobotParser parser = new RobotParser(robotProject);
        parser.setEagerImport(true);
        
        Path p = Paths.get(getInstance().getClass()
                .getResource(filename)
                .toURI());
        
        List<RobotFileOutput> parse = parser.parse(p.toFile());
        RobotFileOutput robotFileOutput = parse.get(0);
        return robotFileOutput.getFileModel();        
    }
}
