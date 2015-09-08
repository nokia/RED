package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;


public interface IRobotProjectHolderListenable {

    void addModelFile(final IRobotFileOutput robotOutput);


    void removeModelFile(final IRobotFileOutput robotOutput);


    void addImportedResource(final ResourceImportReference referenced);
}
