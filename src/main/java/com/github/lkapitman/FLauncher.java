package com.github.lkapitman;

import com.github.lkapitman.files.FileManager;
import com.github.lkapitman.ui.PanelManager;
import com.github.lkapitman.ui.panels.PanelLogin;
import com.github.lkapitman.utils.Constants;
import fr.arinonia.arilibfx.updater.DownloadJob;
import fr.arinonia.arilibfx.updater.DownloadManager;
import fr.arinonia.arilibfx.updater.Updater;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

public class FLauncher {

    private static final FileManager fileManager = new FileManager(Constants.PROJECT_NAME);

    private PanelManager panelManager;
    private PanelLogin panelLogin;
    private static boolean downloaded;

    private static Properties settings = new Properties();

    private static ResourceBundle res = ResourceBundle.getBundle("data");

    public void init(Stage stage) {

        this.panelManager = new PanelManager(this, stage);
        this.panelManager.init();
        this.panelManager.showPanel(panelLogin = new PanelLogin());



    }

    public void launchGame() {
        Updater updater = new Updater();
        DownloadJob game = new DownloadJob("game", this.panelLogin.getHomePanel());
        game.setExecutorService(5);

        if (checkSettings() != 0) {
            if (checkSettings() == 1) {
                this.panelManager.getTrayIcon().displayMessage(Constants.PROJECT_NAME, res.getString("javafx.error.cant.write"), TrayIcon.MessageType.ERROR);
            }
            if (checkSettings() == 2) {
                this.panelManager.getTrayIcon().displayMessage(Constants.PROJECT_NAME, res.getString("javafx.error.cant.read"), TrayIcon.MessageType.ERROR);
            }
        } else {
            updater.addJobToDownload(new DownloadManager(settings.getProperty("GAME_URL"), game, fileManager.getSettingsFolder()));
            updater.setFileDeleter(true);
            Thread thread = new Thread(updater::start);
            thread.start();
        }
    }

    public static boolean isDownloaded() {
        return downloaded;
    }

    public static FileManager getFileManager() {
        return fileManager;
    }

    public static ResourceBundle getRes() {
        return res;
    }

    public int checkSettings() {
        if (fileManager.getSettingsFile().canRead()) {
            try {
                settings.load(new FileReader(fileManager.getSettingsFile()));
                if (fileManager.getSettingsFile().canWrite()) {
                    App.logger.log(res.getString("java.setting.not.found"));
                    settings.setProperty("GAME_URL", "http://localhost/instance.json");
                    settings.setProperty("SERVER_IP", "localhost");
                    settings.setProperty("AUTHENTICATOR", "VK_BOT");
                } else {
                    App.logger.warn(res.getString("java.error.write.config"));
                    return 1;
                }
            } catch (IOException e) {
                new File(fileManager.getSettingsFolder(), "launcher.properties");
                checkSettings();
                App.logger.warn(res.getString("java.error.cant.load.config"));
                App.logger.log(res.getString("java.info.create.config"));
            }
        }
        return 2;
    }


    public Properties getSettings() {
        return settings;
    }
}
