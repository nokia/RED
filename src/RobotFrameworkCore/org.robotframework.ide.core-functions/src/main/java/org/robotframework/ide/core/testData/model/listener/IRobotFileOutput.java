package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.objectCreator.IRobotModelObjectCreator;


public interface IRobotFileOutput extends IRobotFileOutputListenable {

    RobotFile getFileModel();


    IRobotModelObjectCreator getObjectCreator();
}
