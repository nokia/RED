package org.robotframework.ide.core.testData.text.section;

import java.util.Comparator;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;


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
        ModelType modelTypeO2 = o2.getModelType();

        return result;
    }
}
