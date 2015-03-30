package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.model.table.setting.AbstractImportable;
import org.robotframework.ide.core.testData.model.util.MovableLinkedListWrapper;


public class SettingTable {

    private final MovableLinkedListWrapper<AbstractImportable> importedArtifacts = new MovableLinkedListWrapper<AbstractImportable>();


    public MovableLinkedListWrapper<AbstractImportable> getImportedArtifacts() {
        return importedArtifacts;
    }
}
