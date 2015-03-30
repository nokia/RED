package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.table.SettingTable;


/**
 * Mapping used for taking test library into use.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see SettingTable
 */
public class LibraryReference extends AbstractImportable {

    /**
     * @param pathOrName
     *            path to library file or library name
     * @see AbstractImportable
     */
    public LibraryReference(String pathOrName) {
        super(pathOrName);
    }

}
