package com.spring_midterm.midterm.data;

import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.repository.IRepository;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

	private final IRepository<Student> studentRepository;
	private final IRepository<Task> taskRepository;

	public DataInitializer(IRepository<Student> studentRepository, IRepository<Task> taskRepository) {
		this.studentRepository = studentRepository;
		this.taskRepository = taskRepository;
	}

	@Override
	public void run(String... args) {
		if (studentRepository.count() > 0) {
			log.info("Database already contains data, skipping initialization");
			return;
		}

		log.info("Initializing dev data...");

		Student john = new Student();
		john.setFirstName("John");
		john.setLastName("Doe");
		john.setEmail("john.doe@example.com");
		studentRepository.save(john);

		Student jane = new Student();
		jane.setFirstName("Jane");
		jane.setLastName("Smith");
		jane.setEmail("jane.smith@example.com");
		studentRepository.save(jane);

		Task task1 = new Task();
		task1.setTitle("Complete project setup");
		task1.setDescription("Set up the initial project structure and dependencies");
		task1.setCompleted(true);
		task1.setDueDate(LocalDate.now().plusDays(7));
		task1.setStudent(john);
		taskRepository.save(task1);

		Task task2 = new Task();
		task2.setTitle("Write unit tests");
		task2.setDescription("Add comprehensive unit tests for all services");
		task2.setCompleted(false);
		task2.setDueDate(LocalDate.now().plusDays(14));
		task2.setStudent(john);
		taskRepository.save(task2);

		Task task3 = new Task();
		task3.setTitle("Review pull requests");
		task3.setDescription("Review open PRs and provide feedback");
		task3.setCompleted(false);
		task3.setDueDate(LocalDate.now().plusDays(3));
		task3.setStudent(jane);
		taskRepository.save(task3);

		log.info("Dev data initialized: 2 students, 3 tasks");
	}
}
