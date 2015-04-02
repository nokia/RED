package org.robotframework.ide.eclipse.main.plugin.tempmodel;

import java.util.Arrays;
import java.util.List;


public class Keyword {

    private final String name;
	private final String[] arguments;
    
	public Keyword(final String name, final String... arguments) {
        this.name = name;
		this.arguments = arguments;
    }

    public String getName() {
        return name;
    }
    
	public List<String> getArguments() {
		return Arrays.asList(this.arguments);
    }
}
