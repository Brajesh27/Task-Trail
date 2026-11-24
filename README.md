# TaskTrail

An employee task manager built with Java and Spring Boot. Employers assign tasks and monitor progress; employees update their own tasks; anything unfinished by its due date carries forward automatically.

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Language | Java 21 | Current LTS, what employers expect |
| Framework | Spring Boot 3 | The most-asked-for Java skill in job posts |
| Pages | Thymeleaf (server-rendered HTML) | No separate frontend to learn yet |
| Database | H2 (in-memory) while learning, MySQL later | Zero setup now, real DB for the final version |
| Data access | Spring Data JPA / Hibernate | Write Java classes, get SQL tables |
| Login | Spring Security, two roles | EMPLOYER and EMPLOYEE |
| Build / tests | Maven, JUnit 5, MockMvc | Standard tooling |

## Learning roadmap

Each step ends with something you can run, and teaches one main idea.

1. **Project setup and first page** ✅ — how a request reaches a `@Controller`, and how Thymeleaf fills a template.
2. **Data model** ✅ — `User` and `Task` entities, JPA, H2 console, repositories. Concept: ORM and relationships (one employee has many tasks).
3. **Two logins** ✅ — Spring Security with EMPLOYER and EMPLOYEE roles, password hashing, separate dashboards per role.
4. **Employer features** ✅ — add employees, create and assign tasks with due dates, form validation. Concept: service layer, DTOs.
5. **Employee features** ✅ — "My tasks" list, update status (TODO, IN_PROGRESS, DONE). Concept: authorization (an employee can only touch their own tasks).
6. **Carry forward** ✅ — a `@Scheduled` job that runs daily, moves overdue unfinished tasks to today, and counts how many times each was carried. Concept: scheduling and transactions.
7. **Monitoring dashboard** ✅ — per-employee completion stats, overdue and carried-forward lists, filters.
8. **Polish for the resume** ✅ (mostly) — MySQL profile, Docker, REST API, more features and tests. Still to do: deploy and add screenshots here.
9. **Extra features** ✅ — edit/delete, priority, search and filters, comments, history, reminder emails, charts, change password.

## Features

- **Two logins:** Employer and Employee, BCrypt-hashed passwords, each role locked to its own pages
- **Team management:** employers add employees (duplicate emails rejected)
- **Tasks:** create, **edit** and **delete** tasks with a title, description, due date and **priority** (Low, Medium, High)
- **Status updates:** employees move their own tasks through To do, In progress and Done
- **Search and filters:** search text, employee, status, priority and "Late only" on both dashboards; tables sort most urgent first
- **Task page:** details, **comments** from the employer and the assigned employee, and a **history** of every change
- **Daily carry-forward:** a scheduled job moves unfinished overdue tasks to today and counts each carry
- **Monitoring dashboard:** overview numbers, **charts** (Chart.js), per-employee completion %, "Needs attention" list
- **Reminder emails:** every morning at 08:00, one email per employee listing what is due today
- **Change password** for every user
- **REST API** at `/api/tasks` (JSON, HTTP Basic login)
- **MySQL profile and Docker Compose** for a real database

## Run it

You need Java 21 and Maven (or IntelliJ IDEA Community, which bundles Maven).

```bash
mvn spring-boot:run      # start the app (in-memory H2 database, sample data)
mvn test                 # run the 56 tests
```

Open http://localhost:8080. Sample logins: `boss@tasktrail.com` / `boss123` (employer), `ravi@tasktrail.com` / `ravi123` and `anu@tasktrail.com` / `anu123` (employees).

### With MySQL

Either start MySQL yourself and run `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`
(settings in `application-mysql.properties`, overridable with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`),
or let Docker do everything:

```bash
docker compose up --build    # MySQL + TaskTrail, data kept in a volume
docker compose down          # stop (add -v to also delete the data)
```

Sample data is only added when the database is empty.

### Sending real reminder emails

Until you set this up, reminders are only written to the console log. To send them, add to
`application.properties` (Gmail example; use an App Password, never your real password, and don't commit it):

```properties
tasktrail.mail.enabled=true
tasktrail.mail.from=you@gmail.com
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=you@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### REST API

| Method | URL | Who | Body |
|---|---|---|---|
| GET | `/api/tasks?q=&status=&priority=&late=` | both (employees see their own) | |
| GET | `/api/tasks/{id}` | both | |
| POST | `/api/tasks` | employer | `{"title", "description", "dueDate", "priority", "assigneeId"}` |
| PATCH | `/api/tasks/{id}/status` | assigned employee | `{"status": "DONE"}` |
| DELETE | `/api/tasks/{id}` | employer | |

```bash
curl -u boss@tasktrail.com:boss123 http://localhost:8080/api/tasks
curl -u ravi@tasktrail.com:ravi123 -X PATCH -H "Content-Type: application/json" \
     -d '{"status":"DONE"}' http://localhost:8080/api/tasks/1/status
```

### Deploying

The `Dockerfile` builds a small image that any Docker host can run (Render, Railway, Fly.io, a VPS).
Create a MySQL database there, then set `SPRING_PROFILES_ACTIVE=mysql`, `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

## Code map

```
config/      SecurityConfig, DataSeeder, CarryForwardJob, ReminderJob
controller/  pages (Dashboard, EmployerTask, EmployerEmployee, EmployeeTask, TaskDetail, Account, Login, Home)
             and the REST API (ApiTaskController, ApiErrorHandler)
dto/         form and response objects (TaskForm, TaskFilter, PasswordForm, TaskResponse, ...)
model/       entities: User, Task, Comment, TaskEvent; enums Role, TaskStatus, Priority
repository/  Spring Data interfaces
service/     the rules: TaskService, UserService, DashboardService, CarryForwardService, ReminderService
templates/   Thymeleaf pages; static/ holds app.css and Chart.js
```
# Task-Trail
