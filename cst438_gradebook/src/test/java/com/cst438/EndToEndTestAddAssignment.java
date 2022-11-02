package com.cst438;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

@SpringBootTest
public class EndToEndTestAddAssignment {
	public static final String CHROME_DRIVER_FILE_LOCATION = "/Users/zai/Library/Selenium/chromedriver";

	public static final String URL = "http://localhost:3000";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	public static final String TEST_USER_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "Test";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final String TEST_COURSE_TITLE = "A Test Course";
	public static final int TEST_COURSE_ID = 111001;
	public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
	public static final Date TEST_ASSIGNMENT_DUE_DATE = new Date(1667335826006L);

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentGradeRepository assignnmentGradeRepository;

	@Autowired
	AssignmentRepository assignmentRepository;

	@Test
	public void addAssignmentToCourseTest() throws Exception {
//		Database setup:  create course		
		Course c = new Course();
		c.setCourse_id(TEST_COURSE_ID);
		c.setInstructor(TEST_INSTRUCTOR_EMAIL);
		c.setSemester("Fall");
		c.setYear(2021);
		c.setTitle(TEST_COURSE_TITLE);

//	    add a student TEST into course 99999
		Enrollment e = new Enrollment();
		e.setCourse(c);
		e.setStudentEmail(TEST_USER_EMAIL);
		e.setStudentName(TEST_STUDENT_NAME);

		courseRepository.save(c);
		e = enrollmentRepository.save(e);

		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
		WebDriver driver = new ChromeDriver(options);
		// Puts an Implicit wait for 10 seconds before throwing exception
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		driver.get(URL);
		Thread.sleep(SLEEP_DURATION);

		try {
			// Find and click the "Add Assignment" button.
			driver.findElement(By.xpath("//a[@href='/assignment/add']")).click();
			Thread.sleep(SLEEP_DURATION);

			// Enter the assignment details.
			driver.findElement(By.xpath("//input[@name='assignName']")).sendKeys(TEST_ASSIGNMENT_NAME);
			driver.findElement(By.xpath("//input[@name='dueDate']")).sendKeys(TEST_ASSIGNMENT_DUE_DATE.toString());
			driver.findElement(By.xpath("//input[@name='course']")).sendKeys(TEST_COURSE_TITLE);

			// Click the submit button.
			driver.findElement(By.xpath("//button")).click();
			Thread.sleep(SLEEP_DURATION);
			Thread.sleep(SLEEP_DURATION);

			// Verify assignment has been added to the bottom of the Gradebook.
			List<WebElement> assignCells = driver
					.findElements(By.xpath("//div[@role='row'][last()]/div[@role='cell']"));
			boolean success = false;
			for (WebElement element : assignCells) {
				System.out.println(element.getText());
				switch (element.getAttribute("data-colindex")) {
				case "0":
					if (element.getText() == TEST_ASSIGNMENT_NAME)
						success = true;
					break;
				case "1":
					if (element.getText() == TEST_COURSE_TITLE)
						success = true;
					break;
				case "2":
					if (element.getText() == TEST_ASSIGNMENT_DUE_DATE.toString())
						success = true;
				}
			}

			assertTrue(success);

		} catch (Exception ex) {
			throw ex;
		} finally {

			/*
			 * clean up database so the test is repeatable.
			 */
			enrollmentRepository.delete(e);
			List<Assignment> assignments = (List<Assignment>) assignmentRepository.findAll();
			for (Assignment a : assignments) {
				if (a.getCourse().getTitle().equals(TEST_COURSE_TITLE)) {
					assignmentRepository.delete(a);
					return;
				}
			}
			courseRepository.delete(c);

			driver.quit();
		}
	}
}
