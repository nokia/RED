/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElement;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.IContainerElement;


public class VariableStructureExtractor {

    public Container buildStructureTree(final String text) {
        Container mainContainer = new Container(null);

        if (text != null) {
            Container currentContainer = mainContainer;

            char[] textChars = text.toCharArray();
            int textLength = textChars.length;
            for (int charIndex = 0; charIndex < textLength; charIndex++) {
                char currentChar = textChars[charIndex];
                ContainerElementType type = ContainerElementType
                        .getTypeFor(currentChar);
                ContainerElement element = new ContainerElement(
                        new TextPosition(text, charIndex, charIndex), type);
                if (type.shouldOpenNewContainer()) {
                    Container newContainer = new Container(currentContainer);
                    currentContainer.addElement(newContainer);
                    currentContainer = newContainer;
                    currentContainer.addElement(element);
                } else if (shouldCloseContainer(type)) {
                    Container matchingContainer = findNearestContainerToClose(
                            type, currentContainer);
                    if (matchingContainer == null) {
                        currentContainer.addElement(element);
                    } else {
                        matchingContainer.addElement(element);
                        closeContainer(matchingContainer);
                        currentContainer = matchingContainer.getParent();
                    }
                } else {
                    boolean wasMerged = false;
                    List<IContainerElement> elements = currentContainer
                            .getElements();
                    if (!elements.isEmpty()) {
                        IContainerElement lastElement = elements.get(elements
                                .size() - 1);
                        ContainerElementType lastType = lastElement.getType();
                        if (lastType != null && lastType.canBeMerged()
                                && type == lastType) {
                            if (lastElement instanceof ContainerElement) {
                                ContainerElement contElement = (ContainerElement) lastElement;
                                contElement.increaseEndPosition();
                                wasMerged = true;
                            }
                        }
                    }

                    if (!wasMerged) {
                        currentContainer.addElement(element);
                    }
                }
            }
        }

        return mainContainer;
    }


    private void closeContainer(final Container container) {
        if (container != null) {
            for (IContainerElement contElem : container.getElements()) {
                if (contElem.isComplex()) {
                    Container cont = (Container) contElem;
                    if (cont.isOpenForModification()) {
                        closeContainer(cont);
                    }
                }
            }
            container.closeForModification();
        }
    }


    private Container findNearestContainerToClose(
            final ContainerElementType type, final Container currentContainer) {
        Container cont = null;

        if (currentContainer != null) {
            if (currentContainer.isOpenForModification()) {
                for (IContainerElement contElement : currentContainer
                        .getElements()) {
                    if (contElement.isComplex()) {
                        cont = findNearestContainerToClose(type,
                                (Container) contElement);
                        if (cont != null) {
                            break;
                        }
                    }
                }

                if (cont == null) {
                    if (type == ContainerElementType
                            .getCloseContainerType(currentContainer
                                    .getElements().get(0).getType())) {
                        cont = currentContainer;
                    }
                }
            }
        }

        return cont;
    }


    private boolean shouldCloseContainer(ContainerElementType type) {
        return type.getCloseContainer() != null;
    }
}
