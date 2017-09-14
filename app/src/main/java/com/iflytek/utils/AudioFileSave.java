package com.iflytek.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class AudioFileSave {
    String mFilePath;
    FileOutputStream os;
    int startTime;
    public AudioFileSave(String filePath){
        startTime = (int) new Date().getTime();
        mFilePath = filePath;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int stopWrite(){
        int i = (int)(new Date().getTime() - startTime) / 1000;
        try {
            os.close();
            File file = new File(mFilePath);
            WavWriter writer = new WavWriter(file, 16000);
            writer.writeHeader();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    public void writeData(byte[] data){
        try {
            if(os != null)
                os.write(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void discardWrite(){
        stopWrite();
        File file = new File(mFilePath);
        if(file.exists())
            file.delete();
    }
    public String getFilePath(){
        return mFilePath;
    }
    
}
