package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriterSync {
    private static final FileWriterSync instance = new FileWriterSync();

    private FileWriterSync() {
        super();
    }

    public synchronized void writeFile(String path, String data) throws IOException {

        if (!new File(path).isFile()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            writer.write(data);
            writer.newLine();
            writer.close();
        } else {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));

            writer.write(data);
            writer.newLine();
            writer.close();
        }
    }

    public synchronized  void writeFile(String path, byte[] data) throws IOException {
        if (!new File(path).isFile()) {
            Files.write(Paths.get(path), data);
        } else {
            Files.write(Paths.get(path), data, StandardOpenOption.APPEND);
        }
    }

    public static FileWriterSync getInstance() {
        return instance;
    }
}
