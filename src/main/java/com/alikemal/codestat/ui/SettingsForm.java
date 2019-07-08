package com.alikemal.codestat.ui;

import com.alikemal.codestat.StatsCollector;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.alikemal.codestat.Config.*;

/**
 * Created by nicd on 02/06/16.
 */
public class SettingsForm implements Configurable {


    private JPanel ui;
    private JTextField apiKey;
    private JTextField apiURL;
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

        if (persistedApiURL.equals("")) {
            persistedApiURL = DEFAULT_API_URL;
        }

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
    public void apply() {
        persistedApiKey = apiKey.getText();
        persistedApiURL = apiURL.getText();

        propertiesComponent.setValue(API_KEY_NAME, persistedApiKey);
        propertiesComponent.setValue(API_URL_NAME, persistedApiURL);

        ApplicationManager.getApplication().getComponent(StatsCollector.class);
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
