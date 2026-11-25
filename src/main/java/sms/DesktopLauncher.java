package sms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DesktopLauncher {

    public static Process launchDesktopWindow(String url, int width, int height) {
        String edgePath1 = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";
        String edgePath2 = "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe";
        String chromePath1 = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
        String chromePath2 = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";

        String browserExe = null;
        if (new File(edgePath1).exists()) browserExe = edgePath1;
        else if (new File(edgePath2).exists()) browserExe = edgePath2;
        else if (new File(chromePath1).exists()) browserExe = chromePath1;
        else if (new File(chromePath2).exists()) browserExe = chromePath2;

        if (browserExe != null) {
            try {
                File userDataDir = new File(System.getProperty("user.home"), ".sms_desktop_profile");
                if (!userDataDir.exists()) userDataDir.mkdirs();

                List<String> cmd = new ArrayList<>();
                cmd.add(browserExe);
                cmd.add("--app=" + url);
                cmd.add("--window-size=" + width + "," + height);
                cmd.add("--user-data-dir=" + userDataDir.getAbsolutePath());
                cmd.add("--no-first-run");
                cmd.add("--no-default-browser-check");
                cmd.add("--disable-extensions");

                ProcessBuilder pb = new ProcessBuilder(cmd);
                return pb.start();
            } catch (IOException e) {
                System.err.println("Failed to launch desktop app window: " + e.getMessage());
            }
        }

        // Fallback: Default system desktop browser
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            }
        } catch (Exception ex) {
            System.err.println("Could not open desktop window automatically: " + ex.getMessage());
        }
        return null;
    }
}
