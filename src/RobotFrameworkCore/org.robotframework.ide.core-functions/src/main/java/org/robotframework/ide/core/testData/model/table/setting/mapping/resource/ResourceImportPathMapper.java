package org.robotframework.ide.core.testData.model.table.setting.mapping.resource;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ResourceImportPathMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public ResourceImportPathMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_RESOURCE_FILE_NAME);
        rt.setText(new StringBuilder(text));
        AImported imported = utility.getNearestImport(robotFileOutput);
        ResourceImport resource;
        if (imported instanceof ResourceImport) {
            resource = (ResourceImport) imported;
        } else {
            resource = null;

            // FIXME: sth wrong - declaration of variables not inside setting
            // and
            // was not catch by previous variables declaration logic
        }
        resource.setPathOrName(rt);

        processingState.push(ParsingState.SETTING_RESOURCE_IMPORT_PATH);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_RESOURCE_IMPORT);
        } else {
            result = false;
        }

        return result;
    }
}
