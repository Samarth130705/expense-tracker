package com.expensetracker.model;

import java.time.LocalDate;

public class Expense {
    private int id;
    private LocalDate date;
    private String category;
    private String description;
    private double amount;

    public Expense(int id, LocalDate date, String category, String description, double amount) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
}
