/**
 * Class for setting the application's default font and size, allowing application
 * wide changes to be made from a single place.
 */

package semgen.utilities;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class SemGenFont {
	private static String defaultFont = "SansSerif";
	private static int defaultfontsize = 12;
	
	public static Font defaultPlain() {
		return new Font(defaultFont,Font.PLAIN, defaultfontsize);
	}
	
	public static Font defaultPlain(int sizeDiff) {
		return new Font(defaultFont,Font.PLAIN, defaultfontsize+sizeDiff);
	}
	
	public static Font defaultBold() {
		return new Font(defaultFont,Font.BOLD, defaultfontsize);
	}
	
	public static Font defaultBold(int sizeDiff) {
		return new Font(defaultFont,Font.BOLD, defaultfontsize+sizeDiff);
	}
	
	public static Font defaultItalic() {
		return new Font(defaultFont,Font.ITALIC, defaultfontsize);
	}
	
	public static Font defaultItalic(int sizeDiff) {
		return new Font(defaultFont,Font.ITALIC, defaultfontsize+sizeDiff);
	}
	
	public static Font Plain(String font) {
		return new Font(font,Font.PLAIN, defaultfontsize);
	}
	
	public static Font Plain(String font, int sizeDiff) {
		return new Font(font,Font.PLAIN, defaultfontsize+sizeDiff);
	}
	
	public static Font Bold(String font) {
		return new Font(font,Font.BOLD, defaultfontsize);
	}
	
	public static Font Bold(String font, int sizeDiff) {
		return new Font(font,Font.BOLD, defaultfontsize+sizeDiff);
	}
	
	public static Font Italic(String font) {
		return new Font(font,Font.BOLD, defaultfontsize);
	}
	
	public static Font Italic(String font, int sizeDiff) {
		return new Font(font,Font.BOLD, defaultfontsize+sizeDiff);
	}
	
	public static void defaultUIFont() {
		setUIFont(new javax.swing.plaf.FontUIResource(defaultFont,
				Font.PLAIN, defaultfontsize));
	}
	
	public static void setUIFont(FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			if (UIManager.get(key) instanceof FontUIResource) UIManager.put(key, f);}
	}
}
