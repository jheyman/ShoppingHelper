package gbbtbb.com.shoppinghelper;

public class ListRowItem {

    private String item_name;
    private int amount;
    private boolean alreadySpent;
    private String creationDate;

    public ListRowItem(String name, int amount, String creationDate) {
        this.item_name = name;
        this.amount = amount;
        this.alreadySpent = alreadySpent;
        this.creationDate = creationDate;
    }

    public String getItemName() { return item_name; }
    public void setItemName(String name) { this.item_name = item_name; }

    public boolean getAlreadySpent() { return alreadySpent; }
    public void setAlreadySpent(boolean spent) { this.alreadySpent = spent; }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String date) { this.creationDate = date; }

    @Override
    public String toString() {
        return item_name + "\n";
    }
}
