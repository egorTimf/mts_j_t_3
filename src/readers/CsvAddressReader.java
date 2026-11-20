package readers;

import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import model.Address;

public class CsvAddressReader implements AddressReader {
    private static final List<String> COLUMNS = List.of("city", "street", "house", "floor");

    @Override
    public void read(Path path, Consumer<Address> onAddress) throws IOException {
        try (var input = new LineNumberReader(Files.newBufferedReader(path))) {
            try {
                List<String> header = readRow(input);
                if (header == null || header.size() != COLUMNS.size()
                        || !header.containsAll(COLUMNS)) {
                    throw new IllegalArgumentException("Ожидаются столбцы " + COLUMNS + ".");
                }
                int city = header.indexOf("city");
                int street = header.indexOf("street");
                int house = header.indexOf("house");
                int floor = header.indexOf("floor");

                List<String> row;
                while ((row = readRow(input)) != null) {
                    if (row.size() != COLUMNS.size()) {
                        throw new IllegalArgumentException("В записи должно быть четыре поля.");
                    }
                    onAddress.accept(Address.fromFields(row.get(city), row.get(street),
                            row.get(house), row.get(floor)));
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("CSV, строка " + input.getLineNumber()
                        + ". " + e.getMessage(), e);
            }
        }
    }

    private List<String> readRow(LineNumberReader input) throws IOException {
        String line;
        do {
            line = input.readLine();
            if (line == null) {
                return null;
            }
            if (input.getLineNumber() == 1 && line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
        } while (line.isBlank());

        List<String> fields = new ArrayList<>(COLUMNS.size());
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closed = false;
        while (true) {
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == ';' && !quoted) {
                    fields.add(field.toString());
                    field.setLength(0);
                    closed = false;
                } else if (c == '"' && quoted) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closed = true;
                    }
                } else if (c == '"' && field.length() == 0 && !closed) {
                    quoted = true;
                } else if (closed || c == '"') {
                    throw new IllegalArgumentException("Неверные кавычки в поле.");
                } else {
                    field.append(c);
                }
            }
            if (!quoted) {
                fields.add(field.toString());
                return fields;
            }
            line = input.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Поле в кавычках не закрыто.");
            }
            field.append('\n');
        }
    }
}
