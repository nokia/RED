package org.robotframework.ide.core.testData.text.context.mapper;

import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;


public class MapperOutput {

    private ElementType etLast;
    private FilePosition fp;


    public ElementType getMappedElementType() {
        return etLast;
    }


    public void setMappedElementType(ElementType etLast) {
        this.etLast = etLast;
    }


    public FilePosition getNextPosition() {
        return fp;
    }


    public void setNextPosition(FilePosition fp) {
        this.fp = fp;
    }

}