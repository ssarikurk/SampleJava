# <img src="images/selenium.png" width="30"> <img src="images/cucumber.png" width="30"> Sample Java-Selenium-Cucumber Framework

This project is a **sample automation framework** built using **Selenium WebDriver** and **Cucumber (BDD)**.  
It is designed as a scalable template to automate:

- **UI Testing**
- **API Testing** 
- **Database Testing** (MongoDB, MySQL)

The goal is to create a **flexible, easy-to-extend** automation structure where UI, API, and Database layers can be covered with real examples.

---

## 📦 Purpose

> Provide a structured, modular, and scalable automation framework template for learning and real-world projects.
> 
> This project uses publicly available test sites and is intended for educational and demonstration purposes only.

---

## 🛠 Tech Stack

- **Java**
- **Selenium WebDriver**
- **Cucumber (BDD Framework)**
- **JUnit** 
- **Maven** (Dependency Management and Build Tool)

---

## 🗂 Project Structure

| Folder | Purpose |
|--------|--------|
| `pages/` | Page Object Model (POM) classes (web elements & methods) |
| `step_definitions/` | Cucumber step definitions |
| `runners/` | Test runners (Cucumber + JUnit integration) |
| `utilities/` | Utility classes (e.g., `ConfigurationReader`, `Driver`) |
| `features/` | Cucumber `.feature` files (Gherkin scenarios) |
| `configuration.properties` | Project settings (e.g., baseUrl, credentials) stored in the root directory |

---

## 🔧 Key Utility Classes

| Category           | Classes                    | Highlights                                                         |
|--------------------|----------------------------|--------------------------------------------------------------------|
| **Test Core**      | `Driver`, `ETestFramework` | Parallel execution over SeleniumHub Docker, framework integrations |
| **Data Tools**     | `ExcelUtil`, `MongoDBUtils`, `SqlUtils` | Excel/MongoDB/SQL operations                                       |
| **Cloud/DevOps**  | `AwsUtils`, `JenkinsUtils` | AWS S3, CI/CD pipelines                                            |
| **Helpers**       | `DateUtils`, `LogUtils`, `RandomUtils` | Logging, randomized data, file operations                          |

---

## ⚙️ How to Run

1. Clone or download the repository.
2. Open the project in **IntelliJ IDEA** (or any Java IDE).
3. Ensure Maven dependencies are installed (`mvn clean install`).
4. Run the `CukesRunner.java` class to execute all scenarios.
5. Alternatively, use Maven command-line to execute tests:

```bash
mvn clean test

