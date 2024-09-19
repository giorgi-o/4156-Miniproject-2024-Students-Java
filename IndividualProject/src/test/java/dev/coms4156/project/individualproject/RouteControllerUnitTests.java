package dev.coms4156.project.individualproject;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


/**
 * Unit tests for the Course class.
 */
@SpringBootTest
@ContextConfiguration
@AutoConfigureMockMvc
public class RouteControllerUnitTests {

  private void deptNotFoundTest(HttpMethod method, String endpoint) throws Exception {
    endpoint = "/" + endpoint + "?deptCode=nonexistent";
    mockMvc.perform(request(method, endpoint))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string(equalTo("Department Not Found")));
  }

  private void courseNotFoundTest(HttpMethod method, String endpoint, String extraParams)
          throws Exception {
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
  @Order(3)
  void retrieveCoursesTest() throws Exception {
    mockMvc.perform(get("/retrieveCourses?courseCode=9999"))
            .andDo(print())
            .andExpect(status().isNotFound());

    Course mockCsCourse = new Course("Mock CS Instructor", "Mock CS Location", "Mock CS Time", 1);
    HashMap<String, Department> deptMap = IndividualProjectApplication.myFileDatabase.getDepartmentMapping();
    Department comsDept = deptMap.get("COMS");
    comsDept.addCourse("9999", mockCsCourse);

    mockMvc.perform(get("/retrieveCourses?courseCode=9999"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Mock CS Instructor")));

    Course mockEconCourse = new Course("Mock Econ Instructor", "Mock Econ Location", "Mock Econ Time", 1);
    Department econDept = deptMap.get("ECON");
    econDept.addCourse("9999", mockEconCourse);

    mockMvc.perform(get("/retrieveCourses?courseCode=9999"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Mock Econ Instructor")))
            .andExpect(content().string(containsString("Mock CS Instructor")));
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
    mockMvc.perform(patch(
            "/changeCourseTeacher?deptCode=COMS&courseCode=4156&teacher=Griffin Newbold"))
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

  @Test
  @Order(2)
  void enrollStudentInCourseTest() throws Exception {
    // test happy path
    mockMvc.perform(patch("/setEnrollmentCount?deptCode=COMS&courseCode=4156&count=1"));
    mockMvc.perform(patch("/enrollStudentInCourse?deptCode=COMS&courseCode=4156"))
              .andDo(print())
              .andExpect(status().isOk())
              .andExpect(content().string(containsString("Student has been enrolled")));

    // test course not found
    courseNotFoundTest(PATCH, "enrollStudentInCourse", null);

    // test course full
    mockMvc.perform(patch("/setEnrollmentCount?deptCode=COMS&courseCode=4156&count=999"));
    mockMvc.perform(patch("/enrollStudentInCourse?deptCode=COMS&courseCode=4156"))
              .andDo(print())
              .andExpect(status().isBadRequest())
              .andExpect(content().string(equalTo("Course is full.")));
  }

  @Autowired
  private MockMvc mockMvc;
}

