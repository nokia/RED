package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.List;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.listener.IRobotProjectHolderListenable;


public interface IRobotProjectHolder extends IRobotProjectHolderListenable {

    void addImportedResources(final List<ResourceImportReference> referenced);


    RobotRuntimeEnvironment getRobotRuntime();


    IRobotFileOutput findFileByName(final File file);


    boolean shouldBeLoaded(final File file);


    boolean shouldBeLoaded(final IRobotFileOutput robotOutput);

}
