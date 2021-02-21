package com.janison.bundler.jetbrains.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.janison.bundler.jetbrains.execution.BundlerExecutionTask;
import com.janison.bundler.jetbrains.execution.BundlerExecutor;
import org.jetbrains.annotations.NotNull;

public class BundlerBuildReleaseAction extends AnAction {

  private static final Logger LOGGER = Logger.getInstance(BundlerBuildReleaseAction.class);

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();

    if (project == null || project.isDisposed()) {
      return;
    }

    final BundlerExecutor executor = project.getComponent(BundlerExecutor.class);
    final BundlerExecutionTask bundlerExecutionTask = new BundlerExecutionTask(project);

    executor.saveAllDocumentsAndRunTask(bundlerExecutionTask, "build-release");
  }
}
