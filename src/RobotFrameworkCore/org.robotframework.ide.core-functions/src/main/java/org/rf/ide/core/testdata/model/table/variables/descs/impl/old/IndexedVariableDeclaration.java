/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.ArrayList;
import java.util.List;

class IndexedVariableDeclaration extends VariableDeclaration {

    static void merge(final List<IElementDeclaration> declarations) {
        for (int i = 0; i < declarations.size(); i++) {
            final IElementDeclaration declaration = declarations.get(i);
            if (declaration.isComplex()) {
                merge(declaration.getElementsDeclarationInside());
            }
            if (declaration instanceof VariableDeclaration && i < declarations.size() - 1
                    && declarations.get(i + 1) instanceof IndexDeclaration) {

                final TextPosition start = declaration.getStart();
                int j = i + 1;
                for (; j < declarations.size(); j++) {
                    if (declarations.get(j) instanceof IndexDeclaration) {
                        merge(declarations.get(j).getElementsDeclarationInside());
                    } else {
                        break;
                    }
                }
                final TextPosition end = ((IndexDeclaration) declarations.get(j - 1)).getEnd();

                final List<IElementDeclaration> elementsInside = new ArrayList<>();
                for (int k = 0; k < j - i; k++) {
                    elementsInside.addAll(declarations.remove(i).getElementsDeclarationInside());
                }

                final IndexedVariableDeclaration indexedVariableDeclaration = new IndexedVariableDeclaration(start,
                        end);
                indexedVariableDeclaration.setLevelUpElement(declaration.getLevelUpElement());
                indexedVariableDeclaration
                        .setTypeIdentificator(((VariableDeclaration) declaration).getTypeIdentificator());
                indexedVariableDeclaration
                        .setRobotTokenPosition(((VariableDeclaration) declaration).getRobotTokenPosition());
                elementsInside.forEach(e -> e.setLevelUpElement(indexedVariableDeclaration));
                elementsInside.forEach(indexedVariableDeclaration::addElementDeclarationInside);
                declarations.add(i, indexedVariableDeclaration);
            }
        }
    }

    private IndexedVariableDeclaration(final TextPosition variableStart, final TextPosition variableEnd) {
        super(variableStart, variableEnd);
    }

    @Override
    public boolean isIndexed() {
        return true;
    }
}
