package com.janison.bundler.jetbrains.ui;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class BundlerToolWindowFactory implements ToolWindowFactory, DumbAware {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    final BundlerConsoleView bundlerConsoleView = project
        .getComponent(BundlerConsoleView.class);
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    final ConsoleView consoleView = bundlerConsoleView.getConsoleView();
    final Content content = contentFactory
        .createContent(consoleView.getComponent(), "Bundler Log", false);
    toolWindow.getContentManager().addContent(content);
  }
}
