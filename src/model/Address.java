package model;

public record Address(String city, String street, String house, int floor) {
    public Address {
        city = required(city, "city");
        street = required(street, "street");
        house = required(house, "house");
        if (floor < 1) {
            throw new IllegalArgumentException("Число этажей должно быть положительным.");
        }
    }

    public static Address fromFields(String city, String street, String house, String floor) {
        if (floor == null || floor.isBlank()) {
            throw new IllegalArgumentException("Пустое поле floor.");
        }
        try {
            return new Address(city, street, house, Integer.parseInt(floor.strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректное число этажей: " + floor, e);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Пустое поле " + field + ".");
        }
        // Миллионы записей используют одни и те же названия и номера домов.
        return value.strip().intern();
    }
}
