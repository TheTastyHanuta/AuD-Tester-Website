import java.util.LinkedList;

public class AuDOpenHashTableTestcaseHelper extends AuDOpenHashTable {

    private int _capacity;

    public AuDOpenHashTableTestcaseHelper(int capacity) throws Exception {

        super(capacity);

        LinkedList<Contact>[] table = new LinkedList[capacity];

        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<Contact>();
        }
        TestcaseHelper.setValueOfPrivateSuperclassField(this, "table", table);

        this._capacity = capacity;
    }

    protected int hash(String s) {
        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            sum += s.charAt(i);
        }
        sum = sum % this._capacity;
        return sum;
    }


    public void cleanInsert(Contact c) throws Exception {
        int hash = hash(c.getEmail());
        LinkedList<Contact>[] table = TestcaseHelper.getValueOfPrivateSuperclassField(this, "table");
        table[hash].add(c);
        TestcaseHelper.setValueOfPrivateSuperclassField(this, "table", table);
    }

}
