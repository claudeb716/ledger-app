package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;


public class FinancialTracker {

    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

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

    public static void loadTransactions(String fileName) {
        // TODO: create file if it does not exist, then read each line,
        //       parse the five fields, build a Transaction object,
        //       and add it to the transactions list.




        try {
            File transactionFile = new File(fileName);
            transactionFile.createNewFile();
            BufferedReader tr = new BufferedReader(new FileReader(fileName));

            String line;


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


            System.out.println("Add Date(yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            System.out.println("Add Time(HH:mm:ss): ");
            LocalTime time = LocalTime.parse(scanner.nextLine());
            System.out.println("Add Description: ");
            String describe = scanner.nextLine();
            System.out.println("Add Vendor: ");
            String ven = scanner.nextLine();
            System.out.println("Add Amount(Positive): ");
            String price = scanner.nextLine();
            Double finalAmount = parseDouble(price);

            Transaction newDeposit = new Transaction(date, time, describe, ven, finalAmount);
            String line = String.format("%s|%s|%s|%s|%.2f",
                    newDeposit.getDate(), newDeposit.getTime(),
                    newDeposit.getDescription(), newDeposit.getVendor(), newDeposit.getAmount());

            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));
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
            System.out.println("Add Date(yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            System.out.println("Add Time(HH:mm:ss): ");
            LocalTime time = LocalTime.parse(scanner.nextLine());
            System.out.println("Add Description: ");
            String describe = scanner.nextLine();
            System.out.println("Add Vendor: ");
            String ven = scanner.nextLine();
            System.out.println("Add Amount(Positive): ");
            String price = scanner.nextLine();
            Double finalAmount = parseDouble(price);

            Transaction newDeposit = new Transaction(date, time, describe, ven, finalAmount);

            String line = String.format("%s|%s|%s|%s|%.2f",
                    newDeposit.getDate(), newDeposit.getTime(),
                    newDeposit.getDescription(), newDeposit.getVendor(), newDeposit.getAmount());

            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));
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
       ---------------------------------- -------------------------------- */
    private static void displayLedger() {
        /* TODO – print all transactions in column format */
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }
    private static void displayDeposits() {
        /* TODO – only amount > 0               */
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0){
                System.out.println(transaction);
            }
        }
    }
    private static void displayPayments() {
        /* TODO – only amount < 0               */
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                System.out.println(transaction);
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

            LocalDate today = LocalDate.now();
            LocalDate start = today.withDayOfMonth(1);
            LocalDate previousMonth = start.minusMonths(1);
            LocalDate end2 = start.minusDays(1);
            LocalDate yearStart = today.withDayOfYear(1);
            LocalDate previousYear =today.minusYears(1).withDayOfYear(1);
            LocalDate end3 = today.minusYears(1).withMonth(12).withDayOfMonth(31);

            switch (input) {
                case "1" -> {/* TODO – month-to-date report */
                    filterTransactionsByDate(start, today);
                }
                case "2" -> {/* TODO – previous month report */
                    filterTransactionsByDate(previousMonth, end2);
                }
                case "3" -> {/* TODO – year-to-date report   */
                    filterTransactionsByDate(yearStart,today);
                }
                case "4" -> {/* TODO – previous year report  */
                    filterTransactionsByDate(previousYear, end3);
                }
                case "5" -> {
                    /* TODO – prompt for vendor then report */
                    System.out.println("Enter Vendor name");
                    String venName = scanner.nextLine().trim();
                    filterTransactionsByVendor(venName);
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
        boolean found = false;
        for (Transaction transaction : transactions){
        if (!transaction.getDate().isBefore(start) && !transaction.getDate().isAfter(end)){
            System.out.println(transaction);
            found = true;
        }
        }   if (!found){
            System.out.println("No Reports Found!");
        }


    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getVendor().equalsIgnoreCase(vendor)){
                System.out.println(transaction);
                found = true;
            }
        }    if (!found){
            System.out.println("No transactions found for: " + vendor);
        }
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
        boolean running = true;
        while (running) {
            System.out.println("Custom Search");
            System.out.println("Start Date");
            String startDate = scanner.nextLine().trim();
            System.out.println("End Date");
            String endDate = scanner.nextLine().trim();
            System.out.println("Description");
            String describe = scanner.nextLine().trim();
            System.out.println("Vendor");
            String vendorName = scanner.nextLine().trim();
            System.out.println("Amount");
            Double price = scanner.nextDouble();
            String line = String.format(startDate,endDate,describe,vendorName,price);

        }

    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble( String amount) {
        /* TODO – return Double   or null */
        double finalAmount = Double.parseDouble(amount);
        if (finalAmount > 0) {
                return Math.abs(finalAmount);
            } else if (finalAmount < 0) {
                return Math.abs(finalAmount);
            }else {
                return Math.abs(finalAmount);
            }
    }
}
