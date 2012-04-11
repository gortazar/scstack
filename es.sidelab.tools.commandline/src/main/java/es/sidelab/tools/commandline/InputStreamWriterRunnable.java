package es.sidelab.tools.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class InputStreamWriterRunnable implements Runnable {

	private OutputStream out;
	private Exception error;

	public InputStreamWriterRunnable(OutputStream out) {
		this.out = out;
	}

	public void run() {

		try {

			byte[] buffer = new byte[1024];
			int leidos = 0;
			while ((leidos = System.in.read(buffer)) != 0) {
				out.write(buffer, 0, leidos);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
