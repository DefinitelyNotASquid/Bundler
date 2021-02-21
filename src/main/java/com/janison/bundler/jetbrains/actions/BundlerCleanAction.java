package com.janison.bundler.jetbrains.actions;

import com.janison.bundler.jetbrains.execution.BundlerExecutionTask;
import com.janison.bundler.jetbrains.execution.BundlerExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BundlerCleanAction extends AnAction {

  private static final Logger LOGGER = Logger.getInstance(BundlerCleanAction.class);

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

    executor.saveAllDocumentsAndRunTask(bundlerExecutionTask, "clean");
  }
}
