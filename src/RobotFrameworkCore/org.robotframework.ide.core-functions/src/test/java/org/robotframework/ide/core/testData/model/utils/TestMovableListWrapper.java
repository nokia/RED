package org.robotframework.ide.core.testData.model.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.robotframework.ide.core.testData.model.utils.movableListWrapper.TestMoveDownMethod;
import org.robotframework.ide.core.testData.model.utils.movableListWrapper.TestMoveUpMethod;

@RunWith(Suite.class)
@SuiteClasses({ TestMoveUpMethod.class, TestMoveDownMethod.class })
public class TestMovableListWrapper {

}
