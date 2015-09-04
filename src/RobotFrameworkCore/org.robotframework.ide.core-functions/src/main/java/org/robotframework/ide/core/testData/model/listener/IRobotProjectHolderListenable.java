package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.RobotFileOutput;


public interface IRobotProjectHolderListenable {

    void addModelFile(final RobotFileOutput robotOutput);


    void removeModelFile(final RobotFileOutput robotOutput);


    void addImportedResource(final ResourceImportReference referenced);
}
