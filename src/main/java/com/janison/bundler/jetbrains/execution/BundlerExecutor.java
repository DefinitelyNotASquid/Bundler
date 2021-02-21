package com.janison.bundler.jetbrains.execution;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BundlerExecutor implements ProjectComponent {

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final ProgressManager progressManager;
  private final Project project;

  BundlerExecutor(Project project, ProgressManager progressManager) {
    this.project = project;
    this.progressManager = progressManager;
  }

  /**
   * Runs our custom {@link BundlerExecutionTask} synchronously with the specified progress indicator.
   *
   * @param task instance of {@link BundlerExecutionTask} to be run
   * @see ProgressManager#runProcess(Runnable, ProgressIndicator)
   */
  public void runTask(BundlerExecutionTask task, String actionName) {
    BackgroundableProcessIndicator processIndicator = new BackgroundableProcessIndicator(task);
    executor.submit(() ->
        progressManager.runProcess(() -> task.run(processIndicator,actionName), processIndicator));
  }

  /**
   * Saves all the cached documents to the file system and runs the task.
   * Documents are needed to be saved since bundler can only be run on filesystem files
   */
  public void saveAllDocumentsAndRunTask(BundlerExecutionTask task, String actionName) {
    saveAllDocuments();
    runTask(task,actionName);
  }

  private void saveAllDocuments() {
    WriteAction.run(() -> FileDocumentManager.getInstance().saveAllDocuments());
  }
}
