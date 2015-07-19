package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;


public class TableHeader extends AModelElement {

    public TableHeader(final ElementType type, RobotLine containingLine,
            LineElement originalElement) {
        super(type, containingLine, originalElement);
        if (!isCorrectType(type)) {
            throw new IllegalArgumentException(String.format(
                    "Type %s is not acceptable table header type.",
                    ((type != null) ? type.getClass() : "null")));
        }
    }


    private boolean isCorrectType(final ElementType type) {
        return (type == ElementType.SETTING_TABLE_HEADER
                || type == ElementType.VARIABLE_TABLE_HEADER
                || type == ElementType.TEST_CASE_TABLE_HEADER || type == ElementType.KEYWORD_TABLE_HEADER);
    }


    @Override
    public String toString() {
        return String
                .format("TableHeader [getContainingLine()=%s, getOriginalElement()=%s, getType()=%s]",
                        getContainingLine(), getOriginalElement(), getType());
    }

}
