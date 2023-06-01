import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class PersonController {

    @PostMapping("/persons")
    public ResponseEntity<String> receiveJsonAndConvertToCsv(@RequestBody List<Person> persons) {
        try {
            CsvSchema schema = CsvSchema.builder().setUseHeader(true).build();
            ObjectReader oReader = new CsvMapper().readerFor(Person.class).with(schema);

            File csvFile = new File("persons.csv");
            oReader.writeValues(csvFile).writeAll(persons).close();

            return ResponseEntity.ok("File created successfully!");

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing JSON to CSV: " + e.getMessage());
        }
    }
}
