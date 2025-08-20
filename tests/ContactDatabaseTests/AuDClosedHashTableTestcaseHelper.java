import java.util.NoSuchElementException;

public class AuDClosedHashTableTestcaseHelper extends AuDClosedHashTable {

    private int _capacity;

    public AuDClosedHashTableTestcaseHelper(int capacity) throws Exception {
        super(capacity);
        TestcaseHelper.setValueOfPrivateSuperclassField(this, "counter", 0);
        TestcaseHelper.setValueOfPrivateSuperclassField(this, "table", new Contact[capacity]);
        this._capacity = capacity;
    }

    protected int hash(String s, int i) {
        if (i % 2 == 0) {
            return Math.floorMod((hash(s) - i / 2 - 1), this._capacity);
        } else {
            return Math.floorMod((hash(s) + i / 2), this._capacity);
        }
    }


    @Override
    public boolean isFull() {
        int counter = 0;
        try {
            counter = TestcaseHelper.getValueOfPrivateSuperclassField(this, "counter");
        } catch (Exception e) {
            e.printStackTrace();
        }
       return counter == this._capacity;
    }

    public void cleanInsert(Contact c) throws Exception {
        if (isFull()) {
            throw new UnsupportedOperationException();
        }

        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(this, "table");
        int counter = TestcaseHelper.getValueOfPrivateSuperclassField(this, "counter");

        int i = 0;
        int hash = hash(c.getEmail(), i++);
        while (table[hash] != null) {
            hash = hash(c.getEmail(), i++);
        }
        table[hash] = c;

        TestcaseHelper.setValueOfPrivateSuperclassField(this, "counter", ++counter);
    }

    public int cleanGetIndexOf(String email) throws Exception {

        int i = 0;

        int hash = hash(email, i++);
        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(this, "table");
        boolean[] deleted = TestcaseHelper.getValueOfPrivateSuperclassField(this, "deleted");

        while (i <= this._capacity) {

            if (table[hash] == null && !deleted[hash]) {
                break;
            }

            if (table[hash] != null && table[hash].getEmail().equals(email)) {
                return hash;
            }

            hash = hash(email, i++);
        }

        throw new NoSuchElementException("Element with email " + email + " does not exist!");
    }

    public Contact cleanGetContact(String email) throws Exception {

        Contact[] table = TestcaseHelper.getValueOfPrivateSuperclassField(this, "table");
        int index = cleanGetIndexOf(email);

        return table[index];
    }
}
