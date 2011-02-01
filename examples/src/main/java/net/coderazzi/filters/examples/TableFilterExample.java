/**
 * Author:  Luis M Pena  ( lu@coderazzi.net )
 * License: MIT License
 *
 * Copyright (c) 2007 Luis M. Pena  -  lu@coderazzi.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderazzi.filters.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.util.Comparator;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.coderazzi.filters.Filter;
import net.coderazzi.filters.IFilter;
import net.coderazzi.filters.examples.utils.AgeCustomChoice;
import net.coderazzi.filters.examples.utils.CenteredRenderer;
import net.coderazzi.filters.examples.utils.EventsWindow;
import net.coderazzi.filters.examples.utils.FlagRenderer;
import net.coderazzi.filters.examples.utils.TestData;
import net.coderazzi.filters.examples.utils.TestTableModel;
import net.coderazzi.filters.gui.AutoChoices;
import net.coderazzi.filters.gui.FilterSettings;
import net.coderazzi.filters.gui.IFilterEditor;
import net.coderazzi.filters.gui.IFilterHeaderObserver;
import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui.TableFilterHeader.Position;


@SuppressWarnings("serial")
public class TableFilterExample extends JFrame {

    private static final String MAX_HISTORY_LENGTH = "max history length";
	private static final String IGNORE_CASE = "ignore case";
	private static final String ENABLED = "enabled";
    private static final String EDITABLE = "editable";
	private static final String AUTO_CHOICES = "auto choices";

    TestTableModel tableModel;
    JTable table;
    JPanel tablePanel;    
    JPanel filterHeaderPanel;
    TableFilterHeader filterHeader;
    JMenu filtersMenu;
    JCheckBoxMenuItem allEnabled;
    JCheckBoxMenuItem useFlagRenderer;
    JCheckBoxMenuItem countrySpecialSorter;
    JCheckBoxMenuItem enableUserFilter;
    IFilter userFilter;
    
    public TableFilterExample() {
        super("Table Filter Example");
        getContentPane().add(createGui());
        setJMenuBar(createMenu());
    	filterHeader.setTable(table);
        customizeTable();
    }
    
    private JPanel createGui(){
    	tableModel = TestTableModel.createTestTableModel();
    	table = new JTable(tableModel);
    	tablePanel = new JPanel(new BorderLayout());
    	tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
    	tablePanel.setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createLoweredBevelBorder(),
    			BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    	filterHeader = new TableFilterHeader(){
    		
    		@Override protected void customizeEditor(IFilterEditor editor) {
    			super.customizeEditor(editor);
    			//enter here any code to customize the editor
    		}
    	};
    	filterHeader.addHeaderObserver(new IFilterHeaderObserver() {
			
			@Override public void tableFilterUpdated(TableFilterHeader header,
					IFilterEditor editor, TableColumn tableColumn) {
				//no need to react
			}
			
			@Override public void tableFilterEditorExcluded(TableFilterHeader header,
					IFilterEditor editor, TableColumn tableColumn) {
		    	getMenu(filtersMenu, (String) tableColumn.getHeaderValue(), true);
			}
			
			@Override public void tableFilterEditorCreated(TableFilterHeader header,
					IFilterEditor editor, TableColumn tableColumn) {
		    	addColumnToFiltersMenu(editor, (String) tableColumn.getHeaderValue());
			}
		});
		useFlagRenderer=new JCheckBoxMenuItem("country flags as icons -in choices-", true);
		useFlagRenderer.addItemListener(new ItemListener() {
			
			@Override public void itemStateChanged(ItemEvent e) {
				setCountryEditorRenderer();
			}
		});
    	
		countrySpecialSorter = new JCheckBoxMenuItem("country column sorted by red proportion", false);
		countrySpecialSorter.addItemListener(new ItemListener() {			
			@Override public void itemStateChanged(ItemEvent e) {
				setCountryComparator(countrySpecialSorter.isSelected());
			}
		});
    	return tablePanel;
    }
    
    private JMenuBar createMenu(){
    	JMenuBar menu = new JMenuBar();
    	menu.add(createTableMenu());
    	menu.add(createHeaderMenu());
    	menu.add(createFiltersMenu());
    	menu.add(createMiscellaneousMenu());
    	return menu;
    }
    
    private JMenu createFiltersMenu(){
    	
		userFilter = new Filter() {
			int nameColumn=tableModel.getColumn(TestTableModel.NAME);
			@Override public boolean include(Entry entry) {
				return -1!=entry.getStringValue(nameColumn).indexOf('e');
			}
		};
		
		JCheckBoxMenuItem includeUserFilter=new JCheckBoxMenuItem(
				new AbstractAction("include in header") {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				if(source.isSelected()){
					filterHeader.addFilter(userFilter);
				} else {
					filterHeader.removeFilter(userFilter);					
				}
				enableUserFilter.setSelected(userFilter.isEnabled());
			}
		});
		
		enableUserFilter=new JCheckBoxMenuItem(
				new AbstractAction("enable") {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				userFilter.setEnabled(source.isSelected());
			}
		});
		enableUserFilter.setSelected(userFilter.isEnabled());
		
    	JMenu menu = new JMenu("User filter (name without 'e')");
    	menu.add(includeUserFilter);
    	menu.add(enableUserFilter);
    	filtersMenu = new JMenu("Filters");
    	filtersMenu.setMnemonic(KeyEvent.VK_F);
		filtersMenu.add(menu);		
		filtersMenu.addSeparator();
    	
		return filtersMenu;

    }
    
    private JMenu createTableMenu(){
    	JCheckBoxMenuItem autoResize = 
    		new JCheckBoxMenuItem(new AbstractAction("Auto resize") {
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				table.setAutoResizeMode(source.isSelected()? 
						JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
				table.doLayout();
			}
		});
		autoResize.setSelected(table.getAutoResizeMode()!=JTable.AUTO_RESIZE_OFF);
    	final AbstractAction removeElement = 
    		new AbstractAction("Remove top row") {
			
			@Override public void actionPerformed(ActionEvent e) {
				tableModel.removeTestData();
				if (tableModel.getRowCount()==0){
					((JComponent)e.getSource()).setEnabled(false);
				}
			}
		};
				
    	JMenu tableMenu = new JMenu("Table");
    	tableMenu.setMnemonic(KeyEvent.VK_T);
    	
    	tableMenu.add(new JMenuItem(
    			new AbstractAction("Create male row [first position]") {			
			@Override public void actionPerformed(ActionEvent e) {
				addTestData(true);
		        removeElement.setEnabled(true);
			}
		}));
    	tableMenu.add(new JMenuItem(
    			new AbstractAction("Create female row [first position]") {			
			@Override public void actionPerformed(ActionEvent e) {
				addTestData(false);
		        removeElement.setEnabled(true);
			}
		}));
    	
		tableMenu.add(new JMenuItem(removeElement));
		tableMenu.addSeparator();
		tableMenu.add(autoResize);
		tableMenu.addSeparator();
		tableMenu.add(new JMenuItem(new AbstractAction("Change model width") {			
			@Override public void actionPerformed(ActionEvent e) {
				tableModel.changeModel(table);
                customizeTable();
                removeElement.setEnabled(true);
			}
		}));
		tableMenu.add(new JMenuItem(new AbstractAction("Use new model") {			
			@Override public void actionPerformed(ActionEvent e) {
            	tableModel = TestTableModel.createTestTableModel();
                table.setModel(tableModel);
                customizeTable();
                removeElement.setEnabled(true);
			}
		}));
    	return tableMenu;
    }

    private JMenu createHeaderMenu(){    	
    	JCheckBoxMenuItem onUse=new JCheckBoxMenuItem(
    			new AbstractAction("on use") {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				if (source.isSelected()){
					filterHeader.setTable(table);
					customizeTable();
				} else {
					filterHeader.setTable(null);
				}				
			}
		});
    	
    	JCheckBoxMenuItem ignoreCase=new JCheckBoxMenuItem(
    			new AbstractAction(IGNORE_CASE) {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.getParserModel().setIgnoreCase(source.isSelected());	
				updateFiltersInfo();
			}
		});
    	
    	JCheckBoxMenuItem adaptiveChoices=new JCheckBoxMenuItem(
    			new AbstractAction("adaptive choices") {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setAdaptiveChoices(source.isSelected());				
				updateFiltersInfo();
			}
		});
    	
    	allEnabled=new JCheckBoxMenuItem(
    			new AbstractAction(ENABLED) {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setEnabled(source.isSelected());
				updateFiltersInfo();
			}
		});
    	
    	JCheckBoxMenuItem visible=new JCheckBoxMenuItem(
    			new AbstractAction("visible") {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				filterHeader.setVisible(source.isSelected());				
			}
		});
    	
    	JMenuItem reset = new JMenuItem(new AbstractAction("reset") {			
			@Override public void actionPerformed(ActionEvent e) {
				filterHeader.resetFilter();
				updateFiltersInfo();
			}
		});
    	onUse.setSelected(true);
    	ignoreCase.setMnemonic(KeyEvent.VK_C);
    	ignoreCase.setSelected(filterHeader.getParserModel().isIgnoreCase());
    	adaptiveChoices.setSelected(filterHeader.isAdaptiveChoices());
    	allEnabled.setSelected(filterHeader.isEnabled());
    	visible.setSelected(filterHeader.isVisible());

    	JMenu ret = new JMenu("Filter Header");
    	ret.setMnemonic(KeyEvent.VK_H);
    	ret.add(onUse);
    	ret.addSeparator();
    	ret.add(ignoreCase);
    	ret.add(adaptiveChoices);
    	ret.add(createAutoChoicesMenu(filterHeader.getAutoChoices(), new AutoChoicesSet() {			
			@Override public void setAutoChoices(AutoChoices ao) {
				filterHeader.setAutoChoices(ao);
				updateFiltersInfo();
			}
		}));
    	ret.add(allEnabled);
    	ret.addSeparator();
    	ret.add(visible);
    	ret.add(createPositionMenu());
    	ret.add(createAppearanceMenu(null));
    	ret.add(createMaxRowsMenu());
    	ret.addSeparator();
    	ret.add(reset);
    	return ret;
    }
    
    private JMenu createAutoChoicesMenu(AutoChoices preselected, final AutoChoicesSet iface){
    	JMenu ret = new JMenu(AUTO_CHOICES);
    	ButtonGroup group = new ButtonGroup();
    	for (AutoChoices ao : AutoChoices.values()){
    		final AutoChoices set = ao;
        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(
        			new AbstractAction(ao.toString().toLowerCase()) {			
    			@Override public void actionPerformed(ActionEvent e) {
    				iface.setAutoChoices(set);
    			}
    		});    		
        	group.add(item);
        	if (preselected==ao){
        		item.setSelected(true);
        	}
        	ret.add(item);
    	}
    	return ret;
    }
        	
    private JMenu createlLookAndFeelMenu(){
    	JMenu ret = new JMenu("Look And Feel");
    	ButtonGroup group = new ButtonGroup();
    	LookAndFeel now = UIManager.getLookAndFeel();
    	for (LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()){
    		final String classname = lfi.getClassName();
        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(
        			new AbstractAction(lfi.getName()) {			
    			@Override public void actionPerformed(ActionEvent e) {
    				try{
    					UIManager.setLookAndFeel(classname);
    	        	} catch(Exception ex){
    	        		ex.printStackTrace();
    	        		System.exit(0);
    	        	}
    	        	SwingUtilities.updateComponentTreeUI(TableFilterExample.this);
    	        	TableFilterExample.this.pack();
    			}
    		});
        	group.add(item);
        	ret.add(item);
        	if (lfi.getName().equals(now.getName())){
        		item.setSelected(true);
        	}    		    			
    	}
    	if (group.getButtonCount()<2){
    		ret.setEnabled(false);
    	}
    	return ret;
    }
        	
    private JMenu createMiscellaneousMenu(){
    	
    	JMenuItem events = new JMenuItem(new AbstractAction("events window") {
    		EventsWindow window;
			@Override public void actionPerformed(ActionEvent e) {
				if (window==null || !window.isVisible()){
					window = new EventsWindow(TableFilterExample.this, 
							filterHeader);
					window.setVisible(true);
				} else {
					window.requestFocus();
				}
			}
		});

    	JMenu ret = new JMenu("Miscellaneous");
    	ret.setMnemonic(KeyEvent.VK_M);
    	ret.add(events);
    	ret.addSeparator();
    	ret.add(createlLookAndFeelMenu());
    	return ret;
    }
    
    private JMenu createPositionMenu(){
    	JRadioButtonMenuItem top = new JRadioButtonMenuItem(
    			new AbstractAction("top (automatic)") {			
			@Override public void actionPerformed(ActionEvent e) {
				setPosition(Position.TOP);
			}
		});
    	JRadioButtonMenuItem inline = new JRadioButtonMenuItem(
    			new AbstractAction("inline (automatic)") {			
			@Override public void actionPerformed(ActionEvent e) {
				setPosition(Position.INLINE);
			}
		});
    	JRadioButtonMenuItem manual = new JRadioButtonMenuItem(
    			new AbstractAction("bottom (manual)") {			
			@Override public void actionPerformed(ActionEvent e) {
				setPosition(Position.NONE);
			}
		});
    	ButtonGroup group = new ButtonGroup();
    	group.add(top);
    	group.add(inline);
    	group.add(manual);
    	switch(filterHeader.getPosition()){
    		case TOP: 
    			top.setSelected(true);
    			break;
    		case INLINE: 
    			inline.setSelected(true);
    			break;
    		case NONE: 
    			manual.setSelected(true);
    			break;
    	}
    	setPosition(filterHeader.getPosition());
    	JMenu ret = new JMenu("position");
    	ret.add(top);
    	ret.add(inline);
    	ret.add(manual);
    	return ret;
    }
    	
    private JMenu createAppearanceMenu(final IFilterEditor editor){
    	JMenu ret = new JMenu("appearance");
    	ret.add(new JMenuItem(new AbstractAction("background color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select background color", 
						editor==null? filterHeader.getBackground() : editor.getBackground());
				if (ret!=null){
					if (editor==null) {filterHeader.setBackground(ret);} 
					else {editor.setBackground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("foreground color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select foreground color", 
						editor==null? filterHeader.getForeground() : editor.getForeground());
				if (ret!=null){
					if (editor==null) {filterHeader.setForeground(ret);} 
					else {editor.setForeground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("disabled color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select disabled color", 
						editor==null? filterHeader.getDisabledForeground() : editor.getDisabledForeground());
				if (ret!=null){
					if (editor==null) {filterHeader.setDisabledForeground(ret);} 
					else {editor.setDisabledForeground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("grid color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select grid color", 
						editor==null? filterHeader.getGridColor() : editor.getGridColor());
				if (ret!=null){
					if (editor==null) {filterHeader.setGridColor(ret);} 
					else {editor.setGridColor(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("error color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select error color", 
						editor==null? filterHeader.getErrorForeground() : editor.getErrorForeground());
				if (ret!=null){
					if (editor==null) {filterHeader.setErrorForeground(ret);} 
					else {editor.setErrorForeground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("selection foreground ...") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select selection foreground", 
						editor==null? filterHeader.getSelectionForeground() : editor.getSelectionForeground());
				if (ret!=null){
					if (editor==null) {filterHeader.setSelectionForeground(ret);} 
					else {editor.setSelectionForeground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("selection background ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select selection background", 
						editor==null? filterHeader.getSelectionBackground() : editor.getSelectionBackground());
				if (ret!=null){
					if (editor==null) {filterHeader.setSelectionBackground(ret);} 
					else {editor.setSelectionBackground(ret);}
				}
			}
		}));
    	ret.add(new JMenuItem(new AbstractAction("text selection color ...") {			
			@Override public void actionPerformed(ActionEvent e) {
				Color ret = JColorChooser.showDialog(TableFilterExample.this, 
						"Select text selection color", 
						editor==null? filterHeader.getTextSelectionColor() : editor.getTextSelectionColor());
				if (ret!=null){
					if (editor==null) {filterHeader.setTextSelectionColor(ret);} 
					else {editor.setTextSelectionColor(ret);}
				}
			}
		}));
    	ret.addSeparator();
    	ret.add(createFontSizeMenu());			
    	return ret;
    }
    
    void addColumnToFiltersMenu(final IFilterEditor editor, final String name) {
    	JMenu menu = (JMenu) getMenu(filtersMenu, name, false);
    	menu.add(createAutoChoicesMenu(editor.getAutoChoices(), new AutoChoicesSet(){
    		@Override public void setAutoChoices(AutoChoices ao) {
    			editor.setAutoChoices(ao);
    			updateFilter(editor, name);
    		}
    	}
    	));
    	JCheckBoxMenuItem editable = new JCheckBoxMenuItem(EDITABLE, editor.isEditable());
    	editable.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				editor.setEditable(source.isSelected());				
			}
		});
    	menu.add(editable);
    	JCheckBoxMenuItem enabled = new JCheckBoxMenuItem(ENABLED, editor.getFilter().isEnabled());
    	enabled.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				editor.getFilter().setEnabled(source.isSelected());
				allEnabled.setSelected(filterHeader.isEnabled());
			}
		});
    	menu.add(enabled);
    	JCheckBoxMenuItem ignoreCase = new JCheckBoxMenuItem(IGNORE_CASE, editor.isIgnoreCase());
    	ignoreCase.addActionListener(new ActionListener() {			
			@Override public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source =(JCheckBoxMenuItem) e.getSource();
				boolean ignoreCase=source.isSelected();
				editor.setIgnoreCase(ignoreCase);
			}
		});
    	menu.add(ignoreCase);
    	menu.addSeparator();
    	if (name.equalsIgnoreCase("country")){
    		menu.add(useFlagRenderer);
    		menu.add(countrySpecialSorter);
    		menu.addSeparator();
    	}
    	
    	JMenu history = new JMenu(MAX_HISTORY_LENGTH);
    	ButtonGroup max = new ButtonGroup();
    	
    	for (int i=0; i<10;i++){
    		JRadioButtonMenuItem item = createMaxHistoryMenuItem(editor, i);
    		max.add(item);    		
    		history.add(item);
    	}
    	
    	menu.add(createAppearanceMenu(editor));
    	menu.add(history);

    	menu.add(new JMenuItem(new AbstractAction("Remove this column"){
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			TableColumnModel model = table.getColumnModel();
    			model.removeColumn(model.getColumn(model.getColumnIndex(name)));
    		}
    	}));
    }
    
    void updateFiltersInfo() {
		TableColumnModel model = table.getColumnModel();
    	int n = model.getColumnCount();
    	while (n-->0){
    		TableColumn tc = model.getColumn(n);
			updateFilter(filterHeader.getFilterEditor(tc.getModelIndex()), (String) tc.getHeaderValue());
    	}
    	enableUserFilter.setSelected(userFilter.isEnabled());
    }

    void updateFilter(IFilterEditor editor, String columnName) {
    	JMenu menu = (JMenu) getMenu(filtersMenu, columnName, false);
    	((JCheckBoxMenuItem) getMenu(menu, EDITABLE, false)).setSelected(editor.isEditable());
    	((JCheckBoxMenuItem) getMenu(menu, ENABLED, false)).setSelected(editor.getFilter().isEnabled());
    	((JCheckBoxMenuItem) getMenu(menu, IGNORE_CASE, false)).setSelected(editor.isIgnoreCase());
    	JMenu autoChoicesMenu = (JMenu) getMenu(menu, AUTO_CHOICES, false);
    	((JRadioButtonMenuItem ) getMenu(autoChoicesMenu, editor.getAutoChoices().toString().toLowerCase(), false)).setSelected(true);
    	JMenu historyMenu = (JMenu) getMenu(menu, MAX_HISTORY_LENGTH, false);
    	JRadioButtonMenuItem item = ((JRadioButtonMenuItem ) getMenu(historyMenu, String.valueOf(editor.getMaxHistory()), false)); 
    	if (item!=null){ item.setSelected(true);}
    }

    JMenuItem getMenu(JMenu menu, String name, boolean remove){
    	int pos=menu.getItemCount();
    	while(pos-->0){
    		JMenuItem item = menu.getItem(pos);
    		if (item!=null && item.getText().equals(name)){
    			if (remove){
    				menu.remove(pos);
    				item=null;
    			}
    			return item;
    		}
    	}
    	if (remove){
    		return null;
    	}
    	JMenu ret = new JMenu(name);
    	menu.add(ret);
    	return ret;
    }
    
    private JMenu createFontSizeMenu(){
    	int RELATIVE_FONT_SIZES[]={-2, -1, 0, 1, 2, 4, 8, 16};
    	int size=filterHeader.getFont().getSize();
    	JMenu ret = new JMenu("font size");
    	ButtonGroup group = new ButtonGroup();
    	for (int i : RELATIVE_FONT_SIZES){
    		JRadioButtonMenuItem item = createFontSizeMenuItem(size+i);
    		ret.add(item);
    		group.add(item);
    		if (i==0){
    			item.setSelected(true);
    		}
    	}
    	return ret;
    }
    
    private JRadioButtonMenuItem createFontSizeMenuItem(final int size){
    	return new JRadioButtonMenuItem(new AbstractAction(String.valueOf(size)) {			
			@Override public void actionPerformed(ActionEvent e) {
				filterHeader.setFont(
						filterHeader.getFont().deriveFont((float)(size)));
			}
		});
    }
    
    private JMenu createMaxRowsMenu (){
    	JMenu ret = new JMenu("max visible rows on popup");
    	ButtonGroup group = new ButtonGroup();
    	for (int i=4; i<16;i++){
    		JRadioButtonMenuItem item = createMaxRowsMenuItem(i);
    		if (i==filterHeader.getMaxVisibleRows()){
    			item.setSelected(true);
    		}
    		group.add(item);
    		ret.add(item);
    	}
    	return ret;
    }
    
    private JRadioButtonMenuItem createMaxRowsMenuItem(final int i){
    	return new JRadioButtonMenuItem(new AbstractAction(String.valueOf(i)) {			
			@Override public void actionPerformed(ActionEvent e) {
				filterHeader.setMaxVisibleRows(i);
			}
		});
    }
    
    private JRadioButtonMenuItem createMaxHistoryMenuItem(final IFilterEditor editor, final int i){
    	JRadioButtonMenuItem ret = new JRadioButtonMenuItem(new AbstractAction(String.valueOf(i)) {			
			@Override public void actionPerformed(ActionEvent e) {
				editor.setMaxHistory(i);
			}
		});
    	if (editor.getMaxHistory()==i){
    		ret.setSelected(true);
    	}
    	return ret;
    }
    
    void setCountryEditorRenderer(){
        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);
        boolean set = useFlagRenderer.isSelected();
        if (tableModel!=null && tableModel.getColumnCount() > countryColumn) {
        	filterHeader.getFilterEditor(countryColumn).setListCellRenderer(
        			set? new FlagRenderer() : null);
        	filterHeader.getFilterEditor(countryColumn).setEditable(false);
        }
    }
    
    void setCountryComparator(boolean set){
        int column = tableModel.getColumn(TestTableModel.COUNTRY);
        if (tableModel!=null && tableModel.getColumnCount() > column) {
        	Comparator<TestData.Flag> comp = set? new TestData.RedComparator() : null;
        	filterHeader.getFilterEditor(column).setComparator(comp);
        }    	    	
    }
    
    void customizeTable() {
    	if (filterHeader.getTable()!=null){
	        int countryColumn = tableModel.getColumn(TestTableModel.COUNTRY);
	
	        if (tableModel.getColumnCount() > countryColumn) {
	            table.getColumnModel().getColumn(
	            		table.convertColumnIndexToView(countryColumn)).
	            		setCellRenderer(new FlagRenderer());
	
	        	setCountryEditorRenderer();
	        }
	
	        int agesColumn = tableModel.getColumn(TestTableModel.AGE);
	
	        if (tableModel.getColumnCount() > agesColumn) {
	            table.getColumnModel().getColumn(
	            		table.convertColumnIndexToView(agesColumn)).
	            		setCellRenderer(new CenteredRenderer());
	            filterHeader.getFilterEditor(agesColumn).setCustomChoices(AgeCustomChoice.getCustomChoices());
	        }
	
	        int datesColumn = tableModel.getColumn(TestTableModel.DATE);
	
	        if (tableModel.getColumnCount() > datesColumn) {
	            table.getColumnModel().getColumn(
	            		table.convertColumnIndexToView(datesColumn)).
	            		setCellRenderer(new DefaultTableCellRenderer(){
	
	            			private static final long serialVersionUID = 
	            				8042527267257156699L;
	            			Format parser = filterHeader.getParserModel().getFormat(Date.class);
	
	            			@Override public Component getTableCellRendererComponent(
	            					JTable table, Object value, boolean isSelected, 
	            					boolean hasFocus, int row, int column) {
	            				if (value instanceof Date){
	            					value = parser.format(value);
	            				}
	            				return super.getTableCellRendererComponent(table, 
	            						value, isSelected, hasFocus, row, column);
	            			}			
	            		});
	        }        
	        setCountryComparator(countrySpecialSorter.isSelected());
    	}
    }

    void addTestData(boolean male) {
        TestData td = new TestData();
        td.male = male;
        tableModel.addTestData(td);
    }

	void setPosition(Position position) {
		if (filterHeader.getPosition()==Position.NONE){
			filterHeaderPanel.remove(filterHeader);
			tablePanel.remove(filterHeaderPanel);
			tablePanel.revalidate();
		}
		filterHeader.setPosition(position);
		if (filterHeader.getPosition()==Position.NONE){
			filterHeaderPanel = new JPanel(new BorderLayout());
			filterHeaderPanel.add(filterHeader, BorderLayout.CENTER);
			filterHeaderPanel.setBorder(BorderFactory.createLineBorder(
					filterHeader.getDisabledForeground(),1));
			tablePanel.add(filterHeaderPanel, BorderLayout.SOUTH);
			tablePanel.revalidate();
		}
	}
	
	interface AutoChoicesSet{
		void setAutoChoices(AutoChoices ao);
	}
	
    public final static void main(String[] args) {
    	FilterSettings.autoChoices=AutoChoices.ENABLED;
        TableFilterExample frame = new TableFilterExample();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
