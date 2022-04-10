package com.company;

public class Todo {
    public int userId;
    public int id;
    public String title;
    public boolean completed;

    @Override
    public String toString() {
        return "" +
                "userIdL: " + userId +
                ", id: " + id +
                ", title: '" + title + '\'' +
                ", completed: " + completed;
    }
}
