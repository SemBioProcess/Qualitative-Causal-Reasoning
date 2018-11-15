package semgen.utilities.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import semgen.utilities.uicomponent.SemGenDialog;

public class JSimModelSelectorDialogForWriting extends SemGenDialog implements ActionListener, PropertyChangeListener{

	private static final long serialVersionUID = 5064278024814739053L;
	private String selectedModelName = new String();
	private JOptionPane optionPane;
	private JPanel panel = new JPanel();
	private JPanel inputpanel = new JPanel();
	private JRadioButton inputbutton = new JRadioButton("Save as new model");
	private JTextField inputbox = new JTextField();
	private Dimension dims = new Dimension(430, 250);
	private ArrayList<JRadioButton> projmodselection = new ArrayList<JRadioButton>();

	public JSimModelSelectorDialogForWriting(ArrayList<String> modelnames, String suggestednewname) {
		super("Choose how to save model in project");
		
		setPreferredSize(dims);
		setResizable(false);

		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		ButtonGroup buttongroup = new ButtonGroup();
		
		Dimension inputboxdims = new Dimension(275, 30);
		inputbox.setMaximumSize(inputboxdims);
		inputbox.setMinimumSize(inputboxdims);
		inputbox.setForeground(Color.blue);
		
		if(suggestednewname != null) inputbox.setText(suggestednewname);
		
		inputbutton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		inputbutton.addActionListener(this);
		inputbutton.setSelected(modelnames.isEmpty());
		
		projmodselection.add(inputbutton);
		buttongroup.add(inputbutton);
		
		// Add the radio buttons indicating the names of models
		// already in the project file
		for(String modname : modelnames){
			JRadioButton button = new JRadioButton("Overwrite " + modname);
			button.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
			buttongroup.add(button);
			button.addActionListener(this);
			projmodselection.add(button);
			panel.add(button);
		}		
		
		inputpanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
		inputpanel.setLayout(new BoxLayout(inputpanel, BoxLayout.X_AXIS));
		inputpanel.setVisible(modelnames.isEmpty());
		inputpanel.add(new JLabel("Enter name: "));
		inputpanel.add(inputbox);
		//inputpanel.add(Box.createHorizontalGlue());
		inputpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		
		panel.add(inputbutton);
		panel.add(inputpanel);
		panel.add(Box.createVerticalGlue());
		
		Object[] array = { new JScrollPane(panel) };

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		Object[] options = new Object[] { "OK", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);
		showDialog();
	}
	
	public String getSelectedModelName(){
		return selectedModelName;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals("value")) {
			String value = optionPane.getValue().toString();
			
			if (value.equals("OK")) {
				
				for(JRadioButton btn : projmodselection){
						if(btn.isSelected()){
							if(btn==inputbutton){
							    if( ! inputbox.getText().isEmpty()){
									selectedModelName = inputbox.getText();
									break;
							    }
							    JOptionPane.showMessageDialog(this, "Please enter a model name");
							    return;
							}
							selectedModelName = btn.getText().replaceFirst("Overwrite ", "");
						}					
				}
			}
			else if(value.equals("Cancel")){
				selectedModelName = null;
			}
			dispose();
		}			
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if(o instanceof JRadioButton){
			inputpanel.setVisible(inputbutton.isSelected());
		}
	}
	
}