package com.example.lamp;

import android.app.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Fileoperation extends Application {

    private String filePath;
    private String fileContent;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean updateContent() {
        try {
            readInfo();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean checkFileExists() {
        File f = new File(filePath);
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public void createEmptyFileContent() {
        this.fileContent = "{\"mac\":\"" + "null" + "\",\"time\":\"" + "null" + "\"}";
    }

    public void saveInfo() throws IOException {
        File file = new File(filePath);
        FileOutputStream output = new FileOutputStream(file);
        output.write(fileContent.getBytes());
        output.close();
    }

    public String readInfo() throws IOException {
        File file = new File(filePath);
        FileInputStream input = new FileInputStream(file);
        byte[] temp = new byte[1024];
        StringBuilder sb = new StringBuilder("");
        int len = 0;
        while ((len = input.read(temp)) > 0) {
            sb.append(new String(temp, 0, len));
        }
        input.close();
        fileContent = sb.toString();
        return fileContent;
    }

    public String getJsonValue(String key) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(fileContent);
            return jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return "null";
        }
    }

    public String modifyJsonContent(String key, String value) throws JSONException {
        JSONObject jsonObject = new JSONObject(fileContent);
        switch (key) {
            case "mac":
                String time = jsonObject.getString("time");
                fileContent = "{\"mac\":\"" + value + "\",\"time\":\"" + time + "\"}";
                break;
            case "time":
                String mac = jsonObject.getString("mac");
                fileContent = "{\"mac\":\"" + mac + "\",\"time\":\"" + value + "\"}";
                break;
        }

        return fileContent;
    }

}
