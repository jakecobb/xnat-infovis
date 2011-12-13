package edu.gatech.cs7450.prefuse;

import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
	private static final String HOST = "http://node18.cci.emory.edu:8080/xnat/REST";
	private static XNATConnection conn;
	
	@BeforeClass
	public static void beforeClass() {
		conn = new XNATConnection(HOST, "nbia", "nbia");
	}
	
	@AfterClass
	public static void afterClass() {
		conn = null;
	}
	
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
		final String PROJECT = "HF_BRN_TUMOR";
		long start = System.currentTimeMillis();
		XNATSearch search = new XNATSearch(conn);
		
		SearchWhere where = new SearchWhere(SearchMethod.AND, new SingleCriteria("xnat:mrSessionData/PROJECT", CompareOperator.EQUAL, PROJECT));
		SearchQuery query = new SearchQuery("Test Search", where);
		
		XNATResultSet result = search.runSearch(query);
		
		Tree dataTree = TreeViewLoader.loadResult(PROJECT, result);
		long end = System.currentTimeMillis();
		
		System.out.println("query+loadResult took " + (end - start) + " ms");
		
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
	
	@Test
	public void testNewTreeViewLoad() throws Exception {
		final String PROJECT = "HF_BRN_TUMOR";
		long start = System.currentTimeMillis();
		TreeViewLoader loader = new TreeViewLoader(conn, PROJECT);
		
		Tree dataTree = loader.loadProjectData();
		long end = System.currentTimeMillis();
		System.out.println("loadProjectData took " + (end - start) + " ms");
		
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
