package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.model.table.setting.AbstractImportable;
import org.robotframework.ide.core.testData.model.table.setting.LibraryReference;
import org.robotframework.ide.core.testData.model.table.setting.ResourceFileReference;
import org.robotframework.ide.core.testData.model.table.setting.VariablesFileReference;
import org.robotframework.ide.core.testData.model.util.MovableLinkedListWrapper;


/**
 * Mapping for Setting table used to import test libraries, resource files and
 * variable files and to define metadata for test suites and test cases. It can
 * included in test case files and resource files. Note that in resource file, a
 * Setting table can only include settings for importing libraries, resources,
 * and variables.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class SettingTable implements IRobotSectionTable {

    private boolean declarationOfTableAppears = false;

    /**
     * container for libraries, resources and variables, they can appear in any
     * order - means - that it could be library, then variables file and again
     * library
     */
    private final MovableLinkedListWrapper<AbstractImportable> importedArtifacts = new MovableLinkedListWrapper<AbstractImportable>();


    /**
     * @return all imported as external libraries, resources and variables files
     */
    public MovableLinkedListWrapper<AbstractImportable> getImportedArtifacts() {
        return importedArtifacts;
    }


    /**
     * @param library
     * @return an information if adding was successful
     */
    public boolean addLibrary(LibraryReference library) {
        return importedArtifacts.add(library);
    }


    /**
     * @param resourceFile
     * @return an information if adding was successful
     */
    public boolean addResource(ResourceFileReference resourceFile) {
        return importedArtifacts.add(resourceFile);
    }


    /**
     * 
     * @param variablesFile
     * @return an information if adding was successful
     */
    public boolean addVariablesFile(VariablesFileReference variablesFile) {
        return importedArtifacts.add(variablesFile);
    }


    public void unsetPresent() {
        this.declarationOfTableAppears = false;
    }


    public void setPresent() {
        this.declarationOfTableAppears = true;
    }


    @Override
    public boolean isPresent() {
        return this.declarationOfTableAppears;
    }


    @Override
    public String getTableName() {
        return "Settings";
    }
}
