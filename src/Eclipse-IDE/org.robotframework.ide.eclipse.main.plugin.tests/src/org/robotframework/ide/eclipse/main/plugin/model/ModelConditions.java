package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Iterables.all;

import org.assertj.core.api.Condition;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Predicate;

public class ModelConditions {

    public static Condition<RobotFileInternalElement> filePositionsSet() {
        return new Condition<RobotFileInternalElement>("all token positions set") {

            @Override
            public boolean matches(final RobotFileInternalElement element) {
                return all(((AModelElement<?>) element.getLinkedElement()).getElementTokens(),
                        new Predicate<RobotToken>() {

                    @Override
                    public boolean apply(final RobotToken token) {
                        final FilePosition tokenPosition = token.getFilePosition();
                        return !tokenPosition.isNotSet();
                    }
                });
            }
        };
    }

    public static Condition<RobotFileInternalElement> nullParent() {
        return new Condition<RobotFileInternalElement>("no parent reference") {

            @Override
            public boolean matches(final RobotFileInternalElement element) {
                return element.getParent() == null
                        && ((AModelElement<?>) element.getLinkedElement()).getParent() == null;
            }
        };
    }
}
