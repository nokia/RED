/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.server.response;

import com.fasterxml.jackson.databind.ObjectMapper;

interface ResponseObjectsMapper {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
