package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class FinancialTracker {

    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
//    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        loadTransactions(FILE_NAME); // Branch test - chris

        Scanner myScanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("R) Print Receipt");
            System.out.println("X) Exit");

            String input = myScanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(myScanner);
                case "P" -> addPayment(myScanner);
                case "L" -> ledgerMenu(myScanner);
                case "R" -> printReceipt();
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
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
        try {
            String dateInput = readInput(scanner, "Add Date(yyyy-MM-dd)");
            if (isCancel(dateInput)) { cancelEntry(); return; }
            LocalDate date = LocalDate.parse(dateInput);

            String timeInput = readInput(scanner, "Add Time(HH:mm:ss)");
            if (isCancel(timeInput)) { cancelEntry(); return; }
            LocalTime time = LocalTime.parse(timeInput);

            String describe = readInput(scanner, "Add Description");
            if (isCancel(describe)) { cancelEntry(); return; }

            String ven = readInput(scanner, "Add Vendor");
            if (isCancel(ven)) { cancelEntry(); return; }

            String price = readInput(scanner, "Add Amount(Positive)");
            if (isCancel(price)) { cancelEntry(); return; }
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
    private static void addPayment(Scanner scanner) {
        try {
            String dateInput = readInput(scanner, "Add Date(yyyy-MM-dd)");
            if (isCancel(dateInput)) { cancelEntry(); return; }
            LocalDate date = LocalDate.parse(dateInput);

            String timeInput = readInput(scanner, "Add Time(HH:mm:ss)");
            if (isCancel(timeInput)) { cancelEntry(); return; }
            LocalTime time = LocalTime.parse(timeInput);

            String describe = readInput(scanner, "Add Description");
            if (isCancel(describe)) { cancelEntry(); return; }

            String ven = readInput(scanner, "Add Vendor");
            if (isCancel(ven)) { cancelEntry(); return; }

            String price = readInput(scanner, "Add Amount(Positive)");
            if (isCancel(price)) { cancelEntry(); return; }
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
    private static String readInput(Scanner scanner, String prompt) {
        System.out.println(prompt + " (C to cancel): ");
        return scanner.nextLine().trim();
    }
    private static boolean isCancel(String input) {
        return input.equalsIgnoreCase("C") || input.equalsIgnoreCase("CANCEL");
    }
    private static void cancelEntry() {
        System.out.println("Entry cancelled. Returning to menu.");
    }
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
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }
    private static void displayDeposits() {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0){
                System.out.println(transaction);
                found =true;
            }
            if (!found){
                System.out.println("No Deposits found.");
            }
        }
    }
    private static void displayPayments() {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                System.out.println(transaction);
                found = true;
            }
        }   if (!found){
            System.out.println("No Payments found.");
        }
    }

    private static void printReceipt() {
        StringBuilder receipt = new StringBuilder();
        String headerLine = String.format("%-12s %-30s %12s", "Date", "Description", "Amount");
        String separator = "-".repeat(headerLine.length());

        receipt.append("RECEIPT").append(System.lineSeparator());
        receipt.append(separator).append(System.lineSeparator());
        receipt.append(headerLine).append(System.lineSeparator());
        receipt.append(separator).append(System.lineSeparator());

        double total = 0;
        for (Transaction transaction : transactions) {
            receipt.append(String.format("%-12s %-30s %12.2f%n",
                    transaction.getDate(), transaction.getDescription(), transaction.getAmount()));
            total += transaction.getAmount();
        }

        receipt.append(separator).append(System.lineSeparator());
        receipt.append(String.format("%-12s %-30s %12.2f%n", "", "TOTAL", total));

        System.out.println(receipt);

        writeReceiptToExcel(total);
        writeReceiptToText(total);
    }

    private static void writeReceiptToExcel(double total) {
        File receiptsFolder = new File("Receipts");
        if (!receiptsFolder.exists()) {
            receiptsFolder.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        File receiptFile = new File(receiptsFolder, "receipt_" + timestamp + ".xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Receipt");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Date", "Description", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getDate().toString());
                row.createCell(1).setCellValue(transaction.getDescription());
                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(transaction.getAmount());
                amountCell.setCellStyle(amountStyle);
            }

            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(1).setCellValue("TOTAL");
            Cell totalCell = totalRow.createCell(2);
            totalCell.setCellValue(total);
            totalCell.setCellStyle(amountStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(receiptFile)) {
                workbook.write(fos);
            }
            System.out.println("Receipt saved to " + receiptFile.getPath());
        } catch (IOException e) {
            System.err.println("Error writing receipt to Excel: " + e.getMessage());
        }
    }

    private static void writeReceiptToText(double total) {
        File receiptsFolder = new File("Receipts");
        if (!receiptsFolder.exists()) {
            receiptsFolder.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
        File receiptFile = new File(receiptsFolder, "receipt_" + timestamp + ".txt");

        int width = 50;
        String divider = "=".repeat(width);
        String subDivider = "-".repeat(width);
        String title = "LEDGER RECEIPT";
        String centeredTitle = " ".repeat(Math.max(0, (width - title.length()) / 2)) + title;

        StringBuilder text = new StringBuilder();
        text.append(divider).append("\n");
        text.append(centeredTitle).append("\n");
        text.append(divider).append("\n");

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            text.append(appendReceiptLine(transaction.getDate().toString(), transaction.getDescription(), transaction.getAmount(), width));
            if (i < transactions.size() - 1) {
                text.append(subDivider).append("\n");
            }
        }

        text.append(divider).append("\n");
        text.append("Total: ").append(formatAmount(total)).append("\n");

        try (FileWriter writer = new FileWriter(receiptFile)) {
            writer.write(text.toString());
            System.out.println("Receipt saved to " + receiptFile.getPath());
        } catch (IOException e) {
            System.err.println("Error writing receipt to text file: " + e.getMessage());
        }
    }

    private static String appendReceiptLine(String date, String description, double amount, int width) {
        String amountText = formatAmount(amount);
        String padding = " ".repeat(Math.max(0, width - amountText.length()));

        StringBuilder line = new StringBuilder();
        line.append("Date: ").append(date).append("\n");
        line.append("Description: ").append(description).append("\n");
        line.append(padding).append(amountText).append("\n");
        return line.toString();
    }

    private static String formatAmount(double amount) {
        return (amount < 0 ? "-$" : "$") + String.format("%.2f", Math.abs(amount));
    }

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

    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
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
}
