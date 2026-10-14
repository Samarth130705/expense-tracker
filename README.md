# Expense Tracker (JavaFX + SQLite)

Features: add / edit / delete expenses, categories, monthly view with total,
pie chart (spending by category) and bar chart (monthly totals for the year).

## Requirements
- JDK 17+
- Maven 3.8+

## Run
```
cd expense-tracker
mvn javafx:run
```
The SQLite file `expenses.db` is created in the folder you run from.

## Structure
- `model/Expense.java`   – data class
- `db/Database.java`     – JDBC connection + schema
- `db/ExpenseDao.java`   – all SQL (CRUD, category totals, monthly totals)
- `App.java`             – JavaFX UI

## Ideas to extend
CSV export, budget limits per category, custom categories table, search/filter, MySQL support.
