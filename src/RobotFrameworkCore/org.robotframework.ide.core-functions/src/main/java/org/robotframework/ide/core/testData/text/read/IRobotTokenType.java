/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.util.List;


public interface IRobotTokenType {

    List<String> getRepresentation();


    List<DeprecatedInfo> getDeprecatedRepresentations();

    public class DeprecatedInfo {

        private final String representation;
        private final String info;


        public DeprecatedInfo(final String representation, final String info) {
            this.representation = representation;
            this.info = info;
        }


        public String getRepresentation() {
            return representation;
        }


        public String getInfo() {
            return info;
        }
    }
}
