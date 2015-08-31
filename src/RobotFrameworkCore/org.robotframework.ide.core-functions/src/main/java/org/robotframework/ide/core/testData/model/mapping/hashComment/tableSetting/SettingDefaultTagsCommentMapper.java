package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.listener.ITablesGetter;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingDefaultTagsCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_DEFAULT_TAGS || state == ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            ITablesGetter fileModel) {
        List<DefaultTags> suiteDefaultTags = fileModel.getSettingTable()
                .getDefaultTags();
        if (!suiteDefaultTags.isEmpty()) {
            DefaultTags defaultTags = suiteDefaultTags.get(suiteDefaultTags
                    .size() - 1);
            defaultTags.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }

    }

}
