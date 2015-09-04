package org.robotframework.ide.core.testData.model.listener;

import java.io.File;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.RobotFileOutput.Status;


public interface IRobotFileOutputListenable {

    void setProcessedFile(final File processedFile);


    void setLastModificationEpochTime(final long lastModificationEpoch);


    void addBuildMessage(final BuildMessage msg);


    void addResourceReference(final ResourceImportReference ref);


    void addVariablesReference(final VariablesFileImportReference varImportRef);


    void setStatus(final Status status);

}
