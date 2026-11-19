package readers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import model.Address;

public class XmlAddressReader implements AddressReader {
    @Override
    public void read(Path path, Consumer<Address> onAddress) throws IOException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        try (var input = new BufferedInputStream(Files.newInputStream(path))) {
            XMLStreamReader xml = factory.createXMLStreamReader(input);
            try {
                xml.nextTag();
                xml.require(XMLStreamConstants.START_ELEMENT, null, "root");
                while (xml.nextTag() == XMLStreamConstants.START_ELEMENT) {
                    xml.require(XMLStreamConstants.START_ELEMENT, null, "item");
                    onAddress.accept(Address.fromFields(
                            xml.getAttributeValue(null, "city"), xml.getAttributeValue(null, "street"),
                            xml.getAttributeValue(null, "house"), xml.getAttributeValue(null, "floor")));
                    xml.nextTag();
                    xml.require(XMLStreamConstants.END_ELEMENT, null, "item");
                }
                xml.require(XMLStreamConstants.END_ELEMENT, null, "root");
                while (xml.hasNext()) {
                    xml.next();
                }
            } catch (IllegalArgumentException e) {
                throw new IOException("XML, строка " + xml.getLocation().getLineNumber()
                        + ". " + e.getMessage(), e);
            } finally {
                xml.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException("Некорректный XML. " + e.getMessage(), e);
        }
    }
}
