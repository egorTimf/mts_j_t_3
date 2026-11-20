package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import model.Address;
import readers.AddressReader;
import services.AddressStatistics;

public class ConsoleUI {
    private final Map<String, AddressReader> readers;

    public ConsoleUI(Map<String, AddressReader> readers) {
        this.readers = Map.copyOf(readers);
    }

    public void run() {
        var console = System.console();
        Scanner input = console == null ? new Scanner(System.in) : new Scanner(console.reader());
        while (true) {
            System.out.println("Введите путь к CSV/XML или 0 для выхода:");
            if (!input.hasNextLine()) {
                break;
            }
            String name = input.nextLine().strip();
            if (name.equals("0") || name.equalsIgnoreCase("exit")) {
                break;
            }
            if (name.isEmpty()) {
                continue;
            }
            // Windows может скопировать путь вместе с кавычками.
            if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
                name = name.substring(1, name.length() - 1);
            }
            try {
                process(Path.of(name));
            } catch (IOException | IllegalArgumentException | SecurityException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        System.out.println("Работа завершена.");
    }

    private void process(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Файл не найден: " + path);
        }
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        AddressReader reader = readers.get(extension);
        if (reader == null) {
            throw new IOException("Поддерживаются только файлы CSV и XML.");
        }

        AddressStatistics statistics = new AddressStatistics();
        long start = System.nanoTime();
        reader.read(path, statistics::add);
        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        printStatistics(statistics);
        System.out.printf("Время обработки: %.3f с%n%n", seconds);
    }

    private void printStatistics(AddressStatistics statistics) {
        System.out.printf("Записей: %d, уникальных: %d%n",
                statistics.getTotal(), statistics.getUniqueCount());
        Map<Address, Long> duplicates = statistics.getDuplicates();
        if (duplicates.isEmpty()) {
            System.out.println("Дублирующихся записей нет.");
        } else {
            System.out.println("Дублирующиеся записи:");
            duplicates.forEach((address, count) -> System.out.printf(
                    "%s, %s, д. %s, этажей %d — вхождений: %d%n",
                    address.city(), address.street(), address.house(), address.floor(), count));
        }

        System.out.printf("%-24s", "Город");
        for (int floor = 1; floor <= AddressStatistics.MAX_FLOOR; floor++) {
            System.out.printf("%9s", floor + " эт.");
        }
        System.out.println();
        for (String city : statistics.getCities()) {
            System.out.printf("%-24s", city);
            for (int floor = 1; floor <= AddressStatistics.MAX_FLOOR; floor++) {
                System.out.printf("%9d", statistics.getBuildingCount(city, floor));
            }
            System.out.println();
        }
    }
}
