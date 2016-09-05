package org.rf.ide.core.testdata.model.presenter.update.keywords;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.IKeywordTableElementOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordUnknownSettings;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordUnknownModelOperation implements IKeywordTableElementOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_SETTING_UNKNOWN;
    }

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return elementType == RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION;
    }

    @Override
    public AModelElement<?> create(final UserKeyword userKeyword, final String settingName, final List<String> args,
            final String comment) {
        final KeywordUnknownSettings unknown = userKeyword.newUnknownSettings();
        unknown.getDeclaration().setText(settingName);
        for (final String arg : args) {
            unknown.addArgument(arg);
        }
        if (comment != null && !comment.isEmpty()) {
            unknown.setComment(comment);
        }
        return unknown;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        userKeyword.addUnknownSettings(0, (KeywordUnknownSettings) modelElement);
        return modelElement;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final KeywordUnknownSettings unknown = (KeywordUnknownSettings) modelElement;

        if (value != null) {
            unknown.addArgument(index, value);
        } else {
            unknown.removeElementToken(index);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(final UserKeyword userKeyword, final AModelElement<?> modelElement) {
        userKeyword.removeUnitSettings((AModelElement<UserKeyword>) modelElement);
    }
}
