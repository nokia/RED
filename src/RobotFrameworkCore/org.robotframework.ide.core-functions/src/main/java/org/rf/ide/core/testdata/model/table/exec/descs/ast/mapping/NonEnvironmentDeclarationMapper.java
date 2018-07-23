/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.List;

import org.rf.ide.core.testdata.model.table.exec.descs.ast.ContainerElementType;

/**
 * @author lwlodarc
 *
 */
public class NonEnvironmentDeclarationMapper extends DeclarationMapper {

    @Override
    IElementDeclaration getPossibleVariableIdentifier(List<IElementDeclaration> mappedElements) {
        if (mappedElements != null) {
            final int numberOfMapped = mappedElements.size();
            if (numberOfMapped >= 2 && mappedElements.get(numberOfMapped - 1) instanceof VariableDeclaration) {
                final IElementDeclaration previous = mappedElements.get(numberOfMapped - 2);
                final String idText = previous.getStart().getText().trim();
                if (idText.length() >= 1 && idText.charAt(0) != '%' && ContainerElementType.VARIABLE_TYPE_ID
                        .getRepresentation().contains(idText.charAt(0))) {
                    return previous;
                }
            }
        }
        return null;
    }
}
