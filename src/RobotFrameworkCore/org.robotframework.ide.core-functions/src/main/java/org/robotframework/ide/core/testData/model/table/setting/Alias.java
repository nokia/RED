package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.common.IOptional;


/**
 * Mapping for optional alias in imported library section in Settings Table.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see LibraryReference
 */
public class Alias implements IOptional {

    private String alias;


    /**
     * @return text after {@code WITH NAME}
     */
    public String getAlias() {
        return alias;
    }


    public void setAlias(String alias) {
        this.alias = alias;
    }


    /**
     * say if this field exists inside library tag
     */
    @Override
    public boolean isPresent() {
        return isNotNullAndNotEmpty(alias);
    }


    /**
     * @param text
     * @return
     */
    private boolean isNotNullAndNotEmpty(String text) {
        return text != null && !"".equals(text.trim());
    }
}
