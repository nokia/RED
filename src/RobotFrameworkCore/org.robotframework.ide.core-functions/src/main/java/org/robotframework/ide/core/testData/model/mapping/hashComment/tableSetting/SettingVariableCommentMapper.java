package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingVariableCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_VARIABLE_IMPORT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<AImported> imports = fileModel.getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof VariablesImport) {
                VariablesImport vars = (VariablesImport) aImported;
                vars.addCommentPart(rt);
            } else {
                // FIXME: error
            }
        } else {
            // FIXME: errors
        }
    }

}
