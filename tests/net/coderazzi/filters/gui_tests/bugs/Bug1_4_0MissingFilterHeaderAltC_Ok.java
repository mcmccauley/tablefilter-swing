package net.coderazzi.filters.gui_tests.bugs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.coderazzi.filters.gui.TableFilterHeader;
import net.coderazzi.filters.gui_tests.TestTableModel;

/**
 * Variation of Bug1_4_0MissingFilterHeader. 
 * The table and header are setup after adding the table to the container
 * This test run fine on version 1.4.0
 */
public class Bug1_4_0MissingFilterHeaderAltC_Ok extends JPanel{
	
	private static final long serialVersionUID = -22834158973030319L;

	private JTable table;
	private TableFilterHeader filterHeader;
	
	public Bug1_4_0MissingFilterHeaderAltC_Ok() {
		super(new BorderLayout());
		table = new JTable();
		filterHeader = new TableFilterHeader();
		add(new JScrollPane(table), BorderLayout.CENTER);
		table.setModel(TestTableModel.createLargeTestTableModel(20));
		filterHeader.setTable(table);
	}
	
	public static void main(String[] args) {
		Bug1_4_0MissingFilterHeaderAltC_Ok testTableFilter = new Bug1_4_0MissingFilterHeaderAltC_Ok();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(testTableFilter);
		frame.pack();
		frame.setVisible(true);
	}
}
