package edu.gatech.cs7450.prefuse;

import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.junit.Test;

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
}
