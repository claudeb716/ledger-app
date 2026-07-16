package com.pluralsight;

import java.io.*;
import java.time.LocalDate;
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);

    // Added: thrown by readInput() when the user types 0, so it can bubble up through
    // whatever menu or prompt they're in, back to main(), which catches it and quits.
    private static class QuitException extends RuntimeException {}

    // Added: thrown by readInput() when the user types H. It bubbles up to main(), which
    // catches it and redisplays the home menu - so H returns home from ANY menu or prompt.
    private static class HomeException extends RuntimeException {}

    // Added: every prompt reads through this instead of scanner.nextLine().
    // Typing 0 anywhere quits the app; typing H anywhere jumps back to the home menu.
    private static String readInput(Scanner scanner) {
        String line = scanner.nextLine().trim();
        if (line.equals("0")) {
            throw new QuitException();
        }
        if (line.equalsIgnoreCase("H")) {
            throw new HomeException();
        }
        return line;
    }

    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner myScanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            // Changed: the try/catch lives INSIDE the loop now. A HomeException from anywhere
            // below lands here and simply redisplays this (home) menu; a QuitException exits.
            try {
                System.out.println("Welcome to TransactionApp");
                System.out.println("Choose an option:");
                System.out.println("D) Add Deposit");
                System.out.println("P) Make Payment (Debit)");
                System.out.println("L) Ledger");
                System.out.println("H) Home   (returns here from any menu)");
                System.out.println("0 or X) Exit");

                String input = readInput(myScanner);

                switch (input.toUpperCase()) {
                    case "D" -> addDeposit(myScanner);
                    case "P" -> addPayment(myScanner);
                    case "L" -> ledgerMenu(myScanner);
                    case "X" -> running = false;
                    default -> System.out.println("Invalid option");
                }
            } catch (HomeException e) {
                // already home - just loop and show the main menu again
            } catch (QuitException e) {
                running = false;
            }
        }

        System.out.println("Goodbye!");
        myScanner.close();
    }

    // Changed: if the file doesn't exist yet, create an empty one instead of erroring out.
    // Same FILE_NAME and same pipe format as before - only the "missing file" behavior changed.
    public static void loadTransactions(String fileName) {
        File transactionFile = new File(fileName);
        try {
            if (!transactionFile.exists()) {
                transactionFile.createNewFile();   // start with an empty ledger
                return;
            }

            BufferedReader tr = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = tr.readLine()) != null) {
                String[] parts = line.split("\\|");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                String description = parts[2].trim();
                String vendor = parts[3].trim();
                double amount = Double.parseDouble(parts[4].trim());
                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            tr.close();
        } catch (Exception e) {
            System.err.println("Error loading file " + e.getMessage());
        }
    }

    // Changed: added full input validation - re-prompts until every field is valid.
    private static void addDeposit(Scanner scanner) {
        System.out.println("--- Add Deposit --- (0 = quit, H = home, at any prompt)");

        // Keep asking until BOTH a valid date and time are entered
        LocalDate date = null;
        LocalTime time = null;
        while (date == null || time == null) {
            System.out.println("Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTime = readInput(scanner);
            try {
                String[] parts = dateTime.split(" ");
                date = LocalDate.parse(parts[0], DATE_FMT);
                time = LocalTime.parse(parts[1], TIME_FMT);
            } catch (Exception e) {
                System.out.println("Invalid format. Please use yyyy-MM-dd HH:mm:ss");
            }
        }

        // Description cannot be blank
        String description = "";
        while (description.isEmpty()) {
            System.out.println("Enter Description: ");
            description = readInput(scanner);
            if (description.isEmpty()) System.out.println("Description cannot be empty.");
        }

        // Vendor cannot be blank
        String vendor = "";
        while (vendor.isEmpty()) {
            System.out.println("Enter Vendor: ");
            vendor = readInput(scanner);
            if (vendor.isEmpty()) System.out.println("Vendor cannot be empty.");
        }

        // Amount must be a positive number
        double amount = 0;
        boolean validAmount = false;
        while (!validAmount) {
            System.out.println("Enter amount to deposit (positive): ");
            try {
                amount = Double.parseDouble(readInput(scanner));
                if (amount <= 0) System.out.println("Amount must be positive.");
                else validAmount = true;
            } catch (Exception e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }

        transactions.add(new Transaction(date, time, description, vendor, amount));
        saveTransaction(date, time, description, vendor, amount);
        System.out.println("Deposit Added!");
    }

    // Changed: added the same validation as addDeposit, PLUS the key fix - the amount is
    // flipped to NEGATIVE before it's stored so payments show up under "Payments".
    private static void addPayment(Scanner scanner) {
        System.out.println("--- Make Payment --- (0 = quit, H = home, at any prompt)");

        LocalDate date = null;
        LocalTime time = null;
        while (date == null || time == null) {
            System.out.println("Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTime = readInput(scanner);
            try {
                String[] parts = dateTime.split(" ");
                date = LocalDate.parse(parts[0], DATE_FMT);
                time = LocalTime.parse(parts[1], TIME_FMT);
            } catch (Exception e) {
                System.out.println("Invalid format. Please use yyyy-MM-dd HH:mm:ss");
            }
        }

        String description = "";
        while (description.isEmpty()) {
            System.out.println("Enter Description: ");
            description = readInput(scanner);
            if (description.isEmpty()) System.out.println("Description cannot be empty.");
        }

        String vendor = "";
        while (vendor.isEmpty()) {
            System.out.println("Enter Vendor: ");
            vendor = readInput(scanner);
            if (vendor.isEmpty()) System.out.println("Vendor cannot be empty.");
        }

        double amount = 0;
        boolean validAmount = false;
        while (!validAmount) {
            System.out.println("Enter amount to pay (positive): ");
            try {
                amount = Double.parseDouble(readInput(scanner));
                if (amount <= 0) System.out.println("Amount must be positive.");
                else validAmount = true;
            } catch (Exception e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        }

        amount = -amount;   // key fix: store payments as negative

        transactions.add(new Transaction(date, time, description, vendor, amount));
        saveTransaction(date, time, description, vendor, amount);
        System.out.println("Payment Added!");
    }

    // Added: one shared helper that appends to the SAME file (transactions.csv) in the SAME
    // pipe format. addDeposit and addPayment used to duplicate this block.
    private static void saveTransaction(LocalDate date, LocalTime time, String description, String vendor, double amount) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true));
            bw.write(String.format("%s|%s|%s|%s|%.2f", date, time, description, vendor, amount));
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.err.println("Error saving transaction");
        }
    }

    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            // Changed: newest-first ordering; ties on the same day are broken by time
            transactions.sort(Comparator.comparing(Transaction::getDate)
                                        .thenComparing(Transaction::getTime)
                                        .reversed());

            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.println("0) Quit");

            // Note: H (home) and 0 (quit) are handled globally by readInput, so they don't
            // need their own cases here - they unwind straight past this menu.
            String input = readInput(scanner);
            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static void displayLedger() {
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    // Changed: collect all deposits, then report "none" only once if the list is empty
    private static void displayDeposits() {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() > 0) {
                System.out.println(transaction);
                found = true;
            }
        }
        if (!found) System.out.println("No Deposits found.");
    }

    private static void displayPayments() {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getAmount() < 0) {
                System.out.println(transaction);
                found = true;
            }
        }
        if (!found) System.out.println("No Payments found.");
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
            System.out.println("B) Back");
            System.out.println("H) Home");
            System.out.println("0) Quit");

            // H (home) and 0 (quit) are handled globally by readInput; B steps back one level.
            String input = readInput(scanner);

            LocalDate theEnd = LocalDate.now();
            switch (input.toUpperCase()) {
                case "1" -> {
                    LocalDate start = theEnd.withDayOfMonth(1);
                    filterTransactionsByDate(start, theEnd);
                }
                case "2" -> {
                    // Changed: use the ACTUAL previous month (old code always jumped to January)
                    LocalDate previousMonth = theEnd.minusMonths(1).withDayOfMonth(1);
                    LocalDate end2 = theEnd.withDayOfMonth(1).minusDays(1);
                    filterTransactionsByDate(previousMonth, end2);
                }
                case "3" -> {
                    LocalDate yearStart = theEnd.withDayOfYear(1);
                    filterTransactionsByDate(yearStart, theEnd);
                }
                case "4" -> {
                    LocalDate previousYear = theEnd.minusYears(1).withDayOfYear(1);
                    LocalDate end3 = theEnd.minusYears(1).withMonth(12).withDayOfMonth(31);
                    filterTransactionsByDate(previousYear, end3);
                }
                case "5" -> {
                    System.out.println("Enter Vendor name");
                    String venName = readInput(scanner);
                    filterTransactionsByVendor(venName);
                }
                case "6" -> customSearch(scanner);
                case "B" -> running = false;   // Back to the Ledger menu (H = all the way home)
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (!transaction.getDate().isBefore(start) && !transaction.getDate().isAfter(end)) {
                System.out.println(transaction);
                found = true;
            }
        }
        if (!found) System.out.println("No Reports Found!");
    }

    private static void filterTransactionsByVendor(String vendor) {
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.getVendor().equalsIgnoreCase(vendor)) {
                System.out.println(transaction);
                found = true;
            }
        }
        if (!found) System.out.println("No transactions found for: " + vendor);
    }

    // Added: the category search. Any field left blank is skipped, so users can filter by
    // one category or stack several to isolate an exact slice of transactions.
    private static void customSearch(Scanner scanner) {
        System.out.println("Custom Search (blank = skip a field, 0 = quit, H = home)");

        System.out.println("Start Date (yyyy-MM-dd): ");
        LocalDate startDate = parseDate(readInput(scanner));

        System.out.println("End Date (yyyy-MM-dd): ");
        LocalDate endDate = parseDate(readInput(scanner));

        System.out.println("Description: ");
        String describe = readInput(scanner);

        System.out.println("Vendor: ");
        String vendorName = readInput(scanner);

        System.out.println("Amount: ");
        Double amount = parseDouble(readInput(scanner));

        boolean found = false;
        for (Transaction transaction : transactions) {
            // Each filter only applies if the user actually entered a value for it
            if (startDate != null && transaction.getDate().isBefore(startDate)) continue;
            if (endDate != null && transaction.getDate().isAfter(endDate)) continue;
            if (!describe.isEmpty() && !transaction.getDescription().toLowerCase().contains(describe.toLowerCase())) continue;
            if (!vendorName.isEmpty() && !transaction.getVendor().equalsIgnoreCase(vendorName)) continue;
            if (amount != null && Double.compare(transaction.getAmount(), amount) != 0) continue;

            System.out.println(transaction);
            found = true;
        }

        // Descriptive "nothing found" message that echoes what was searched for
        if (!found) {
            String message = "No transactions found";
            if (!describe.isEmpty()) message += " with description \"" + describe + "\"";
            if (!vendorName.isEmpty()) message += " for vendor \"" + vendorName + "\"";
            if (startDate != null) message += " from " + startDate;
            if (endDate != null) message += " to " + endDate;
            if (amount != null) message += " with amount " + amount;
            System.out.println(message + ".");
        }
    }

    // Changed: returns null when the field is blank or unparseable, which is how the
    // custom search knows to skip that filter.
    private static LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s.trim(), DATE_FMT);
        } catch (Exception e) {
            System.out.println("Invalid date format, ignoring that filter.");
            return null;
        }
    }

    // Changed: same idea for amounts - blank/invalid returns null so the filter is skipped.
    private static Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            System.out.println("Invalid amount format, ignoring that filter.");
            return null;
        }
    }
}
