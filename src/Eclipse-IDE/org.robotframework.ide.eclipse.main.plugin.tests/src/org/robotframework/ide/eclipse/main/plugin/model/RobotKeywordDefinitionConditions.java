/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model;

import org.assertj.core.api.Condition;

public class RobotKeywordDefinitionConditions {

    public static Condition<RobotKeywordDefinition> properlySetParent() {
        return new Condition<RobotKeywordDefinition>() {

            @Override
            public boolean matches(final RobotKeywordDefinition keyword) {
                return keyword.getParent() != null && keyword.getParent().getChildren().contains(keyword)
                        && keyword.getLinkedElement().getParent() != null
                        && keyword.getLinkedElement().getParent().getKeywords().contains(keyword.getLinkedElement())
                        && keyword.getParent().getLinkedElement() == keyword.getLinkedElement().getParent();
            }
        };
    }
}
