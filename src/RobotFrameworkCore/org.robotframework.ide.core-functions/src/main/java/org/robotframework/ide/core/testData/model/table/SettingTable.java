package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.model.table.setting.AbstractImportable;
import org.robotframework.ide.core.testData.model.util.MovableLinkedListWrapper;


/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class SettingTable {

    private final MovableLinkedListWrapper<AbstractImportable> importedArtifacts = new MovableLinkedListWrapper<AbstractImportable>();


    public MovableLinkedListWrapper<AbstractImportable> getImportedArtifacts() {
        return importedArtifacts;
    }


    public void addLibrary() {
        // importedArtifacts.add(e)
    }


    public void addResource() {
        // importedArtifacts.add(e)
    }


    public void addVariablesFile() {
        // importedArtifacts.add(e)
    }
}
