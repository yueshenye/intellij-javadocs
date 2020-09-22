package com.github.setial.intellijjavadocs.configuration.configurable;

import com.github.setial.intellijjavadocs.configuration.JavaDocConfiguration;
import com.github.setial.intellijjavadocs.model.settings.JavaDocSettings;
import com.github.setial.intellijjavadocs.ui.settings.ConfigPanel;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JavaDocConfigurable implements Configurable {
    private ConfigPanel configPanel;
    private JavaDocConfiguration javaDocConfiguration;
    private JavaDocSettings workingSettings;

    public JavaDocConfigurable() {
        javaDocConfiguration = ServiceManager.getService(JavaDocConfiguration.class);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "JavaDoc";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        workingSettings = javaDocConfiguration.getConfiguration();

        if (configPanel == null) {
            configPanel = new ConfigPanel(workingSettings);
        }
        reset();
        return configPanel;
    }

    @Override
    public boolean isModified() {
        return configPanel.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        configPanel.apply();
        javaDocConfiguration.updateConfiguration(workingSettings);
    }

    @Override
    public void reset() {
        configPanel.reset();
    }

    @Override
    public void disposeUIResources() {
        configPanel.disposeUIResources();
        configPanel = null;
    }
}
