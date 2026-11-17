package readers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import model.Address;

public interface AddressReader {
    void read(Path path, Consumer<Address> onAddress) throws IOException;
}
