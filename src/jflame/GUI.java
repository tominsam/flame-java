package jflame;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

class MyTableModel extends AbstractTableModel {
	
	public Vector data;
	String[] columnNames;
	String[] attributes;
	
	public MyTableModel( String[] cols, String[] ats, Vector list ) {
		super();
		columnNames = cols;
		attributes = ats;
		data = list;
	}
	public int getColumnCount() {
		return columnNames.length;
	}
	public int getRowCount() {
		return data.size();
	}
	public String getColumnName(int col) {
		return columnNames[col].toString();
	}
	public boolean isCellEditable(int row, int col) {
		return false;
	}        	
	public Object getValueAt(int row, int col) {
		Object element = data.elementAt(row);
		String attribute = attributes[col];
		try {
			return element.getClass()
			              .getDeclaredMethod(attribute,new Class[] {})
			              .invoke(element, new Object[] {});
		} catch (Exception e) {
			System.out.println("Can't print column " + attribute + ": " + e);
		}
		return null;
	}
}

public class GUI extends JFrame implements ListSelectionListener, ServiceWatcher {

    private JLabel hostInfo;
    private JTable hostTable;
    private JTable serviceTable;
	private MyTableModel serviceModel;
	private MyTableModel hostModel;

    public GUI() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("can't use native look and feel - falling back");
        }

		hostModel = new MyTableModel(
        		new String[] { "name", "address" },
        		new String[] { "name", "address" },
        		new Vector()
        );

		
        serviceModel = new MyTableModel(
        		new String[] { "type", "name", "port", "address", "info" },
        		new String[] { "type", "name", "port", "address", "info" },
        		new Vector()
        );

       
        // the components I care about
        hostTable = new JTable();
        hostTable.setModel(hostModel);
        hostTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = hostTable.getSelectionModel();
        rowSM.addListSelectionListener(this);
        
        hostInfo = new javax.swing.JLabel();
        hostInfo.setVerticalAlignment(SwingConstants.TOP);
        hostInfo.setBorder( new EmptyBorder(5,5,5,5) );
        hostInfo.setText("<html><i>Select a host to see details</i>");
        
        serviceTable = new javax.swing.JTable();
		serviceTable.setModel(serviceModel);
        
        JScrollPane hostScrollPane = new JScrollPane(hostTable);
        JScrollPane serviceScrollPane = new JScrollPane(serviceTable);
        serviceScrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );

        JSplitPane jSplitPane1 = new JSplitPane();
        jSplitPane1.setDividerLocation(280);

        JSplitPane jSplitPane2 = new JSplitPane();
        jSplitPane2.setDividerLocation(100);
        jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);

        jSplitPane1.setLeftComponent( hostScrollPane );
        jSplitPane1.setRightComponent(jSplitPane2);
        jSplitPane2.setTopComponent(hostInfo);
        jSplitPane2.setRightComponent( serviceScrollPane );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JFlame");

        getContentPane().add( jSplitPane1 );
        setSize( 800, 500 );
    }

    
    public void serviceAdded(Network n, Service s) {
        updateHosts(n);
    }

    public void serviceRemoved(Network n, Service s) {
        updateHosts(n);
    }

	public void updateHosts( Network n ) {
		// remove hosts that are gone
		hostModel.data.retainAll( n.hosts.values() );

		// add new hosts
		Iterator i = n.hosts.values().iterator();
		while (i.hasNext()) {
			Host host = (Host)i.next();
			if (!hostModel.data.contains( host )) {
				hostModel.data.add( host );
			}
		}

		// update GUI elements
		hostModel.fireTableDataChanged();
		updateServices();
	}
    
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		updateServices();
	}


	private void updateServices() {
		ListSelectionModel selectionModel = hostTable.getSelectionModel();
		int selection = selectionModel.getLeadSelectionIndex();
		if (selection < 0) return;
		Host host = (Host)hostModel.data.elementAt(selection);
		
		hostInfo.setText(
				"<html><b>Information for host "+host.name()+"</b>\n"+
				"<br>IP address: "+host.address() + "\n"
				);

		serviceModel.data = host.services();
		serviceModel.fireTableDataChanged();
	}
    
}
