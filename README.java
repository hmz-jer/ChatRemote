import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import static org.junit.Assert.*;

public class XPathConfigurationItemTest {

    @Test
    public void testValidRecord() throws InvalidConfigurationException {
        CSVRecord record = new CSVRecord(new String[] {"message_lso", "message_id", "debtor_agent"});
        XPathConfigurationItem item = new XPathConfigurationItem(record);
        assertEquals("message_lso", item.getMessagelso());
        assertEquals("message_id", item.getMessageId());
        assertEquals("debtor_agent", item.getDebtorAgent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncompleteRecord() throws InvalidConfigurationException {
        CSVRecord record = new CSVRecord(new String[] {"message_lso", "message_id"});
        XPathConfigurationItem item = new XPathConfigurationItem(record);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testEmptyFields() throws InvalidConfigurationException {
        CSVRecord record = new CSVRecord(new String[] {"", "message_id", "debtor_agent"});
        XPathConfigurationItem item = new XPathConfigurationItem(record);
    }

}
