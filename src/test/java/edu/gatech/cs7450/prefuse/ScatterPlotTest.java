package edu.gatech.cs7450.prefuse;

import java.net.URL;

import javax.swing.JFrame;

import org.junit.Test;

public class ScatterPlotTest {
	@Test
	public void testScatterPlot() throws Exception {
		URL csvFile = ScatterPlotTest.class.getResource("/xnat_table.csv");
		
		final JFrame frame = XNATScatterPlot.buildFrame(csvFile.toURI().toString());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
						break;
					}
				}
			}
		});
		t.start();
		t.join();
	}
}
