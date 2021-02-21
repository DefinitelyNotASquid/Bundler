package com.janison.bundler.jetbrains;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;

public class BundlerMenu implements ApplicationComponent {

  @Override
  public void initComponent() {
    final ActionManager actionManager = ActionManager.getInstance();
    final DefaultActionGroup analyzeMenu = (DefaultActionGroup) actionManager
        .getAction("MainMenu");
    if (analyzeMenu != null) {
      final AnAction action = actionManager.getAction("Bundler.BundlerMenu");
      analyzeMenu.add(action);
    }
  }
}
