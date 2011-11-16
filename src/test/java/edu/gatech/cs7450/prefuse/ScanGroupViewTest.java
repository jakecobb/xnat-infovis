package edu.gatech.cs7450.prefuse;

import java.net.URL;

import org.junit.Test;


public class ScanGroupViewTest {
	@Test
	public void testScanGroupView() throws Exception {
		URL testURL = ScanGroupViewTest.class.getResource("/scan_group_test.xml");
		ScanGroupView.main(new String[] { testURL.toURI().toString() });
		
		Thread.sleep(20000L);
	}
}
