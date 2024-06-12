package io.sim;

import java.io.*;

public class Out {
    public static PrintWriter writer;

    public Out(String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            Out.writer = new PrintWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeLine(String line) {
        writer.println(line);
        writer.flush(); 
    }
    public static void close() {
        writer.close();
    }
}