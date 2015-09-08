package org.robotframework.ide.core.testData.model;

import java.io.File;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.model.RobotFileOutput.Status;
import org.robotframework.ide.core.testData.model.listener.IRobotFileOutputListenable;
import org.robotframework.ide.core.testData.model.objectCreator.IRobotModelObjectCreator;


public interface IRobotFileOutput extends IRobotFileOutputListenable {

    IRobotFile getFileModel();


    IRobotModelObjectCreator getObjectCreator();


    File getProcessedFile();


    long getLastModificationEpochTime();


    Status getStatus();


    void addResourceReferences(List<ResourceImportReference> importedReferences);


    void addVariablesReferenced(List<VariablesFileImportReference> varsImported);
}
