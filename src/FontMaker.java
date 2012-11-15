/*
 * FontMaker is a class with static methods that construct and return java.awt.Font instances
 * It provides makePlainFont and makeCustomFont which uses the ttf filepath from Risk.FONT_PATH
 * Each can be obtained with a font size or a font size and style.
 * Since these are static, FontMaker can be used from any file
 * 
 * Evan Radkoff
 */

import java.awt.Font;
import java.io.File;

public class FontMaker {
	public static Font makeCustomFont(int size) {
		Font font;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(Risk.FONT_PATH));
			font = font.deriveFont(Font.PLAIN, size);
		} catch (Exception e) {
			font = makePlainFont(size);
		}
		return font;
	}
	public static Font makePlainFont(int size) {
		return new Font("herp",Font.PLAIN, size);
	}

	public static Font makeCustomFont(int style, int size) {
		Font font;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, new File(Risk.FONT_PATH));
			font = font.deriveFont(style, size);
		} catch (Exception e) {
			font = makePlainFont(size);
		}
		return font;
	}
	public static Font makePlainFont(int style, int size) {
		return new Font("herp",style, size);
	}
}