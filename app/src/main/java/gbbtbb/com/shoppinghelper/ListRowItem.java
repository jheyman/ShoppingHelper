package gbbtbb.com.shoppinghelper;

public class ListRowItem {

    private String item_name;
    private int amount;
    private boolean alreadySpent;
    private String date;

    public ListRowItem(int amount, String name, String date, boolean alreadySpent) {
        this.item_name = name;
        this.amount = amount;
        this.alreadySpent = alreadySpent;
        this.date=date;
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

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @Override
    public String toString() {
        return item_name + "\n";
    }
}
