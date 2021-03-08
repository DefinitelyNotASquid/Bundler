package com.janison.bundler.jetbrains.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.janison.bundler.jetbrains.execution.BundlerExecutionTask;
import com.janison.bundler.jetbrains.execution.BundlerExecutor;
import com.janison.bundler.jetbrains.infocollectors.SystemMetrics;
import com.janison.bundler.jetbrains.insightstelemetry.AzureTelemtryClient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BundlerTestAction extends AnAction {

  private static final Logger LOGGER = Logger.getInstance(BundlerTestAction.class);

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

    AzureTelemtryClient az = new AzureTelemtryClient();
    SystemMetrics sm = new SystemMetrics();

    Map<String,String> extras = new HashMap<>();

    extras.put("name", project.getProjectFile().getName());

    az.telemetryClient.trackEvent("Test:Solution", sm.getEnvProperties(extras), sm.getMetrics());

    executor.saveAllDocumentsAndRunTask(bundlerExecutionTask, "test");
  }
}
