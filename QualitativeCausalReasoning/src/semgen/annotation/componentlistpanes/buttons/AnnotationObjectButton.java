package semgen.annotation.componentlistpanes.buttons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;

public abstract class AnnotationObjectButton extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;

	protected ArrayList<JLabel> indicators = new ArrayList<JLabel>();
	protected boolean editable;
	protected JLabel namelabel = new JLabel();

	protected JLabel humdeflabel = new JLabel("_");
	
	protected Color editablelabelcolor = new Color(10, 50, 220);
	protected Color noneditablelabelcolor = Color.gray;
	
	protected JPanel indicatorspanel = new JPanel();
	protected JPanel indicatorssuperpanel = new JPanel(new BorderLayout());
	public static Border emptyborder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	public static Border outlineborder = BorderFactory.createLineBorder(SemGenSettings.lightblue, 2, true);
	
	public AnnotationObjectButton(String name, boolean canedit) {
		editable = canedit;
		setLayout(new BorderLayout(0, 0));
		
		this.setFocusable(true);
		this.setMaximumSize(new Dimension(999999, 35));
		setBackground(Color.white);
		setBorder(emptyborder);
		setOpaque(true);
		namelabel.setText(name);
		namelabel.setOpaque(false);
		namelabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 10));
		namelabel.setBackground(Color.black);
		
		setForeground(Color.black);
		if (!editable) namelabel.setForeground(Color.gray);
		setVisible(true);
	}
	
	public void drawButton() {
		makeIndicator(humdeflabel, "F", "Click to set free-text description");
		humdeflabel.addMouseListener(new ClickableMouseListener(humdeflabel));
		
		indicatorspanel.setOpaque(false);
		
		indicatorspanel.setLayout(new BoxLayout(indicatorspanel, BoxLayout.X_AXIS));
		indicatorspanel.setAlignmentY(TOP_ALIGNMENT);
		
		for (JLabel lbl : indicators) {
			indicatorspanel.add(lbl);
		}

		indicatorssuperpanel.setOpaque(false);
		indicatorssuperpanel.add(Box.createGlue(), BorderLayout.WEST);
		indicatorssuperpanel.add(indicatorspanel, BorderLayout.CENTER);

		add(Box.createGlue(), BorderLayout.EAST);
		add(namelabel, BorderLayout.CENTER);
		add(indicatorssuperpanel, BorderLayout.WEST);
	}
	
	public void makeIndicator(JLabel lbl, String name, String tooltip) {
		lbl.setName(name);
		lbl.setToolTipText(tooltip);
		lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		lbl.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		lbl.setFont(SemGenFont.Plain("Serif", -3));
		lbl.addMouseListener(this);
		if (editable) { 
			lbl.addMouseListener(new IndicatorMouseListener(lbl));
		}
		indicators.add(lbl);
	}
	
	public String getText() {
		return namelabel.getText();
	}
	
	public void toggleHumanDefinition(boolean hasdef) {
		toggleIndicator(humdeflabel, hasdef);
	}
		
	protected void toggleIndicator(JLabel lbl, boolean hasann) {
		lbl.setForeground(noneditablelabelcolor);
		if(editable) lbl.setForeground(editablelabelcolor);
		if (hasann) {
			lbl.setText(lbl.getName());
			lbl.setFont(SemGenFont.Bold("Serif", -2));
		}
		else {
			lbl.setText("_");
			lbl.setFont(SemGenFont.Plain("Serif", -2));
		}
		validate();
		repaint();
	}
	
	protected class IndicatorMouseListener extends MouseAdapter {
		private JLabel label;
		public IndicatorMouseListener(JLabel target){
			label = target;
		}
		
		public void mouseExited(MouseEvent e) {
				label.setOpaque(false);
				label.setBackground(null);
				label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	protected class ClickableMouseListener extends MouseAdapter {
		private JLabel label;
		public ClickableMouseListener(JLabel target){
			label = target;
		}
		
		public void mouseEntered(MouseEvent e) {
				label.setOpaque(true);
				label.setBackground(new Color(255,231,186));
				label.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
	}
	@Override
	public void mouseExited(MouseEvent e) {
		this.setBorder(emptyborder);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		this.setBorder(outlineborder);
	}
}
