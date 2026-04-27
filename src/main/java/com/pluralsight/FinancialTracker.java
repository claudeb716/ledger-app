package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner myScanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = myScanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(myScanner);
                case "P" -> addPayment(myScanner);
                case "L" -> ledgerMenu(myScanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        myScanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {
        // TODO: create file if it does not exist, then read each line,
        //       parse the five fields, build a Transaction object,
        //       and add it to the transactions list.


        String line;

        try {
            BufferedReader tr = new BufferedReader(new FileReader(fileName));
            while ((line = tr.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);
                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            tr.close();
        } catch (Exception e) {
            System.err.println("Error");
        }
    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        // TODO
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));

            System.out.println("Add Date(yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            System.out.println("Add Time(HH:mm:ss): ");
            LocalTime time = LocalTime.parse(scanner.nextLine());
            System.out.println("Add Description: ");
            String describe = scanner.nextLine();
            System.out.println("Add Vendor: ");
            String ven = scanner.nextLine();
            System.out.println("Add Amount(Positive): ");
            double price = scanner.nextDouble();
            scanner.nextLine();
            Transaction newDeposit = new Transaction(date, time, describe, ven, price);
            String line = String.format("%s|%s|%s|%s|%.2f",
                    newDeposit.getDate().format(DATE_FMT), newDeposit.getTime().format(TIME_FMT),
                    newDeposit.getDescription(), newDeposit.getVendor(), newDeposit.getAmount());
            bw.write(line);
            bw.newLine();
            bw.close();
            System.out.println("Deposit Added!");
        } catch (IOException e) {
            System.err.println("Error");
        }
    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
    private static void addPayment(Scanner scanner) {
        // TODO
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));

            System.out.println("Add Date(yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            System.out.println("Add Time(HH:mm:ss): ");
            LocalTime time = LocalTime.parse(scanner.nextLine());
            System.out.println("Add Description: ");
            String describe = scanner.nextLine();
            System.out.println("Add Vendor: ");
            String ven = scanner.nextLine();
            System.out.println("Add Amount(Positive): ");
            double price = -1 * scanner.nextDouble();
            scanner.nextLine();
            Transaction newDeposit = new Transaction(date, time, describe, ven, price);
            String line = String.format("%s|%s|%s|%s|%.2f",
                    newDeposit.getDate().format(DATE_FMT), newDeposit.getTime().format(TIME_FMT),
                    newDeposit.getDescription(), newDeposit.getVendor(), newDeposit.getAmount());
            bw.write(line);
            bw.newLine();
            bw.close();
            System.out.println("Payment Added!");
        } catch (IOException e) {
            System.err.println("Error");
        }
    }

    /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void displayLedger() {
        /* TODO – print all transactions in column format */
        for (Transaction transaction : transactions) {
            System.out.printf("%-10s | %-8s | %-20s | %-15s | $%.2f%n",
                    transaction.getDate().format(DATE_FMT), transaction.getTime().format(TIME_FMT),
                    transaction.getDescription(), transaction.getVendor(), transaction.getAmount());
        }
    }
    private static void displayDeposits() {
        /* TODO – only amount > 0               */
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0){
                System.out.printf("%-10s | %-8s | %-20s | %-15s | $%.2f%n",
                        transaction.getDate().format(DATE_FMT), transaction.getTime().format(TIME_FMT),
                        transaction.getDescription(), transaction.getVendor(), transaction.getAmount());
            }
        }
    }
    private static void displayPayments() {
        /* TODO – only amount < 0               */
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                System.out.printf("%-10s | %-8s | %-20s | %-15s | $%.2f%n",
                        transaction.getDate(), transaction.getTime(),
                        transaction.getDescription(), transaction.getVendor(), transaction.getAmount());
            }

        }
    }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            LocalDate end = LocalDate.now();

            switch (input) {
                case "1" -> {
                    /* TODO – month-to-date report */
                    filterTransactionsByDate(start, end);
                }
                case "2" -> {
                    /* TODO – previous month report */
                }
                case "3" -> {/* TODO – year-to-date report   */ }
                case "4" -> {/* TODO – previous year report  */ }
                case "5" -> {
                    /* TODO – prompt for vendor then report */
                    filterTransactionsByVendor(FILE_NAME);
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
        for (Transaction transaction : transactions){
        if (!transaction.getDate().isBefore(start) && !transaction.getDate().isAfter(end)){
            System.out.printf("%-10s | %-8s | %-20s | %-15s | $%.2f%n",
                    transaction.getDate(), transaction.getTime(),
                    transaction.getDescription(), transaction.getVendor(), transaction.getAmount());
        }

        }


    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }
}
