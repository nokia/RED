package org.robotframework.ide.core.testData.text.section;

import java.util.Comparator;
import java.util.Map;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;


public class ModelElementComparator implements Comparator<AModelElement> {

    private final IModelElementsPriority priorityProvider;
    private final static int LESS_THAN = -1;
    private final static int EQUAL = 0;
    private final static int GREATER_THAN = 1;


    public ModelElementComparator(final IModelElementsPriority priorityProvider) {
        this.priorityProvider = priorityProvider;
    }


    @Override
    public int compare(AModelElement o1, AModelElement o2) {
        int result = EQUAL;

        ModelType modelTypeO1 = o1.getModelType();
        int positionO1 = o1.getPosition().getOffset();
        ModelType modelTypeO2 = o2.getModelType();
        int positionO2 = o2.getPosition().getOffset();

        if (positionO1 != IRobotLineElement.NOT_SET
                && positionO2 != IRobotLineElement.NOT_SET) {
            result = Integer.compare(positionO1, positionO2);
        } else if (positionO1 != IRobotLineElement.NOT_SET) {
            result = LESS_THAN;
        } else if (positionO2 != IRobotLineElement.NOT_SET) {
            result = GREATER_THAN;
        } else {
            Map<ModelType, Integer> priorities = priorityProvider
                    .getElementPriorities();
            result = Integer.compare(priorities.get(modelTypeO1),
                    priorities.get(modelTypeO2));
        }

        return result;
    }
}
