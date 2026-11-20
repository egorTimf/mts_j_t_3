import java.util.Map;
import readers.AddressReader;
import readers.CsvAddressReader;
import readers.XmlAddressReader;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        Map<String, AddressReader> readers = Map.of(
                "csv", new CsvAddressReader(),
                "xml", new XmlAddressReader());
        new ConsoleUI(readers).run();
    }
}
