package com.janison.bundler.jetbrains.execution;

import com.janison.bundler.jetbrains.utils.BundlerLogPrinter;
import com.janison.bundler.jetbrains.utils.BundlerSeverity;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.janison.bundler.jetbrains.settings.ProjectSettings;
import com.janison.bundler.jetbrains.ui.BundlerConsoleView;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class BundlerRunner implements ProjectComponent {

  private static final Logger LOGGER = Logger.getInstance(BundlerRunner.class);
  private final ProjectSettings projectSettings;
  private final Project project;
  private final BundlerConsoleView bundlerConsoleView;
  final BundlerLogPrinter logPrinter;

  public BundlerRunner(Project project, ProjectSettings projectSettings,
                       BundlerConsoleView bundlerConsoleView) {
    this.project = project;
    this.projectSettings = projectSettings;
    this.bundlerConsoleView = bundlerConsoleView;
    this.logPrinter = project.getComponent(BundlerLogPrinter.class);
  }

  /**
   * This method creates a runnable process and runs it.
   * {@link OSProcessHandler#startNotify()} is used to capture the output
   *
   * @return the instance with all the required information about the run
   */
  public ProcessOutput execute(String command) throws ExecutionException, FileNotFoundException {
    final GeneralCommandLine commandLine = getNewGeneralCommandLine(command);
    final String commandLineString = commandLine.getCommandLineString();
    final Process process = commandLine.createProcess();
    final OSProcessHandler processHandler = new OSProcessHandler(process, commandLineString);
    final ProcessOutput processOutput = getProcessOutputWithTextAvailableListener(processHandler);

    final String logRunCommand = "Running bundler command \"" + commandLineString + "\"\n";
    bundlerConsoleView.clear();
    bundlerConsoleView.print("Running " +command + " Command.", BundlerSeverity.VERBOSE);

    LOGGER.info(logRunCommand);
    processHandler.startNotify();
    holdAndWaitProcess(processHandler, processOutput);

    LOGGER.info("Finished Running bundler.");
    bundlerConsoleView.print("Finished Running bundler.", BundlerSeverity.VERBOSE);
    return processOutput;
  }

  /**
   * This method creates a new instance of GeneralCommandLine
   * and sets its configurations according to the input params provided
   *
   * <p>The following configurations are set:
   * the current working directory
   * the executable path
   *
   * @return an instance of GeneralCommandLine
   */
  public GeneralCommandLine getNewGeneralCommandLine(String command) throws FileNotFoundException {
    final String cwd = projectSettings.getCwd();
    final String executable = projectSettings.getExecutable();
    final List<String> sections = projectSettings.getSections();

    final GeneralCommandLine commandLine = new GeneralCommandLine();

    commandLine.setWorkDirectory("C:\\CLS\\ICAS\\bundler");
    commandLine.setExePath("C:\\CLS\\ICAS\\bundler\\node.exe");
    commandLine.addParameter("bundler.js");

    switch (command){
      case "build":{
        commandLine.addParameter("#devBuild");
        commandLine.addParameter("#dataviz");
        commandLine.addParameter("#bundlelint");
        commandLine.addParameter("#output:../output/bundler");
        commandLine.addParameter("#bootstrappers:../Bootstrappers/Mocha.Bootstrapper");
        commandLine.addParameter("../");
        break;
      }
      case "clean":{
        commandLine.addParameter("#clean");
        commandLine.addParameter("#output:../output/bundler");
        commandLine.addParameter("#bootstrappers:all");
        commandLine.addParameter("../");
        break;
      }
      default:
        break;

    }

    return commandLine;
  }

  /**
   * This method waits for the process to exit and subsequently
   * sets the required flags for {@link ProcessOutput}.
   *
   * @param processHandler the instance of running process
   * @param processOutput the instance to set appropriate flags
   */
  private void holdAndWaitProcess(@NotNull OSProcessHandler processHandler,
      @NotNull ProcessOutput processOutput) {
    final long timeOutInMilliseconds = TimeUnit.SECONDS
        .toMillis(projectSettings.getTimeOutInSeconds());

    if (processHandler.waitFor(timeOutInMilliseconds)) {
      LOGGER.info("Process exited with exit code " + processHandler.getExitCode());
      processOutput.setExitCode(processHandler.getExitCode());
    } else {
      LOGGER.info("Thanos finger snap!");
      processHandler.destroyProcess();
      processOutput.setTimeout();
    }
  }

  /**
   * This method creates an instance of {@link ProcessOutput}
   * and attaches it to the given {@link OSProcessHandler}.
   *
   * @param processHandler an instance of {@link OSProcessHandler} which is to be linked with
   *     {@link ProcessOutput}
   * @return an instance of {@link ProcessOutput} attached to the process handler
   */
  private ProcessOutput getProcessOutputWithTextAvailableListener(
      @NotNull OSProcessHandler processHandler) {
    final ProcessOutput processOutput = new ProcessOutput();

    addTextAvailableListener(processHandler, processOutput);

    return processOutput;
  }

  /**
   * This method attaches a {@link ProcessAdapter#onTextAvailable} listener
   * to the given instance of {@link OSProcessHandler}.
   *
   * @param processHandler the instance to which the listener is to be attached
   * @param processOutput the instance to which output from stdout and stderr is appended
   *     to create strings
   */
  private void addTextAvailableListener(@NotNull OSProcessHandler processHandler,
      @NotNull ProcessOutput processOutput) {
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        String text = event.getText();

        if (outputType.equals(ProcessOutputTypes.STDERR)) {
          //lets print here
          logPrinter.print(text);
          processOutput.appendStderr(text);
        } else if (!outputType.equals(ProcessOutputTypes.SYSTEM)) {
          //lets print here
          logPrinter.print(text);
          processOutput.appendStdout(text);
        }
      }
    });
  }
}
