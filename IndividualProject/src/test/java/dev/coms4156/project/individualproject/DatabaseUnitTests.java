package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit tests for the Course class.
 */
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class DatabaseUnitTests {

  @Test
  public void saveLoadDbTest() {
    // note: this will reset the database.
    // do not run on production machines!
    app.resetDataFile();
    IndividualProjectApplication.myFileDatabase.saveContentsToFile();

    // load the database from the file
    new MyFileDatabase(0, "./data.txt");
  }

  @Test
  public void loadCorruptedDbTest() {
    // write junk object to corrupted_data.txt
    try (ObjectOutputStream out = new ObjectOutputStream(
            new FileOutputStream("./corrupted_data.txt"))) {
      out.writeObject("junk");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to file.");
    }

    // try to load the corrupted database
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new MyFileDatabase(0, "./corrupted_data.txt"));

    assertEquals("Invalid object type in file.", exception.getMessage());
  }

  @Autowired
  private IndividualProjectApplication app;
}

