public class ContactDatabase {
    public static void main(String[] args) {
        AuDClosedHashTable table = new AuDClosedHashTable(10);
        table.insert(new Contact("John"));
        table.insert(new Contact("Jane"));
        table.insert(new Contact("Jack"));
        table.insert(new Contact("Jill"));
        table.insert(new Contact("James"));
        table.insert(new Contact("Jenny"));
        table.insert(new Contact("Jared"));
        table.insert(new Contact("Jasmine"));
        table.insert(new Contact("Jade"));
        table.insert(new Contact("Jasper"));
        table.insert(new Contact("Jocelyn"));
        table.insert(new Contact("Jared"));
        table.getContact("Jared");
        table.remove(new Contact("Jared"));
        table.getContact("Jared");
    }
}
