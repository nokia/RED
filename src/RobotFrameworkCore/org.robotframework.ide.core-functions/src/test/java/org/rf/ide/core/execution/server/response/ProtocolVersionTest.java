/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ProtocolVersionTest {

    @Test
    public void properMessageIsConstructed_forProtocolVersionMessage() {
        assertThat(new ProtocolVersion(null).toMessage())
                .isEqualTo("{\"protocol_version\":{\"error\":\"\",\"is_correct\":true}}");
        assertThat(new ProtocolVersion("oops").toMessage())
                .isEqualTo("{\"protocol_version\":{\"error\":\"oops\",\"is_correct\":false}}");
    }
}
