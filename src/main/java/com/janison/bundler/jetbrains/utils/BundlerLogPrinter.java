package com.janison.bundler.jetbrains.utils;

import com.janison.bundler.jetbrains.ui.BundlerConsoleView;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class BundlerLogPrinter implements ProjectComponent {

  private static final Logger LOGGER = Logger.getInstance(BundlerLogPrinter.class);

  private final Project project;
  private final BundlerConsoleView bundlerConsoleView;

  public BundlerLogPrinter(Project project, BundlerConsoleView bundlerConsoleView) {
    this.project = project;
    this.bundlerConsoleView = bundlerConsoleView;
  }

  public void print(String value){
    //todo parse and remove illegal characters and then run Tyson's Regex script over it
    bundlerConsoleView.print(value, BundlerSeverity.INFO);
  }
}
