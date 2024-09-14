package dev.coms4156.project.individualproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class CourseUnitTests {

  public static Course courseForTesting() {
    return new Course("Griffin Newbold", "417 IAB", "11:40-12:55", 250);
  }

  @BeforeAll
  public static void setUp() {
    testCourse = courseForTesting();
  }

  @Test
  public void toStringTest() {
    String expectedResult = "\nInstructor: Griffin Newbold; Location: 417 IAB; Time: 11:40-12:55";
    assertEquals(expectedResult, testCourse.toString());
  }

  @Test
  public void testCourseFull() {
    // note: I assume a course is NOT full when enrolled == capacity

    testCourse.setEnrolledStudentCount(250);
    assertTrue(testCourse.isCourseFull());

    testCourse.setEnrolledStudentCount(249);
    assertFalse(testCourse.isCourseFull());
  }

  @Test
  public void enrollAndDropStudentTest() {
    testCourse.setEnrolledStudentCount(249);
    assertTrue(testCourse.enrollStudent());
    assertFalse(testCourse.enrollStudent());

    testCourse.setEnrolledStudentCount(1);
    assertTrue(testCourse.dropStudent());
    assertFalse(testCourse.dropStudent());
  }

  @Test
  public void courseGetterTest() {
    assertEquals("Griffin Newbold", testCourse.getInstructorName());
    assertEquals("417 IAB", testCourse.getCourseLocation());
    assertEquals("11:40-12:55", testCourse.getCourseTimeSlot());
  }

  /**
   * The test course instance used for testing.
   */
  public static Course testCourse;
}

