package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.wb.swt.SWTResourceManager;

public class SharedTextColors implements ISharedTextColors{

	@Override
	public Color getColor(RGB rgb) {
		return SWTResourceManager.getColor(rgb);
	}

	@Override
	public void dispose() {
		
	}

}
