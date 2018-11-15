package semgen.annotation.common;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import semgen.utilities.SemGenFont;


public class AnnotationClickableTextPane extends JTextPane {
	private static final long serialVersionUID = -1862678829844737844L;

	public AnnotationClickableTextPane(String text, int indent, boolean clickable){
		setEditable(false);
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, indent, 7, 15));
		setBackground(new Color(0,0,0,0));

		if(clickable){  // If need mouse listener, then it's clickable, if not, customize for computational code field
			addMouseListener(new mouseforTextPane());
			setFont(SemGenFont.defaultPlain());
		}
		else{
			setContentType("text/html");
			setFont(SemGenFont.defaultItalic(-1));
		}
		setCustomText(text);

	}
	
	public void setCustomText(String text){
		setText(text);
	}
	
	class mouseforTextPane extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			// Paint underline on mouse entered
	        MutableAttributeSet attrs = getInputAttributes();
	        StyleConstants.setUnderline(attrs, true);
	        StyledDocument doc = getStyledDocument();
	        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);		
		}
		
		public void mouseExited(MouseEvent e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			// Remove underline on mouse exit
	        MutableAttributeSet attrs = getInputAttributes();
	        StyleConstants.setUnderline(attrs, false);
	        StyledDocument doc = getStyledDocument();
	        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);	
		}
	}
}
