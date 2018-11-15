package myQualitativeReasoning;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ResultsScroller extends JScrollPane implements ListSelectionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JList<String> resultslist;

	public ResultsScroller(String title){
		
		this.setBorder(BorderFactory.createTitledBorder(title));
		this.getVerticalScrollBar().setUnitIncrement(12);
		this.getHorizontalScrollBar().setUnitIncrement(12);
		this.setPreferredSize(new Dimension(350, 400));
		this.setBackground(Color.white);
		
		resultslist = new JList<String>();
		resultslist.addListSelectionListener(this);
		resultslist.setBackground(Color.white);
		resultslist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultslist.setBorder(BorderFactory.createBevelBorder(1));
		resultslist.setEnabled(true);
		
		this.getViewport().add(resultslist);
	}
	
	
	public void updateResults(String[] data){
		String[] formatteddata = data;
		for(int i=0;i<data.length;i++){
			String datum = data[i];
			formatteddata[i] = datum.endsWith("_property") ? datum.replace("_property", "") : datum;
		}
		resultslist.setListData(formatteddata);
//		resultslist.setListData(data);
		validateAndRepaint();
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void clear(){
		resultslist.setListData(new String[]{});
		validateAndRepaint();
	}
	
	private void validateAndRepaint(){
		this.validate();
		this.repaint();
	}
}
