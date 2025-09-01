import java.util.*;
import java.io.*;
import java.nio.file.*;

class Book {
    int id;
    String title;
    String author;
    boolean isAvailable;

    Book(int id, String title, String author, boolean isAvailable) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return String.format("%-4d | %-30s | %-20s | %s",
                id, title, author, (isAvailable ? "Available" : "Issued"));
    }
}

class Library {
    private final List<Book> books = new ArrayList<>();
    private final Path dataPath;

    Library(Path dataPath) {
        this.dataPath = dataPath;
        loadFromFile();
    }

    // ----- Public operations -----
    public void addBook(String title, String author) {
        int nextId = books.stream().mapToInt(b -> b.id).max().orElse(0) + 1;
        books.add(new Book(nextId, title, author, true));
        System.out.println("✅ Book added with ID: " + nextId);
    }

    public void viewBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in library yet.");
            return;
        }
        System.out.println("\nID   | Title                          | Author               | Status");
        System.out.println("-----+--------------------------------+----------------------+--------");
        for (Book b : books) System.out.println(b);
    }

    public void searchBooks(String query) {
        String q = query.toLowerCase();
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.title.toLowerCase().contains(q) || b.author.toLowerCase().contains(q)) {
                results.add(b);
            }
        }
        if (results.isEmpty()) {
            System.out.println("No matching books found.");
        } else {
            System.out.println("\nSearch results:");
            for (Book b : results) System.out.println(b);
        }
    }

    public void issueBook(int id) {
        Book b = findById(id);
        if (b == null) {
            System.out.println("Invalid Book ID.");
            return;
        }
        if (!b.isAvailable) {
            System.out.println("This book is already issued.");
            return;
        }
        b.isAvailable = false;
        System.out.println("✅ Issued: " + b.title);
    }

    public void returnBook(int id) {
        Book b = findById(id);
        if (b == null) {
            System.out.println("Invalid Book ID.");
            return;
        }
        if (b.isAvailable) {
            System.out.println("This book was not issued.");
            return;
        }
        b.isAvailable = true;
        System.out.println("✅ Returned: " + b.title);
    }

    public void removeBook(int id) {
        Iterator<Book> it = books.iterator();
        while (it.hasNext()) {
            Book b = it.next();
            if (b.id == id) {
                it.remove();
                System.out.println("🗑️ Removed: " + b.title);
                return;
            }
        }
        System.out.println("Book ID not found.");
    }

    public void saveToFile() {
        try {
            if (Files.notExists(dataPath.getParent())) {
                Files.createDirectories(dataPath.getParent());
            }
            try (BufferedWriter bw = Files.newBufferedWriter(dataPath)) {
                bw.write("id,title,author,isAvailable\n");
                for (Book b : books) {
                    bw.write(b.id + "," + sanitize(b.title) + "," + sanitize(b.author) + "," + b.isAvailable + "\n");
                }
            }
            System.out.println("💾 Saved to " + dataPath.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    // ----- Helpers -----
    private Book findById(int id) {
        for (Book b : books) if (b.id == id) return b;
        return null;
    }

    private String sanitize(String s) {
        // Avoid commas in CSV. You can improve with proper CSV quoting later.
        return s.replace(',', ' ').trim();
    }

    private void loadFromFile() {
        if (!Files.exists(dataPath)) return; // nothing to load on first run
        try (BufferedReader br = Files.newBufferedReader(dataPath)) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length != 4) continue;
                int id = Integer.parseInt(parts[0].trim());
                String title = parts[1].trim();
                String author = parts[2].trim();
                boolean isAvailable = Boolean.parseBoolean(parts[3].trim());
                books.add(new Book(id, title, author, isAvailable));
            }
            System.out.println("📂 Loaded " + books.size() + " book(s) from file.");
        } catch (Exception e) {
            System.out.println("Error loading data. Starting fresh. Details: " + e.getMessage());
        }
    }
}

public class LibraryManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Path dataFile = Paths.get("books.csv");
        Library lib = new Library(dataFile);

        while (true) {
                System.out.println("\n=====================================");
                System.out.println("   📚 Library Management System   ");
                System.out.println("=====================================");
                System.out.println("1️⃣  Add Book");
                System.out.println("2️⃣  View All Books");
                System.out.println("3️⃣  Search Book (title/author)");
                System.out.println("4️⃣  Issue Book");
                System.out.println("5️⃣  Return Book");
                System.out.println("6️⃣  Remove Book");
                System.out.println("7️⃣  Save & Exit");
                System.out.print("\n🔸 Enter your choice (1-7): ");

            String choiceRaw = sc.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceRaw);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (1-7).");
                continue;
            }

            switch (choice) {
                case 1:  {
                    System.out.println("\n🔹 Add a New Book");
                    System.out.print("   Title: ");
                    String title = readNonEmpty(sc);
                    System.out.print("   Author: ");
                    String author = readNonEmpty(sc);
                    lib.addBook(title, author);
                    System.out.println("✅ Book added successfully!");
                    break;
                }
                case 2: {
                    System.out.println("\n🔹 All Books in Library:");
                    lib.viewBooks();
                    break;
                }
                case 3:{
                    System.out.print("\n🔹 Enter search text: ");
                    String q = sc.nextLine().trim();
                    lib.searchBooks(q);
                    break;
                }
                case 4:{
                    int id = readInt(sc, "\n🔹 Enter Book ID to issue: ");
                    lib.issueBook(id);
                    System.out.println("📕 Book issued (if available).");
                    break;
                }
                case 5: {
                    int id = readInt(sc, "\n🔹 Enter Book ID to return: ");
                    lib.returnBook(id);
                    System.out.println("📗 Book returned (if issued).");
                    break;
                }
                case 6:{
                    int id = readInt(sc, "\n🔹 Enter Book ID to remove: ");
                    lib.removeBook(id);
                    System.out.println("🗑️ Book removed (if found).");
                    break;
                }
                case 7: {
                    lib.saveToFile();
                    System.out.println("👋 Exiting... Thank you for using the Library Management System!");
                    sc.close();
                    return;
                }
                default: System.out.println("⚠️  Please choose a valid option between 1 and 7.");
            }
        }
    }

    private static String readNonEmpty(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.print("Input cannot be empty. Try again: ");
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine().trim();
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("Not a number. Try again.");
            }
        }
    }
}
