package com.pluralsight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

private static final String BOLD = "\u001B[1m";
    
//    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner myScanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
        printTitle("TRANSACTION APP");

        System.out.println(GREEN + "D)" + RESET + " Add Deposit");
        System.out.println(RED + "P)" + RESET + " Make Payment (Debit)");
        System.out.println(BLUE + "L)" + RESET + " Ledger");
        System.out.println(YELLOW + "X)" + RESET + " Exit");
        System.out.print(PURPLE + "\nChoose an option: " + RESET);

            String input = myScanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(myScanner);
                case "P" -> addPayment(myScanner);
                case "L" -> ledgerMenu(myScanner);
                case "X" -> running = false;
                default -> System.out.println(RED + BOLD + "\n✗ Invalid option" + RESET);
            }
        }
        myScanner.close();
    }

    public static void loadTransactions(String fileName) {
        File transactionFile = new File(fileName);
        if (!transactionFile.exists()) {
            System.out.println("Error: " + fileName + "was not found");
            return;
        }

        try {

            BufferedReader tr = new BufferedReader(new FileReader(fileName));

            String line;

            while ((line = tr.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                String description = parts[2].trim();
                String vendor = parts[3].trim();
                Double amount = parseDouble(parts[4].trim());
                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            tr.close();
        } catch (Exception e) {
            System.err.println("Error loading file " + e.getMessage());
        }
    }
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
            System.out.println(GREEN + BOLD + "\n✓ Deposit Added!" + RESET);
        } catch (IOException e) {
            System.out.println(
        RED + BOLD + "\n✗ Error saving transaction." + RESET
);
        }
    }
    private static void addPayment(Scanner scanner) {
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
            System.out.println(GREEN + BOLD + "\n✓ Payment Added!" + RESET);
        } catch (IOException e) {
            System.err.println("Error");
        }
    }
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            printTitle("LEDGER");

        System.out.println(CYAN + "A)" + RESET + " All");
        System.out.println(GREEN + "D)" + RESET + " Deposits");
        System.out.println(RED + "P)" + RESET + " Payments");
        System.out.println(PURPLE + "R)" + RESET + " Reports");
        System.out.println(YELLOW + "H)" + RESET + " Home");

        System.out.print(BLUE + "\nChoose an option: " + RESET);

            String input = scanner.nextLine().trim();
            transactions.sort(Comparator.comparing(Transaction::getDate));
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

    private static void displayLedger() {
    printTitle("ALL TRANSACTIONS");

    if (transactions.isEmpty()) {
        System.out.println(
                YELLOW + "No transactions found." + RESET
        );
        return;
    }

    printTransactionTableHeader();

    for (Transaction transaction : transactions) {
        printTransactionRow(transaction);
    }

    printTransactionTableFooter();
    }

    private static void displayDeposits() {
    boolean found = false;

    printTitle("DEPOSITS");
    printTransactionTableHeader();

    for (Transaction transaction : transactions) {
        if (transaction.getAmount() > 0) {
            printTransactionRow(transaction);
            found = true;
        }
    }

    printTransactionTableFooter();

    if (!found) {
        System.out.println(
                YELLOW + "No deposits found." + RESET
        );
    }
}
    
private static void displayPayments() {
    boolean found = false;

    printTitle("PAYMENTS");
    printTransactionTableHeader();

    for (Transaction transaction : transactions) {
        if (transaction.getAmount() < 0) {
            printTransactionRow(transaction);
            found = true;
        }
    }

    printTransactionTableFooter();

    if (!found) {
        System.out.println(
                YELLOW + "No payments found." + RESET
        );
    }
}

    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
        printTitle("REPORTS");

        System.out.println(CYAN + "1)" + RESET + " Month To Date");
        System.out.println(CYAN + "2)" + RESET + " Previous Month");
        System.out.println(CYAN + "3)" + RESET + " Year To Date");
        System.out.println(CYAN + "4)" + RESET + " Previous Year");
        System.out.println(PURPLE + "5)" + RESET + " Search by Vendor");
        System.out.println(PURPLE + "6)" + RESET + " Custom Search");
        System.out.println(YELLOW + "0)" + RESET + " Back");

        System.out.print(BLUE + "\nChoose an option: " + RESET);

            String input = scanner.nextLine().trim();

            LocalDate theEnd = LocalDate.now();
            switch (input) {
                case "1" -> {
                    LocalDate start = theEnd.withDayOfMonth(1);
                    filterTransactionsByDate(start, theEnd);
                }
                case "2" -> {
                    LocalDate previousMonth = theEnd.withDayOfMonth(1).withMonth(1);
                    LocalDate end2 = theEnd.withDayOfMonth(1).minusDays(1);
                    filterTransactionsByDate(previousMonth, end2);
                }
                case "3" -> {
                    LocalDate yearStart = theEnd.withDayOfYear(1);
                    filterTransactionsByDate(yearStart,theEnd);
                }
                case "4" -> {
                    LocalDate previousYear = theEnd.minusYears(1).withDayOfYear(1);
                    LocalDate end3 = theEnd.minusYears(1).withMonth(12).withDayOfMonth(31);
                    filterTransactionsByDate(previousYear, end3);
                }
                case "5" -> {
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

    private static void filterTransactionsByDate(
        LocalDate start,
        LocalDate end
) {
    boolean found = false;

    printTitle("TRANSACTION REPORT");
    printTransactionTableHeader();

    for (Transaction transaction : transactions) {
        if (!transaction.getDate().isBefore(start)
                && !transaction.getDate().isAfter(end)) {

            printTransactionRow(transaction);
            found = true;
        }
    }

    printTransactionTableFooter();

    if (!found) {
        System.out.println(
                YELLOW + "No reports found." + RESET
        );
    }
}

    private static void filterTransactionsByVendor(String vendor) {
    boolean found = false;

    printTitle("VENDOR SEARCH: " + vendor.toUpperCase());
    printTransactionTableHeader();

    for (Transaction transaction : transactions) {
        if (transaction.getVendor().equalsIgnoreCase(vendor)) {
            printTransactionRow(transaction);
            found = true;
        }
    }

    printTransactionTableFooter();

    if (!found) {
        System.out.println(
                YELLOW +
                "No transactions found for: " +
                vendor +
                RESET
        );
    }
}

    private static void customSearch(Scanner scanner) {
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


        }
    }
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble( String amount) {
        double finalAmount = Double.parseDouble(amount);
        if (finalAmount > 0) {
                return finalAmount;
            } else if (finalAmount < 0) {
                return finalAmount;
            }else {
                return finalAmount;
            }
    }
    private static void printTitle(String title) {
    System.out.println();
    System.out.println(CYAN + BOLD +
            "============================================================");
    System.out.printf("%30s%n", title);
    System.out.println(
            "============================================================"
            + RESET);
}
private static void printTransactionTableHeader() {
    System.out.println(
            CYAN +
            "+------------+----------+----------------------+----------------------+-------------+"
            + RESET
    );

    System.out.printf(
            BOLD + CYAN +
            "| %-10s | %-8s | %-20s | %-20s | %11s |%n"
            + RESET,
            "DATE",
            "TIME",
            "DESCRIPTION",
            "VENDOR",
            "AMOUNT"
    );

    System.out.println(
            CYAN +
            "+------------+----------+----------------------+----------------------+-------------+"
            + RESET
    );
}
private static void printTransactionTableFooter() {
    System.out.println(
            CYAN +
            "+------------+----------+----------------------+----------------------+-------------+"
            + RESET
    );
}
private static void printTransactionRow(Transaction transaction) {
    String amountColor;

    if (transaction.getAmount() >= 0) {
        amountColor = GREEN;
    } else {
        amountColor = RED;
    }

    System.out.printf(
            "| %-10s | %-8s | %-20s | %-20s | "
                    + amountColor + "%11s" + RESET + " |%n",
            transaction.getDate(),
            transaction.getTime(),
            shortenText(transaction.getDescription(), 20),
            shortenText(transaction.getVendor(), 20),
            String.format("$%,.2f", transaction.getAmount())
    );
}
private static String shortenText(String text, int maximumLength) {
    if (text.length() <= maximumLength) {
        return text;
    }

    return text.substring(0, maximumLength - 3) + "...";
}
}
