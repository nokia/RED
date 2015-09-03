package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.model.table.variables.UnknownVariable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IVariableTableRobotModelObjectCreator {

    ScalarVariable createScalarVariable(final String name,
            final RobotToken declaration);


    ListVariable createListVariable(final String name,
            final RobotToken declaration);


    DictionaryVariable createDictionaryVariable(final String name,
            final RobotToken declaration);


    UnknownVariable createUnknownVariable(final String name,
            final RobotToken declaration);
}
