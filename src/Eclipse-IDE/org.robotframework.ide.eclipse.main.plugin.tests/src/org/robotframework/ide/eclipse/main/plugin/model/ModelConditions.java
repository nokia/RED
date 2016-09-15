package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.all;

import java.util.List;

import org.assertj.core.api.Condition;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Predicate;

public class ModelConditions {

    public static Condition<RobotFileInternalElement> filePositions() {
        return new Condition<RobotFileInternalElement>("all token positions set") {

            @Override
            public boolean matches(final RobotFileInternalElement element) {
                final List<RobotToken> tokens = ((AModelElement<?>) element.getLinkedElement()).getElementTokens();
                return all(tokens, not(havePositionsSet()));
            }
        };
    }

    public static Condition<RobotFileInternalElement> noFilePositions() {
        return new Condition<RobotFileInternalElement>("no token positions set") {

            @Override
            public boolean matches(final RobotFileInternalElement element) {
                final List<RobotToken> tokens = ((AModelElement<?>) element.getLinkedElement()).getElementTokens();
                return all(tokens, havePositionsSet());
            }
        };
    }

    private static Predicate<RobotToken> havePositionsSet() {
        return new Predicate<RobotToken>() {

            @Override
            public boolean apply(final RobotToken token) {
                final FilePosition tokenPosition = token.getFilePosition();
                return tokenPosition.isNotSet();
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

    public static Condition<RobotElement> name(final String name) {
        return new Condition<RobotElement>("name set to " + name) {

            @Override
            public boolean matches(final RobotElement element) {
                return element.getName().equals(name);
            }
        };
    }

    public static Condition<RobotElement> childrens() {
        return new Condition<RobotElement>("non empty children list") {

            @Override
            public boolean matches(final RobotElement element) {
                return !element.getChildren().isEmpty();
            }
        };
    }
}
