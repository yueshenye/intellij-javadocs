package com.github.setial.intellijjavadocs.configuration.impl;

import com.github.setial.intellijjavadocs.configuration.JavaDocConfiguration;
import com.github.setial.intellijjavadocs.model.settings.JavaDocSettings;
import com.github.setial.intellijjavadocs.model.settings.Level;
import com.github.setial.intellijjavadocs.model.settings.Mode;
import com.github.setial.intellijjavadocs.model.settings.Visibility;
import com.github.setial.intellijjavadocs.template.DocTemplateManager;
import com.intellij.openapi.components.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Java doc configuration impl.
 *
 * @author Sergey Timofiychuk
 */
@State(
        name = JavaDocConfiguration.COMPONENT_NAME,
        storages = {@Storage("JavaDoc.xml")}
)
public class JavaDocConfigurationImpl implements JavaDocConfiguration, ApplicationComponent, PersistentStateComponent<Element> {

    private JavaDocSettings settings;
    private boolean loadedStoredConfig = false;

    private DocTemplateManager templateManager;

    /**
     * Instantiates a new Java doc configuration object.
     */
    public JavaDocConfigurationImpl() {
        templateManager = ServiceManager.getService(DocTemplateManager.class);
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }


    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public JavaDocSettings getConfiguration() {
        JavaDocSettings result;
        try {
            result = (JavaDocSettings) getSettings().clone();
        } catch (Exception e) {
            // return null if cannot clone object
            result = null;
        }
        return result;
    }

    @Override
    public void updateConfiguration(JavaDocSettings javaDocSettings) {
        this.settings = javaDocSettings;
        setupTemplates();
    }

    @Nullable
    @Override
    public Element getState() {
        Element root = new Element("JAVA_DOC_SETTINGS_PLUGIN");
        if (settings != null) {
            settings.addToDom(root);
            loadedStoredConfig = true;
        }
        return root;
    }

    @Override
    public void loadState(Element javaDocSettings) {
        settings = new JavaDocSettings(javaDocSettings);
        setupTemplates();
        loadedStoredConfig = true;
    }

    private JavaDocSettings getSettings() {
        if (!loadedStoredConfig) {
            // setup default values
            settings = new JavaDocSettings();
            Set<Level> levels = new HashSet<Level>();
            levels.add(Level.TYPE);
            levels.add(Level.METHOD);
            levels.add(Level.FIELD);

            Set<Visibility> visibilities = new HashSet<Visibility>();
            visibilities.add(Visibility.PUBLIC);
            visibilities.add(Visibility.PROTECTED);

            settings.getGeneralSettings().setOverriddenMethods(false);
            settings.getGeneralSettings().setSplittedClassName(true);
            settings.getGeneralSettings().setMode(Mode.UPDATE);
            settings.getGeneralSettings().setLevels(levels);
            settings.getGeneralSettings().setVisibilities(visibilities);

            settings.getTemplateSettings().setClassTemplates(templateManager.getClassTemplates());
            settings.getTemplateSettings().setConstructorTemplates(templateManager.getConstructorTemplates());
            settings.getTemplateSettings().setMethodTemplates(templateManager.getMethodTemplates());
            settings.getTemplateSettings().setFieldTemplates(templateManager.getFieldTemplates());
        }
        return settings;
    }

    private void setupTemplates() {
        templateManager.setClassTemplates(settings.getTemplateSettings().getClassTemplates());
        templateManager.setConstructorTemplates(settings.getTemplateSettings().getConstructorTemplates());
        templateManager.setMethodTemplates(settings.getTemplateSettings().getMethodTemplates());
        templateManager.setFieldTemplates(settings.getTemplateSettings().getFieldTemplates());
    }
}
