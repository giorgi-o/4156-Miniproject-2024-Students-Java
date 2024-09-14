package dev.coms4156.project.individualproject;

import static dev.coms4156.project.individualproject.CourseUnitTests.courseForTesting;
import static dev.coms4156.project.individualproject.CourseUnitTests.testCourse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit tests for the Course class.
 */
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class DepartmentUnitTests {

  /**
   * Creates a department instance for testing.
   *
   * @return the department instance
   */
  public static Department deptForTesting() {
    Course testCourse = courseForTesting();

    HashMap<String, Course> courses = new HashMap<>();
    courses.put("4156", testCourse);

    return new Department("COMS", courses, "Luca Carloni", 10);
  }

  @BeforeAll
  public static void setUp() {
    testDepartment = deptForTesting();
  }

  @Test
  public void departmentGetterTest() {
    assertEquals("Luca Carloni", testDepartment.getDepartmentChair());
    assertEquals(10, testDepartment.getNumberOfMajors());
    assertEquals(1, testDepartment.getCourseSelection().size());
    assertEquals(testCourse, testDepartment.getCourseSelection().get("4156"));
  }

  @Test
  public void departmentAddDropMajorTest() {
    testDepartment.addPersonToMajor();
    assertEquals(11, testDepartment.getNumberOfMajors());

    testDepartment.dropPersonFromMajor();
    assertEquals(10, testDepartment.getNumberOfMajors());
  }

  @Test
  public void departmentAddCourseTest() {
    // setup: remove existing course
    testDepartment.getCourseSelection().remove("4156");
    assertEquals(0, testDepartment.getCourseSelection().size());

    testDepartment.addCourse("4156", testCourse);
    assertEquals(1, testDepartment.getCourseSelection().size());
  }

  @Test
  public void departmentToStringTest() {
    String expectedResult = """
            COMS 4156:\s
            Instructor: Griffin Newbold; Location: 417 IAB; Time: 11:40-12:55
            """;
    assertEquals(expectedResult, testDepartment.toString());
  }

  /**
   * The department instance used for testing.
   */
  public static Department testDepartment;
}

