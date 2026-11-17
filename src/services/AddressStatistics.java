package services;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import model.Address;

public class AddressStatistics {
    public static final int MAX_FLOOR = 5;

    private final Set<Address> unique = new HashSet<>();
    private final Map<Address, Long> duplicates = new LinkedHashMap<>();
    private final Map<String, long[]> floorsByCity = new TreeMap<>();
    private long total;

    public void add(Address address) {
        total++;
        if (!unique.add(address)) {
            // При первом повторе в файле уже два экземпляра записи.
            duplicates.compute(address, (key, count) -> count == null ? 2L : count + 1);
            return;
        }

        long[] counts = floorsByCity.computeIfAbsent(address.city(), key -> new long[MAX_FLOOR]);
        if (address.floor() <= MAX_FLOOR) {
            counts[address.floor() - 1]++;
        }
    }

    public long getTotal() {
        return total;
    }

    public int getUniqueCount() {
        return unique.size();
    }

    public Map<Address, Long> getDuplicates() {
        return Collections.unmodifiableMap(duplicates);
    }

    public Set<String> getCities() {
        return Collections.unmodifiableSet(floorsByCity.keySet());
    }

    public long getBuildingCount(String city, int floor) {
        return floorsByCity.get(city)[floor - 1];
    }
}
