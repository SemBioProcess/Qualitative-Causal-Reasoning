package myQualitativeReasoning;

import java.awt.Color;

import javax.swing.UIManager;


public class myQualitativeReasoning {

	public static void main(String[] args) {

		try {
			UIManager.put("nimbusOrange", new Color(51,98,140));
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
			}
		    		    
		    new QualitativeReasoningGUI();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
