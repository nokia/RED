package org.robotframework.ide.core.testData.text.context;

/**
 * This is group of multiple lines context (as so on tokens), which should be
 * take in consideration as whole.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public enum ComplexRobotContextType implements IContextElementType {
    /**
     * few lines of type {@link SimpleRobotContextType#UNDECLARED_COMMENT}
     */
    UNDECLARED_COMMENT,
    /**
     * few lines of type {@link SimpleRobotContextType#DECLARED_COMMENT}
     */
    DECLARED_COMMENT;
}
