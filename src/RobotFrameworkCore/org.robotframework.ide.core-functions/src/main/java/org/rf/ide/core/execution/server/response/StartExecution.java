/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;


public class StartExecution implements ServerResponse {

    @Override
    public String toMessage() {
        return "do_start";
    }

}
