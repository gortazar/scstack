/*******************************************************************************
 * This file is part of Sidelab Tools.
 * 
 * Copyright (c) Sidelab Team.
 * http://www.sidelab.es/
 * http://code.sidelab.es/projects/sidelabtools
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package es.sidelab.tools.commandline;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class CommandLine {

	enum OS {
		WIN, LINUX
	}
	private static OS OPERATING_SYSTEM = System.getProperty("os.name").contains("Windows") ? OS.WIN : OS.LINUX;

	private File workDir;
	
	public CommandLine() {}
	
	public CommandLine(File workDir) {
		this.workDir = workDir;
	}

	/**
	 * <p>Runs the command specified</p>.
	 * 
	 * @param command The command to run
	 * @return The Java {@link Process} object that represents the OS process
	 * @throws IOException
	 */
	private Process exec(String command) throws IOException {
		String[] cmdLine;
		if(OPERATING_SYSTEM == OS.WIN) {
			cmdLine = new String[] { "cmd", "/c", command };
		} else {
			StringTokenizer st = new StringTokenizer(command);
			cmdLine = new String[st.countTokens()];
			int index = 0;
			while(st.hasMoreTokens()) {
				cmdLine[index++] = st.nextToken();
			}
		}
		return Runtime.getRuntime().exec(cmdLine, null, workDir);
	}
	
	/**
	 * <p>Runs the command specified, and waits until the command finishes. This method returns the standard and error
	 * outputs of the command.</p>
	 * 
	 * <p>If the command finishes with an error, an {@link ExecutionCommandException} is thrown.</p> 
	 * 
	 *  <p>If there are errors when reading from the std or err outputs of the command, an {@link IOException}
	 *  is thrown.</p>
	 *  
	 * @param command The command to run
	 * @return The standard and error outputs of the command
	 * @throws IOException When reading errors occur
	 * @throws ExecutionCommandException When the process exits with an error
	 */
	public CommandOutput syncExec(String command) throws IOException, ExecutionCommandException {
		
		final Process p = exec(command);

		OutputStreamReaderRunnable std = new OutputStreamReaderRunnable(p.getInputStream());
		OutputStreamReaderRunnable err = new OutputStreamReaderRunnable(p.getErrorStream());
		
		//InputStreamWriterRunnable in = new InputStreamWriterRunnable(p.getOutputStream());
		
		Thread stdThread = new Thread(std);
		Thread errThread = new Thread(err);
		//Thread inThread = new Thread(in);
		//inThread.setDaemon(true);
		
		stdThread.start();
		errThread.start();
		//inThread.start();
		
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new IOException("Process has been interrupted", e);
		}
		
		if(std.getError() != null) {
			throw new IOException("Standard output reading error", std.getError());
		}
		
		if(err.getError() != null) {
			throw new IOException("Error output reading error", err.getError());
		}
		
		if(p.exitValue() != 0) {
			throw new ExecutionCommandException("Process exited with errors. Exit code = " + p.exitValue(), p.exitValue(), std.getOutput(), err.getOutput());
		}

		return new CommandOutput(std.getOutput(), err.getOutput());
	}
	
	/**
	 * <p>Sets the working directory to use.</p>
	 * @param workDir The working directory to use
	 */
	public void setWorkDir(File workDir){
		this.workDir = workDir;
	}

	/**
	 * <p>Returns the configured working directory.</p>
	 * 
	 * @return the configured working directory, or null if a working directory was not configured
	 */
	public File getWorkDir() {
		return workDir;
	}
	
}
