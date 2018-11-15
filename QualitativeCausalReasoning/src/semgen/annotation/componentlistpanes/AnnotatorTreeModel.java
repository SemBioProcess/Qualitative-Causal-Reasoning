package semgen.annotation.componentlistpanes;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import semgen.SemGenSettings;
import semgen.SemGenSettings.SettingChange;
import semgen.annotation.componentlistpanes.buttons.AnnotatorTreeNode;
import semgen.annotation.workbench.AnnotatorTreeMap;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.ModelEdit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.annotation.workbench.drawers.SubModelToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public class AnnotatorTreeModel implements TreeModel, Observer {
	private AnnotatorWorkbench workbench;
	private CodewordToolDrawer cwdrawer;
	private SubModelToolDrawer smdrawer;
	
	private AnnotatorTreeNode focus;
	private RootButton root = new RootButton();
	
	private HashMap<CodewordTreeButton, Integer> cwmap = new HashMap<CodewordTreeButton, Integer>();
	private HashMap<Integer, CodewordTreeButton> cwmapinv = new HashMap<Integer, CodewordTreeButton>();
	private HashMap<SubModelTreeButton, Integer> smmap = new HashMap<SubModelTreeButton, Integer>();
	private HashMap<Integer, SubModelTreeButton> smmapinv = new HashMap<Integer, SubModelTreeButton>();
	
	private ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	protected SemGenSettings settings;
	
	public AnnotatorTreeModel(AnnotatorWorkbench wb, SemGenSettings sets) {
		workbench = wb;
		settings = sets;
		cwdrawer = wb.openCodewordDrawer();
		smdrawer = wb.openSubmodelDrawer();
		
		wb.addObserver(this);
		cwdrawer.addObserver(this);
		smdrawer.addObserver(this);
		sets.addObserver(this);
		
		loadModel();
	}
	
	private void loadModel() {
		AnnotatorTreeMap treemap = workbench.makeTreeMap(settings.showImports());
		
		if (!treemap.getSubmodelList().isEmpty()) {
			int i = 0;
			for (Integer smi : treemap.getSubmodelList()) {
				addSubModelNode(smi, treemap.getSubmodelDSIndicies(i), root);
				i++;
			}
		}
		for (Integer dsi : treemap.getOrphanDS()) {
			addCodewordNode(dsi, root);
		}
	}
	
	private void addSubModelNode(Integer index, ArrayList<Integer> dsindicies, AnnotatorTreeNode parent) {
		SubModelTreeButton newnode = new SubModelTreeButton();
				
		for (Integer dsi : dsindicies) {
			addCodewordNode(dsi, newnode);
		}
		
		smmap.put(newnode, index);
		smmapinv.put(index, newnode);
		parent.add(newnode);
	}
		
	private void addCodewordNode(Integer index, AnnotatorTreeNode parent) {
		CodewordTreeButton newnode = new CodewordTreeButton();
				
		cwmap.put(newnode, index);
		cwmapinv.put(index, newnode);
		parent.add(newnode);
	}
	
	public void reload() {
		clearModel();
		loadModel();
		fireTreeStructureChanged();
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public Object getChild(Object node, int arg1) {
		if (getChildCount(node)!=0)
			return ((DefaultMutableTreeNode)node).getChildAt(arg1);
		return null;
	}

	@Override
	public int getChildCount(Object node) {
		return ((DefaultMutableTreeNode)node).getChildCount();
	}

	@Override
	public int getIndexOfChild(Object node, Object child) {
		return ((DefaultMutableTreeNode)node).getIndex((DefaultMutableTreeNode)child);
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return ((DefaultMutableTreeNode)node).isLeaf();
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		
	}
	
	protected void fireTreeStructureChanged() {
		TreeModelEvent e = new TreeModelEvent(this, root.getPath());
        for (TreeModelListener tml : listeners) {
            tml.treeStructureChanged(e);
        }
    }
	
	protected void fireNodeChanged(DefaultMutableTreeNode node) {
        TreeModelEvent e = new TreeModelEvent(this, node.getPath());
        for (TreeModelListener tml : listeners) {
            tml.treeNodesChanged(e);
        }
    }
	
	public TreePath getSelectedPath() {
		return new TreePath(focus.getPath());
	}
	
	private void changeButtonFocus(AnnotatorTreeNode focusbutton) {
		focus = focusbutton;
	}
	
	private void clearModel() {
		focus = null;
		root.removeAllChildren();
		cwmap.clear(); cwmapinv.clear();
		smmap.clear(); smmapinv.clear();
	}
	
	public void destroy() {
		clearModel();
		fireTreeStructureChanged();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (settings.useTreeView()) {
			if (arg1==ModelEdit.FREE_TEXT_CHANGED) {
				if (focus!=null) {
					fireNodeChanged(focus);
				}
			}
			if (arg1==SettingChange.toggleproptype) {
				fireTreeStructureChanged();
			}
			if (arg1==SettingChange.SHOWIMPORTS) {
				reload();
			}
			if (arg0==smdrawer) {
				if (arg1 == ModelEdit.SMNAMECHANGED) {
					changeButtonFocus(smmapinv.get(smdrawer.getSelectedIndex()));
					fireTreeStructureChanged();
					
				}
				if	(arg1==ModelEdit.SUBMODEL_CHANGED) {
					for (Integer i : smdrawer.getChangedComponents()) {
						fireNodeChanged(smmapinv.get(i));
					}
				}
			}
			if (arg0==cwdrawer) {
				if	(arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.PROPERTY_CHANGED) {
					for (Integer i : cwdrawer.getChangedComponents()) {
						fireNodeChanged(cwmapinv.get(i));
					}
				}
			}
			if (arg0==workbench) {
				if (arg1==ModelEdit.CODEWORD_CHANGED || arg1==ModelEdit.SUBMODEL_CHANGED) {
					fireTreeStructureChanged();
				}
			}
		}
	}
	
	protected class RootButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;
		
		public RootButton() {}
		
		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			JLabel label = new JLabel(workbench.getCurrentModelName());
			label.setIcon(SemGenIcon.homeiconsmall);
			label.setFont(SemGenFont.defaultBold(1));
			return label;
		}

		@Override
		public void onSelection() {
			changeButtonFocus(this);
			workbench.openModelAnnotationsWorkbench().notifyOberserversofMetadataSelection(0);
		}
	}
	
	class SubModelTreeButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			int index = smmap.get(this);
			
			SubModelTreeButton btn = new SubModelTreeButton(smdrawer.getComponentName(index), smdrawer.isEditable(index));
			btn.toggleHumanDefinition(smdrawer.hasHumanReadableDef(index));
			return btn;
		}

		@Override
		public void onSelection() {
			changeButtonFocus(this);
			smdrawer.setSelectedIndex(smmap.get(this));
		}
	}
	
	class CodewordTreeButton extends AnnotatorTreeNode {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateButton(int index) {}

		@Override
		public Component getButton() {
			int index = cwmap.get(this);
			
			CodewordTreeButton btn = new CodewordTreeButton(cwdrawer.getLookupName(index), cwdrawer.isEditable(index), settings.useDisplayMarkers());
			
			btn.toggleHumanDefinition(cwdrawer.hasHumanReadableDef(index));
			btn.toggleSingleAnnotation(cwdrawer.hasSingularAnnotation(index));
			btn.refreshCompositeAnnotationCode(cwdrawer.getAnnotationStatus(index));
			btn.refreshPropertyOfMarker(cwdrawer.getPropertyType(index));
			return btn;
		}
		
		@Override
		public void onSelection() {
			changeButtonFocus(this);
			cwdrawer.setSelectedIndex(cwmap.get(this));
		}
	}
	

}
