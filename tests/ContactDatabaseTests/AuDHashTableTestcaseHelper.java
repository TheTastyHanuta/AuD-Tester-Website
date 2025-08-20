public class AuDHashTableTestcaseHelper extends AuDHashTable {

    private int _capacity;

    public AuDHashTableTestcaseHelper(int capacity) {
        super(capacity);
        this._capacity = capacity;
    }

    public int getCapacity() {
        return this._capacity;
    }

    public int testHash(String s) {
        return hash(s);
    }

    public int cleanHash(String s) {

        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            sum += s.charAt(i);
        }
        sum = sum % this._capacity;
        return sum;
    }

    @Override
    public void insert(Contact c) {

    }

    @Override
    public void remove(Contact c) {

    }

    @Override
    public Contact getContact(String email) {
        return null;
    }
}
