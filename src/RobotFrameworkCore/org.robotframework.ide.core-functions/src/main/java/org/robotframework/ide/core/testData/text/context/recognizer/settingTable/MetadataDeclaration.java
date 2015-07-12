package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Metadata
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class MetadataDeclaration extends ATableElementRecognizer {

    public MetadataDeclaration() {
        super(
                SettingTableRobotContextType.TABLE_SETTINGS_METADATA,
                createExpectedWithOptionalColonAsLast(RobotWordType.METADATA_WORD));
    }
}
