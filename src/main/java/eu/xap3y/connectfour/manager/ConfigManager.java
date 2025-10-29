package eu.xap3y.connectfour.manager;

import eu.xap3y.connectfour.ConnectFour;

public class ConfigManager {

    public static void reloadConfig() {
        if (!ConnectFour.getInstance().getDataFolder().exists()) {
            ConnectFour.getInstance().getDataFolder().mkdir();
        }

        ConnectFour.getInstance().saveDefaultConfig();
        ConnectFour.getInstance().reloadConfig();
    }
}
