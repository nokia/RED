package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.listener.ITablesGetter;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingDocumentationCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_DOCUMENTATION);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            ITablesGetter fileModel) {
        List<SuiteDocumentation> documentations = fileModel.getSettingTable()
                .getDocumentation();
        if (!documentations.isEmpty()) {
            SuiteDocumentation suiteDoc = documentations.get(documentations
                    .size() - 1);
            suiteDoc.addCommentPart(rt);
        } else {
            // FIXME: errors
        }
    }

}
