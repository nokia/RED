package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class MetadataDeclaration extends ATableElementRecognizer {

    public MetadataDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_METADATA,
                createExpectedWithOptionalColonAsLast(RobotWordType.METADATA_WORD));
    }
}
