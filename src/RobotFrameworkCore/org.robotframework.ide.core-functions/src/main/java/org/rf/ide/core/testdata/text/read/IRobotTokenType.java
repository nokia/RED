/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.List;

public interface IRobotTokenType {

    List<String> getRepresentation();

    List<VersionAvailabilityInfo> getVersionAvailabilityInfos();

    VersionAvailabilityInfo findVersionAvailabilityInfo(final String text);
}
