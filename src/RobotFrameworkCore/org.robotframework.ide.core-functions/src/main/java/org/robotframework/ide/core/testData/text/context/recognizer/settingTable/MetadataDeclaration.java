package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class MetadataDeclaration extends ASettingTableElementRecognizer {

    public MetadataDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_METADATA,
                createWithAllAsMandatory(RobotWordType.METADATA_WORD));
    }
}
