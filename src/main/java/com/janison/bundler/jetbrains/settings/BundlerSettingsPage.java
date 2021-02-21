package com.janison.bundler.jetbrains.settings;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon.Position;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.SwingHelper;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

public class BundlerSettingsPage implements Configurable {

  private static final Logger LOGGER = Logger.getInstance(BundlerSettingsPage.class);

  private final Project project;

  private TextFieldWithHistoryWithBrowseButton bundlerLocation;
  private JLabel bundlerLocationLabel;
  private JPanel panel;
  private JButton automaticExecRetrievalButton;

  public BundlerSettingsPage(Project project) {
    this.project = project;
  }

  @Nls(capitalization = Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "bundler";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    initializeComponents();
    return panel;
  }

  private void initializeComponents() {
    final String bundlerLocationLabelString = "bundler folder Location";
    bundlerLocationLabel.setText(bundlerLocationLabelString);

    final String automaticExecRequirementButtonLabel = "Automatically detect bundler location";
    automaticExecRetrievalButton.setText(automaticExecRequirementButtonLabel);

    initializeAutomaticExecRetrievalButton();
    initializeTextFieldWithHistoryWithBrowseButton();
  }

  private void initializeAutomaticExecRetrievalButton() {
    automaticExecRetrievalButton.addActionListener(e -> {
      final long timeout = 3000;

      try {
        final Path bundlerPath = getProjectSettings().determineAndSetExecutable("bundler");

        if (bundlerPath != null) {
          final String bundlerLocationString = bundlerPath.toString();
          this.bundlerLocation.setTextAndAddToHistory(bundlerLocationString);
          showSuccessBalloon("Found bundler at " + bundlerLocationString, timeout,
                  bundlerLocation);
        } else {
          showErrorBalloon("Bundler does not exist in PATH.\n"
              + "Please select bundler location manually.", timeout, automaticExecRetrievalButton);
        }
      } catch (ExecutionException | InterruptedException | IOException ex) {
        showErrorBalloon("Failed to automatically detect bundler.\n"
                + "Please select bundler location manually.", timeout,
            automaticExecRetrievalButton);
        LOGGER.error(ex);
      }
    });
  }

  private void showSuccessBalloon(String message, long timeout, JComponent component) {
    showBalloon(message, MessageType.INFO, timeout, component);
  }

  private void showErrorBalloon(String message, long timeout, JComponent component) {
    showBalloon(message, MessageType.ERROR, timeout, component);
  }

  private void showBalloon(String message, MessageType messageType, long timeout,
      JComponent component) {
    JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(message, messageType, null)
        .setFadeoutTime(timeout)
        .createBalloon()
        .show(RelativePoint.getSouthOf(component), Position.below);
  }

  private void initializeTextFieldWithHistoryWithBrowseButton() {
    final TextFieldWithHistory textFieldWithHistory = bundlerLocation.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(-1);

    textFieldWithHistory.setTextAndAddToHistory(getPersistentSettings().bundlerLocation);
    SwingHelper.installFileCompletionAndBrowseDialog(project, bundlerLocation,
        "Select Bundler Location",
        FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
  }

  @Override
  public boolean isModified() {
    return !bundlerLocation.getChildComponent().getText()
        .equals(getPersistentSettings().bundlerLocation);
  }

  @Override
  public void apply() throws ConfigurationException {
    final String location = bundlerLocation.getChildComponent().getText();
    if (isPathValid(location)) {
      updateScriptPath(location);
    }
  }

  private boolean isPathValid(String location) {
    // @TODO: Implement logic
    return true;
  }

  private void updateScriptPath(String location) {
    final ProjectSettings settings = getProjectSettings();
    final BundlerSettingsPersistent persistentSettings = getPersistentSettings();

    persistentSettings.bundlerLocation = location;
    settings.setExecutable(location);
  }

  private ProjectSettings getProjectSettings() {
    return project.getComponent(ProjectSettings.class);
  }

  private BundlerSettingsPersistent getPersistentSettings() {
    return BundlerSettingsPersistent.getInstance(project);
  }
}
