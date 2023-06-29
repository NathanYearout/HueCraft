package com.sudden.huecraft;


import com.sudden.huecraft.commands.StartCommand;
import com.sudden.huecraft.config.HueConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class APIManager {
    private static int status;
    private String ip;
    private String group;
    private String username;
    private String[] lightIDs;

    public APIManager(String ip, String group, String username) {
        this.ip = ip;
        this.group = group;
        this.username = username;
        status = 0;

        initializeConnection();
    }

    public static boolean isConnected() {
        return status == 3;
    }

    public boolean registerUser() {
        System.out.println("Attempting to register user...");
        if (status == 2) {
            username = doPost("http://" + ip + "/api");

            if (username.contains("\"username\":")) {
                username = username.substring(username.indexOf("username\":\"") + 11, username.lastIndexOf("\""));
                logUsername(username);

                if (group.equals("ALL")) {
                    String s = doGet("http://" + ip + "/api/" + username + "/lights");

                    int lightCount = ((s.length() - s.replace("{", "").length()) - 1) / 8;

                    lightIDs = new String[lightCount];

                    for (int i = 0; i < lightIDs.length; i++) {
                        lightIDs[i] = String.valueOf(i + 1);
                    }
                } else {
                    String s = doGet("http://" + ip + "/api/" + username + "/groups");

                    if (s.contains(group)) {
                        int startIndex = s.indexOf(group) + group.length();
                        String str = s.substring(s.indexOf("[", startIndex) + 1, s.indexOf("]", startIndex));
                        lightIDs = str.replace("\"", "").replace(" ", "").split(",");
                    }
                }

                status = 3;
                return true;
            } else if (username.contains("link button not pressed")) {
                System.out.println("Button has not yet been pressed!");
            }
        }

        return false;
    }

    public void lightSet(int bri, int hue, int sat, int rate) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lightIDs.length; i++) {
            try {
                System.out.println("Trying to update light level to " + bri);
                URL url = new URL("http://" + ip + "/api/" + username + "/lights/" + lightIDs[i] + "/state");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);

                // If bri is 0, turn off the light
                String isOn;
                if (bri > 0) {
                    isOn = "true";
                } else {
                    isOn = "true"; // Currently setting this to true because light gets choppy when turned off completely
                }

                byte[] out = ("{\"on\":"+isOn+",\"bri\":" + bri + ",\"hue\":" + hue + ",\"sat\":" + sat +
                        ",\"transitiontime\":" + rate + "}").getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                conn.setFixedLengthStreamingMode(length);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.connect();

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(out);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeConnection() {
        status = 1;

        String getResponse = doGet("http://" + ip + "/api/" + username);

        if (getResponse.contains("unauthorized user") || username.isEmpty()) {
            status = 2;
        } else if (getResponse.contains("\"lights\":")) {
            if (group.equals("ALL")) {
                String s = doGet("http://" + ip + "/api/" + username + "/lights");
                int lightCount = ((s.length() - s.replace("{", "").length()) - 1) / 8;

                lightIDs = new String[lightCount];

                for (int i = 0; i < lightIDs.length; i++) {
                    lightIDs[i] = String.valueOf(i + 1);
                }
            } else {
                String s = doGet("http://" + ip + "/api/" + username + "/groups");

                if (s.contains(group)) {
                    int startIndex = s.indexOf(group) + group.length();
                    String str = s.substring(s.indexOf("[", startIndex) + 1, s.indexOf("]", startIndex));
                    lightIDs = str.replace("\"", "").replace(" ", "").split(",");
                }
            }

            status = 3;
        } else {
            status = 4;
        }
    }

    private String doGet(String urlString) {
        StringBuilder result = new StringBuilder();
        System.out.println("Doing get request " + urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1500);
            conn.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            rd.close();

        } catch (SocketTimeoutException e) {
            StartCommand.errorMessage();
            System.out.println("Connection to hub timed out, likely bad IP!");
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Response: " + result);
        return result.toString();
    }

    private String doPost(String urlString) {
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            byte[] out = "{\"devicetype\":\"minecraft#huecraft\"}".getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            conn.setFixedLengthStreamingMode(length);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.connect();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Response: " + result);
        return result.toString();
    }

    private void logUsername(String username) {
        HueConfig.username.set(username);
    }

    public String toString() {
        String output;

        switch (status) {
            case 0:
                output = "Not Connected";
                break;
            case 1:
                output = "Initializing";
                break;
            case 2:
                output = "Press Button";
                break;
            case 3:
                output = "Ready";
                break;
            case 4:
                output = "Invalid IP";
                break;
            default:
                output = "Not Connected";
                break;
        }

        return output;
    }
}
