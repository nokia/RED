/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.VariableStructureExtractor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.DeclarationMapper;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.MappingResult;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class VariableExtractor {

    private final VariableStructureExtractor structureExtractor;
    private final DeclarationMapper mapper;


    public VariableExtractor() {
        this.structureExtractor = new VariableStructureExtractor();
        this.mapper = new DeclarationMapper();
    }


    public MappingResult extract(final FilePosition fp, final String text,
            final String fileName) {
        Container mainContainer = structureExtractor.buildStructureTree(text);

        String extractionInsideFile = fileName;
        if (fileName == null) {
            extractionInsideFile = "<NOT_SET>";
        }

        return mapper.map(fp, mainContainer, extractionInsideFile);
    }


    public MappingResult extract(final RobotToken token, final String fileName) {
        return extract(token.getFilePosition(), token.getRaw().toString(),
                fileName);
    }
}
