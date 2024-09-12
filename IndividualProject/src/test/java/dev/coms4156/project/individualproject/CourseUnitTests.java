package dev.coms4156.project.individualproject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  public static Department deptForTesting() {
    Course testCourse = courseForTesting();

    HashMap<String, Course> courses = new HashMap<>();
    courses.put("4156", testCourse);

    return new Department("COMS", courses, "Luca Carloni", 10);
  }

  @BeforeAll
  public static void setUp() {
    testCourse = courseForTesting();
    testDepartment = deptForTesting();
  }


  @Test
  public void toStringTest() {
    String expectedResult = "\nInstructor: Griffin Newbold; Location: 417 IAB; Time: 11:40-12:55";
    assertEquals(expectedResult, testCourse.toString());
  }

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
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./corrupted_data.txt"))) {
      out.writeObject("junk");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to file.");
    }

    // try to load the corrupted database
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            new MyFileDatabase(0, "./corrupted_data.txt"));

    assertEquals("Invalid object type in file.", exception.getMessage());
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
    String expectedResult = "COMS 4156: \n" +
            "Instructor: Griffin Newbold; Location: 417 IAB; Time: 11:40-12:55\n";
    assertEquals(expectedResult, testDepartment.toString());
  }

  private void deptNotFoundTest(HttpMethod method, String endpoint) throws Exception {
    endpoint = "/" + endpoint + "?deptCode=nonexistent";
    mockMvc.perform(request(method, endpoint))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string(equalTo("Department Not Found")));
  }

  private void courseNotFoundTest(HttpMethod method, String endpoint, String extraParams) throws Exception {
    endpoint = "/" + endpoint + "?deptCode=COMS&courseCode=1";
    if (extraParams != null) {
      endpoint += "&" + extraParams;
    }

    mockMvc.perform(request(method, endpoint))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string(equalTo("Course Not Found")));
  }

  @Test
  @Order(1)
  void indexReturnsWelcomeTest() throws Exception {
    // source: https://spring.io/guides/gs/testing-web
    mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("Welcome, in order to make an API call")));
  }

  @Test
  @Order(1)
  void retrieveDepartmentTest() throws Exception {
    mockMvc.perform(get("/retrieveDept?deptCode=COMS"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("COMS 4156")))
            .andExpect(content().string(containsString("Adam Cannon")));

    deptNotFoundTest(GET, "retrieveDept");
  }

  @Test
  @Order(1)
  void retrieveCourseTest() throws Exception {
    mockMvc.perform(get("/retrieveCourse?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Instructor:")));

    courseNotFoundTest(GET, "retrieveCourse", null);
  }

  @Test
  @Order(1)
  void isCourseFullTest() throws Exception {
    mockMvc.perform(get("/isCourseFull?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("false")));

    courseNotFoundTest(GET, "isCourseFull", null);
  }

  @Test
  @Order(1)
  void getMajorCountFromDeptTest() throws Exception {
    mockMvc.perform(get("/getMajorCountFromDept?deptCode=COMS"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("majors in the department")));

    deptNotFoundTest(GET, "getMajorCountFromDept");

  }

  @Test
  @Order(1)
  void identifyDeptChairTest() throws Exception {
    mockMvc.perform(get("/idDeptChair?deptCode=COMS"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("is the department chair.")));

    deptNotFoundTest(GET, "idDeptChair");
  }

  @Test
  @Order(1)
  void findCourseLocationTest() throws Exception {
    mockMvc.perform(get("/findCourseLocation?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("is where the course is located")));

    courseNotFoundTest(GET, "findCourseLocation", null);
  }

  @Test
  @Order(1)
  void findCourseInstructorTest() throws Exception {
    mockMvc.perform(get("/findCourseInstructor?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("is the instructor for the course")));

    courseNotFoundTest(GET, "findCourseInstructor", null);
  }

  @Test
  @Order(1)
  void findCourseTimeTest() throws Exception {
    mockMvc.perform(get("/findCourseTime?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("The course meets at:")));

    courseNotFoundTest(GET, "findCourseTime", null);
  }

  @Test
  @Order(2)
  void addMajorToDeptTest() throws Exception {
    mockMvc.perform(patch("/addMajorToDept?deptCode=COMS"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated successfully")));

    deptNotFoundTest(PATCH, "addMajorToDept");
  }

  @Test
  @Order(2)
  void removeMajorFromDeptTest() throws Exception {
    mockMvc.perform(patch("/removeMajorFromDept?deptCode=COMS"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated or is at minimum")));

    deptNotFoundTest(PATCH, "removeMajorFromDept");
  }

  @Test
  @Order(2)
  void dropStudentFromCourseTest() throws Exception {
    mockMvc.perform(patch("/dropStudentFromCourse?deptCode=COMS&courseCode=4156"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Student has been dropped")));

    courseNotFoundTest(PATCH, "dropStudentFromCourse", null);
  }

  @Test
  @Order(2)
  void setEnrollmentCountTest() throws Exception {
    mockMvc.perform(patch("/setEnrollmentCount?deptCode=COMS&courseCode=4156&count=256"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated successfully")));

    courseNotFoundTest(PATCH, "setEnrollmentCount", "count=1");
  }

  @Test
  @Order(2)
  void changeCourseTimeTest() throws Exception {
    mockMvc.perform(patch("/changeCourseTime?deptCode=COMS&courseCode=4156&time=20:10-21:25"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated successfully")));

    courseNotFoundTest(PATCH, "changeCourseTime", "time=20:10-21:25");
  }

  @Test
  @Order(2)
  void changeCourseTeacherTest() throws Exception {
    mockMvc.perform(patch("/changeCourseTeacher?deptCode=COMS&courseCode=4156&teacher=Griffin Newbold"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated successfully")));

    courseNotFoundTest(PATCH, "changeCourseTeacher", "teacher=Griffin Newbold");
  }

  @Test
  @Order(2)
  void changeCourseLocationTest() throws Exception {
    mockMvc.perform(patch("/changeCourseLocation?deptCode=COMS&courseCode=4156&location=417 IAB"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Attribute was updated successfully")));

    courseNotFoundTest(PATCH, "changeCourseLocation", "location=417 IAB");
  }

  /**
   * The test course instance used for testing.
   */
  public static Course testCourse;

  /**
   * The department instance used for testing.
   */
  public static Department testDepartment;

  @Autowired
  private IndividualProjectApplication app;

  @Autowired
  private MockMvc mockMvc;
}

