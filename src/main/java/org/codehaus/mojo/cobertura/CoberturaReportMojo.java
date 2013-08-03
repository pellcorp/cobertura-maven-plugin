package org.codehaus.mojo.cobertura;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.cobertura.configuration.MaxHeapSizeUtil;
import org.codehaus.mojo.cobertura.tasks.CommandLineArguments;
import org.codehaus.mojo.cobertura.tasks.ReportTask;

/**
 * Generates a Cobertura Report.
 * 
 * @goal report
 * @phase verify
 */
public class CoberturaReportMojo extends AbstractCoberturaMojo {
	/**
	 * The format of the report. (can be 'html' and/or 'xml'. defaults to
	 * 'html')
	 * 
	 * @parameter
	 */
	private String[] formats = new String[] { "html" };

	/**
	 * Only output cobertura errors, avoid info messages.
	 * 
	 * @parameter expression="${quiet}" default-value="false"
	 * @since 2.1
	 */
	private boolean quiet;

	/**
	 * Maximum memory to pass to JVM of Cobertura processes.
	 * 
	 * @parameter expression="${cobertura.maxmem}"
	 */
	private String maxmem = "64m";

	/**
	 * The encoding for the java source code files.
	 * 
	 * @parameter expression="${project.build.sourceEncoding}"
	 *            default-value="UTF-8".
	 * @since 2.4
	 */
	private String encoding;

	/**
	 * The output directory for the report.
	 * 
	 * @parameter default-value="${project.reporting.outputDirectory}/cobertura"
	 * @required
	 */
	private File outputDirectory;

	public CoberturaReportMojo() {
		if (MaxHeapSizeUtil.getInstance().envHasMavenMaxMemSetting()) {
			maxmem = MaxHeapSizeUtil.getInstance().getMavenMaxMemSetting();
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skipMojo()) {
			return;
		}

		executeReport(getDataFile(), outputDirectory, getCompileSourceRoots());
	}

	/**
	 * Returns the compileSourceRoots for the currently executing project.
	 */
	@SuppressWarnings("unchecked")
	private List<String> getCompileSourceRoots() {
		return getProject().getExecutionProject().getCompileSourceRoots();
	}

	private void executeReport(File curDataFile, File curOutputDirectory,
			List<String> curCompileSourceRoots) {
		ReportTask task = new ReportTask();

		// task defaults
		task.setLog(getLog());
		task.setPluginClasspathList(pluginClasspathList);
		task.setQuiet(quiet);

		// task specifics
		task.setMaxmem(maxmem);
		task.setDataFile(curDataFile);
		task.setOutputDirectory(curOutputDirectory);
		task.setCompileSourceRoots(curCompileSourceRoots);
		task.setSourceEncoding(encoding);

		CommandLineArguments cmdLineArgs;
		cmdLineArgs = new CommandLineArguments();
		cmdLineArgs.setUseCommandsFile(true);
		task.setCmdLineArgs(cmdLineArgs);

		for (int i = 0; i < formats.length; i++) {
			executeReportTask(task, formats[i]);
		}
	}

	private void executeReportTask(ReportTask task, String outputFormat) {
		task.setOutputFormat(outputFormat);

		try {
			task.execute();
		} catch (MojoExecutionException e) {
			getLog().error("Error in Cobertura Report generation: " + e.getMessage(), e);
		}
	}
}
