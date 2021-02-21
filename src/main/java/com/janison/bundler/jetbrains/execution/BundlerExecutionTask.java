package com.janison.bundler.jetbrains.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.janison.bundler.jetbrains.utils.Notifier;

import java.io.FileNotFoundException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BundlerExecutionTask extends Task.Backgroundable {

  private static final Logger LOGGER = Logger.getInstance(BundlerExecutionTask.class);

  public BundlerExecutionTask(@Nullable Project project) {
    super(project, "Bundler Task");
  }

  public void run(@NotNull ProgressIndicator progressIndicator, String actionName) {
    final BundlerRunner bundlerRunner = myProject.getComponent(BundlerRunner.class);

    progressIndicator.checkCanceled();

    progressIndicator.setIndeterminate(true);
    progressIndicator.setText("Running Bundler...");
    LOGGER.info("Running Bundler...");

    //no longer using this for now?
    ProcessOutput processOutput = null;
    try {
      processOutput = bundlerRunner.execute(actionName);
    } catch (ExecutionException | FileNotFoundException e) {
      Notifier.showErrorNotification("Failed to run Bundler. Make sure the supplied path is "
          + "correct.", e);
      e.printStackTrace();
    }

    progressIndicator.checkCanceled();

    progressIndicator.setIndeterminate(false);
    progressIndicator.setFraction(.8);
    progressIndicator.setText("Bundler completed!");

    if (processOutput == null) {
      Notifier.showErrorNotification("An unknown error occurred.", null);
    }
  }

  /*
      Overrides needed
   */
  public BundlerExecutionTask(@Nullable Project project, @NotNull String title,
                              boolean canBeCancelled) {
    super(project, title, canBeCancelled);
  }

  @Override
  public void run(@NotNull ProgressIndicator indicator) {

  }
}
