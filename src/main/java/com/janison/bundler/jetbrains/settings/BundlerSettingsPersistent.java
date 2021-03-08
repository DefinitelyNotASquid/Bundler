package com.janison.bundler.jetbrains.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "BundlerSettingsPersistent"
)
public class BundlerSettingsPersistent implements
    PersistentStateComponent<BundlerSettingsPersistent> {

  public String bundlerLocation;

  public BundlerSettingsPersistent() {
    bundlerLocation = "";
  }

  @Nullable
  @Override
  public BundlerSettingsPersistent getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull BundlerSettingsPersistent state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Nullable
  public static BundlerSettingsPersistent getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, BundlerSettingsPersistent.class);
  }
}