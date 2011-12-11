package edu.gatech.cs7450.prefuse;

import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.junit.Test;

import prefuse.data.Tree;
import edu.gatech.cs7450.xnat.SearchQuery;
import edu.gatech.cs7450.xnat.SearchWhere;
import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
import edu.gatech.cs7450.xnat.SingleCriteria;
import edu.gatech.cs7450.xnat.SingleCriteria.CompareOperator;
import edu.gatech.cs7450.xnat.XNATConnection;
import edu.gatech.cs7450.xnat.XNATResultSet;
import edu.gatech.cs7450.xnat.XNATSearch;

public class TreeViewTest {
	@Test
	public void testTreeViewWithChiOntology() throws Exception {
		URL ontURL = TreeViewTest.class.getResource("/chi-ontology.xml.gz");
		
		final String ontPath = ontURL.toURI().toString();
		
      JComponent treeview = TreeView.demo(ontPath, "name");
      final JFrame frame = new JFrame("XNAT Overview");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setContentPane(treeview);
      frame.pack();
      frame.setVisible(true);

		Thread t = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000L);
						if( !frame.isVisible() ) {
							break;
						}
					} catch(InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
		t.start();
		t.join();
	}
	
	@Test
	public void testTreeViewFromSearchResult() throws Exception {
		final String HOST = "http://node18.cci.emory.edu:8080/xnat/REST";
		final String PROJECT = "HF_BRN_TUMOR";
		XNATConnection conn = new XNATConnection(HOST, "nbia", "nbia");
		
		XNATSearch search = new XNATSearch(conn);
		
		SearchWhere where = new SearchWhere(SearchMethod.AND, new SingleCriteria("xnat:mrSessionData/PROJECT", CompareOperator.EQUAL, PROJECT));
		SearchQuery query = new SearchQuery("Test Search", where);
		
		XNATResultSet result = search.runSearch(query);
		
		Tree dataTree = TreeViewLoader.loadResult(PROJECT, result);
		
		
      JComponent treeview = TreeView.demo(dataTree, "name");
      final JFrame frame = new JFrame("XNAT Overview");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setContentPane(treeview);
      frame.pack();
      frame.setVisible(true);

		Thread t = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000L);
						if( !frame.isVisible() ) {
							break;
						}
					} catch(InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
		t.start();
		t.join();
	}
}
