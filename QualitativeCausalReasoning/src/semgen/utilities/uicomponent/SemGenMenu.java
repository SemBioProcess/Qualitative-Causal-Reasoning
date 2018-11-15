package semgen.utilities.uicomponent;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import semgen.GlobalActions;
import semgen.SemGenSettings;

public abstract class SemGenMenu extends JMenu implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	protected SemGenSettings settings;
	public static int maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	protected GlobalActions globalactions;
	
	public SemGenMenu(SemGenSettings sets) {
		settings = sets;
	}
	
	public SemGenMenu(SemGenSettings sets, GlobalActions gacts) {
		settings = sets;
		globalactions = gacts;
	}
	
	public SemGenMenu(String title, SemGenSettings sets) {
		super(title);
		settings = sets;
	}
	
	public SemGenMenu(String title, SemGenSettings sets, GlobalActions gacts) {
		super(title);
		settings = sets;
		globalactions = gacts;
	}
	
	// Format menu items, assign shortcuts, action listeners
	protected JMenuItem formatMenuItem(JMenuItem item, String text, Integer accelerator, Boolean enabled, Boolean addactionlistener){
		item = new JMenuItem(text);
		item.setEnabled(enabled);
		if(accelerator!=null){item.setAccelerator(KeyStroke.getKeyStroke(accelerator,maskkey));}
		item.addActionListener(this);
		return item;
	}
	
	@Override
	public abstract void actionPerformed(ActionEvent arg0);
	
}
