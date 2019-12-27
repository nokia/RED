/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolVersionTest {

    @Test
    public void properMessageIsConstructed_forProtocolVersionMessage() {
        assertThat(new ProtocolVersion(null).toMessage())
                .isEqualTo("{\"protocol_version\":{\"error\":\"\",\"is_correct\":true}}");
        assertThat(new ProtocolVersion("oops").toMessage())
                .isEqualTo("{\"protocol_version\":{\"error\":\"oops\",\"is_correct\":false}}");
    }

    @Test
    public void mapperJsonProcessingExceptionIsWrappedAsResponseException() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);

        final ProtocolVersion response = new ProtocolVersion(mapper, null);

        assertThatExceptionOfType(ResponseException.class).isThrownBy(response::toMessage);
    }
}
