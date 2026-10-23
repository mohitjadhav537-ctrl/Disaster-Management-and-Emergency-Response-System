package library;

public class Library {

    public String libraryName = "VIT Central Library";
    private int totalBooks = 5000;
    protected String librarian = "Mr. Sharma";

    // Same class: all members can be accessed
    public void displayInsideClass() {
        System.out.println("Inside Library class:");
        System.out.println("Public: " + libraryName);
        System.out.println("Private: " + totalBooks);
        System.out.println("Protected: " + librarian);
    }

    public int getTotalBooks() {
        return totalBooks;
    }
}
