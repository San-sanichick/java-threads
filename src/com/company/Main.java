package com.company;

import com.google.gson.Gson;

import javax.crypto.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static class ApiRunnable implements Runnable {
        private volatile Todo todo;
        private final int id;

        public ApiRunnable(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("https://jsonplaceholder.typicode.com/todos/" + id);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int status = connection.getResponseCode();

                if (status == 200) {
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

                    Gson gson = new Gson();

                    todo = gson.fromJson(reader, Todo.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Todo getTodo() {
            return todo;
        }
    }

    static class TodoEncryptorRunnable implements Runnable {
        private final int id;
//        private static int callsTotal = 0;
        public static final AtomicInteger callsTotal      = new AtomicInteger(0);
        public static final AtomicInteger encryptionTotal = new AtomicInteger(0);

        public TodoEncryptorRunnable(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            Todo todo = null;

            ApiRunnable r = new ApiRunnable(id);
            Thread t = new Thread(r);
            t.start();

            try {
                t.join();
//                callsTotal++;
                callsTotal.incrementAndGet();
                todo = r.getTodo();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (todo != null) {
                System.out.println(todo);

                FileWriterSync fileWriterSync = FileWriterSync.getInstance();
                try {
                    fileWriterSync.writeFile("todos.txt", String.valueOf(todo));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    SecretKey secretKey = KeyGenerator.getInstance("DES").generateKey();
                    Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

                    byte[] todoBytes = todo.toString().getBytes();
                    byte[] encryptedTodo = cipher.doFinal(todoBytes);

                    fileWriterSync.writeFile("todos_enc.txt", encryptedTodo);

                    encryptionTotal.incrementAndGet();

                } catch (NoSuchAlgorithmException |
                        NoSuchPaddingException |
                        InvalidKeyException |
                        IllegalBlockSizeException |
                        BadPaddingException |
                        IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class CustomCanvas extends Canvas {
        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.drawLine(20, 20, 200, 150);
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

        for (int i = 1; i < 6; i++) {
            executor.execute(new TodoEncryptorRunnable(i));
        }

        executor.shutdown();

//        JFrame frame = new JFrame("Window");
//        frame.setSize(550, 550);
//        frame.add(new CustomCanvas());
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
