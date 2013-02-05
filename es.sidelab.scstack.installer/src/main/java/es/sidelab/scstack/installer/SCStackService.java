package es.sidelab.scstack.installer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.scstack.crawler.CrawlerException;
import es.sidelab.scstack.crawler.CrawlerInfo;
import es.sidelab.scstack.crawler.RedmineCrawler;
import es.sidelab.scstack.lib.exceptions.SCStackException;

/**
 * Provides a method to install the REST service and web console of 
 * the SidelabCode's Stack as a system daemon.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class SCStackService {
	/** Filename of the archived binaries for the SCStack service. */
	public static final String SERVICE_ZIP = "scstack-service-bin.zip";
	/** Name of the resulting directory when uncompressing
	 * the {@link SCStackService#SERVICE_ZIP} file. */
	public static final String SERVICE_DIR = "scstack-service";
	/** Filename of the JAR file that will be executed by the daemon. 
	 * Should exist inside the folder determined by {@link SCStackService#SERVICE_DIR}. */
	public static final String SERVICE_JAR = "scstack-service.jar";
	/** Filename for the daemon configuration file (will be stored in 
	 * {@code /etc/init/}). */
	public static final String DAEMON_CONF = "scstack-service.conf";
	/** Filename for the daemon executable file (will be stored in 
	 * {@code /etc/init.d/}). */
	public static final String DAEMON = "scstack-service";
	
	/**
	 * Configures the Redmine installation using a RedmineCrawler and 
	 * retrieves the REST API key.
	 * Then it installs the REST service and web console as a daemon. 
	 * @throws CrawlerException
	 * @throws SCStackException 
	 * @throws IOException 
	 * @throws ExecutionCommandException 
	 */
	public void install() throws CrawlerException, SCStackException, ExecutionCommandException, IOException {
		System.out.println("\n*** CONFIGURING REDMINE PAGE (using WebCrawling tech)***\n");
		RedmineCrawler rcJSEnabled;
		rcJSEnabled = new RedmineCrawler(true, new CrawlerInfo("redmine", "http://localhost/login"));
		LDAPConnection conn = new LDAPConnection.Builder("ldapconn")
					.host(Instalacion.config.getProperty("hostLDAP"))
					.port(Instalacion.config.getProperty("puertoLDAP"))
					.account(Instalacion.config.getProperty("bindDN"))
					.accountPassword(Instalacion.config.getProperty("passBindDN"))
					.baseDN(Instalacion.config.getProperty("baseDN"))
					.build();
		String apiKey = rcJSEnabled.configureRedmine("admin", "admin", conn);
		if (apiKey == null || apiKey.isEmpty())
			throw new CrawlerException("Redmine's API key could not be obtained." +
					" REST Service will not be installed.");
		Instalacion.overwriteConfigValue("keyRedmineAPI", apiKey);
		System.out.println("**************************************************\n");
		
		System.out.println("\n*** INSTALLING REST SERVICE AS DAEMON***\n");
		Instalacion.ejecutar("unzip " + SERVICE_ZIP);
		//copy the configuration file (overwriting it if necessary) into the service's folder
		Instalacion.ejecutar("cp " + Instalacion.CONFIGURATION_FILENAME + " " + SERVICE_DIR);
		
		//Generating temporary files for the daemon inside the current dir.
		// These 2 files are marked for deletion when the installation ends. 
		// After their generation they must be copied into 
		//   their corresponding system folders.
		String currentDirPath = System.getProperty("user.dir");
		if (currentDirPath == null)
			throw new SCStackException("Unable to establish current workind directory!");
		File curDir = new File(currentDirPath);
		File scservDir = new File(curDir, SERVICE_DIR);
		if (! scservDir.isDirectory())
			throw new SCStackException("Unable to find the service's unzipped directory!");
		File scservJar = new File(scservDir, SERVICE_JAR);
		if (! scservJar.exists())
			throw new SCStackException("Unable to find the service's JAR file!");
		String tempDaemonExe = generateDaemonExeFile(curDir, scservJar.getAbsolutePath());
		String tempDaemonConf = generateDaemonConfigFile(curDir, scservDir.getAbsolutePath());
		
		File daemonConf = new File("/etc/init/" + DAEMON_CONF);
		Instalacion.ejecutar("cp " + tempDaemonConf + " " + daemonConf.getAbsolutePath());
		if (! daemonConf.exists())
			throw new SCStackException("Unable to copy daemon config file to '/etc/init/' !");
		Instalacion.ejecutar("chown root:root " + daemonConf.getAbsolutePath());
		Instalacion.ejecutar("chmod 644 " + daemonConf.getAbsolutePath());
		if (! daemonConf.canRead())
			throw new SCStackException("Daemon config file from '/etc/init/' is not readable!");
		
		File daemonExe = new File("/etc/init.d/" + DAEMON);
		Instalacion.ejecutar("cp " + tempDaemonExe + " " + daemonExe.getAbsolutePath());
		if (! daemonExe.exists())
			throw new SCStackException("Unable to copy daemon exe file to '/etc/init.d/' !");
		Instalacion.ejecutar("chown root:root " + daemonExe.getAbsolutePath());
		Instalacion.ejecutar("chmod 755 " + daemonExe.getAbsolutePath());
		if (! daemonExe.canExecute())
			throw new SCStackException("Daemon exe file from '/etc/init.d/' is not executable!");

		Instalacion.ejecutar("start " + DAEMON);
		//waiting 3 sec before obtaining the daemon's status
		long time = 3000;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.out.println("Interrupted while sleeping for " + time + "milis.");
			e.printStackTrace();
		}
		Instalacion.ejecutar("status " + DAEMON);
		System.out.println("**************************************************\n");
	}

	private String generateDaemonExeFile(File curDir, String serviceJarPath) throws IOException {
		File f = File.createTempFile(DAEMON, null, curDir);
		PrintWriter out = new PrintWriter(new FileWriter(f));
		try {
			out.println("#!/bin/bash");
			out.println("");
			out.println("# Change this path to your own value");
			out.println("serviceJarPath=" + serviceJarPath);
			out.println("");
			out.println("# Don't change the following lines");
			out.println("cmd=\"java -Dfile.encoding=UTF-8 -jar $serviceJarPath\"");
			out.println("exec $cmd");
			out.println("");
		} finally {
			out.close();
		}
		f.deleteOnExit();
		return f.getAbsolutePath();
	}

	private String generateDaemonConfigFile(File curDir, String serviceDirAbsPath) throws IOException {
		File f = File.createTempFile(DAEMON_CONF, null, curDir);
		PrintWriter out = new PrintWriter(new FileWriter(f));
		try {
			out.println("# SCStack service");
			out.println("");
			out.println("description     \"SCStack Service\"");
			out.println("author          \"Patxi Gortázar <patxi.gortazar@gmail.com>\"");
			out.println("");
			out.println("# Change this path to your own value");
			out.println("env SCSTACK_DIR=" + serviceDirAbsPath);
			out.println("");
			out.println("# Don't change the following lines");
			out.println("##############################################");
			out.println("start on (net-device-up");
			out.println("          and local-filesystems");
			out.println("	      and runlevel [2345])");
			out.println("stop on runlevel [016]");
			out.println("");
			out.println("respawn");
			out.println("respawn limit 15 5");
			out.println("");
			out.println("script");
			out.println("chdir $SCSTACK_DIR");
			out.println("exec /etc/init.d/" + DAEMON + " > $SCSTACK_DIR/" + DAEMON + ".log 2>&1");
			out.println("end script");
			out.println("");
		} finally {
			out.close();
		}
		f.deleteOnExit();
		return f.getAbsolutePath();
	}

}
