package com.alikemal.codestat.ui;

import com.alikemal.codestat.StatsCollector;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.alikemal.codestat.Config.DEFAULT_API_URL;

/**
 * Created by nicd on 02/06/16.
 */
public class SettingsForm implements Configurable {
    public static String CONFIG_PREFIX = "code-stats-intellij-";
    public static String API_KEY_NAME = CONFIG_PREFIX + "api-key";
    public static String API_URL_NAME = CONFIG_PREFIX + "api-url";


    private JPanel ui;
    private JTextField apiKey = new JTextField(API_KEY_NAME);
    private JTextField apiURL = new JTextField(API_URL_NAME);
    private String persistedApiKey;
    private String persistedApiURL;
    private PropertiesComponent propertiesComponent;

    @Nls
    @Override
    public String getDisplayName() {
        return "Code::Stats";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        propertiesComponent = PropertiesComponent.getInstance();

        persistedApiKey = propertiesComponent.getValue(API_KEY_NAME);
        persistedApiURL = propertiesComponent.getValue(API_URL_NAME, DEFAULT_API_URL);

        SwingUtilities.invokeLater(() -> {
            apiKey.setText(persistedApiKey);
            apiURL.setText(persistedApiURL);
        });

        return ui;
    }


    @Override
    public boolean isModified() {
        return !apiKey.getText().equals(persistedApiKey) || !apiURL.getText().equals(persistedApiURL);
    }

    @Override
    public void apply() throws ConfigurationException {
        persistedApiKey = apiKey.getText();
        persistedApiURL = apiURL.getText();

        propertiesComponent.setValue(API_KEY_NAME, persistedApiKey);
        propertiesComponent.setValue(API_URL_NAME, persistedApiURL);

        StatsCollector statsCollector = ApplicationManager.getApplication().getComponent(StatsCollector.class);
    }

    @Override
    public void reset() {
        apiKey.setText(persistedApiKey);
        apiURL.setText(persistedApiURL);
    }

    @Override
    public void disposeUIResources() {

    }
}
