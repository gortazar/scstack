package es.sidelab.tools.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OutputStreamReaderRunnable implements Runnable {

	private InputStream is;
	private Exception error;
	private String output;

	public OutputStreamReaderRunnable(InputStream is) {
		this.is = is;
	}
	
	public void run() {
		final BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = r.readLine()) != null) {
				System.out.println(line);
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			this.error = e;
		}
		
		this.output = sb.toString();
	}

	public Exception getError() {
		return error;
	}
	
	public String getOutput() {
		return output;
	}
}
