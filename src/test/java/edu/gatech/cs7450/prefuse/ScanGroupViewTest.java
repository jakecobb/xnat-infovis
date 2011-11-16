package edu.gatech.cs7450.prefuse;

import java.net.URL;

import javax.swing.JFrame;

import org.junit.Test;


public class ScanGroupViewTest {
	@Test
	public void testScanGroupView() throws Exception {
		URL testURL = ScanGroupViewTest.class.getResource("/scan_group_test.xml");
		
		ScanGroupView view = new ScanGroupView(testURL.toURI().toString());
	
		JFrame frame = new JFrame("blah");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(view);
		frame.pack();
		frame.setVisible(true);
		
		view.begin();
		
		Thread.sleep(500000L);
	}
}
