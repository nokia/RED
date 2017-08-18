package org.rf.ide.core.execution.server.response;

import org.codehaus.jackson.map.ObjectMapper;

interface ResponseObjectsMapper {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
