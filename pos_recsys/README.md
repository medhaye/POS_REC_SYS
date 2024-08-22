# PosRecSysApplication

## Project Purpose & Scope
The objective of the project is to develop a Spring Boot application that processes POS (Point of Sale) transactions to extract and store association rules in a PostgreSQL database. These rules are also saved in a CSV file, with a focus on automating the application's execution on a configurable schedule.

### Intended Audience
The intended audience for this application includes businesses or analysts who need to analyze POS transaction data to discover patterns and associations between products. This application can help in making informed decisions related to:
* **Product Bundling:** Identifying commonly bought product groups for creating bundles.
* **Cross-Selling:** Discovering products often bought together for better recommendations.
* **Inventory Optimization:** Optimizing stock based on associated product purchases.
* **Targeted Marketing:** Designing marketing campaigns based on product associations.

---

## System Architecture & Design

### Layered Architecture
The project follows a layered architecture to ensure a clear separation of concerns. The layers are structured as follows:

#### 1. Presentation Layer (Spring Boot Application Layer)
* #### PosRecSysApplication:
  * Entry point of the Spring Boot application.
  * Initializes the application context, executes the main business logic, and orchestrates interactions between components.
  * Handles scheduling and execution of tasks, with configurable options for automatic execution (daily, weekly, etc.).

#### 2. Service Layer
* #### SPMFAnalyzer:
  * Handles core business logic related to association rule mining. 
  * Interacts with the database and file system, processes orders, converts transactions, and calculates frequent itemsets and association rules using the SPMF library.
* #### Authenticator:
  * Manages authentication and obtains access tokens required to fetch orders from an external service.
* #### OrderFetcher:
    * Fetches orders from an external system using the authenticated token.
* #### OrderProcessor:
  * Processes raw order data into a format suitable for analysis, converting JSON data into a list of transactions.

#### 3. Repository Layer
* #### AssociationRuleRepository:
  * Provides methods to perform CRUD operations on the **association_rules** table in the PostgreSQL database.
  * Extends Spring Data JPA’s **JpaRepository**, offering built-in methods for data persistence.

#### 4. Domain/Entity Layer
* #### AssociationRule:
  * A JPA entity class representing the association rules stored in the database.
  * Includes fields such as **id**, **antecedents**, **consequents**, **confidence**, **lift**, and **support**.

#### 5. Integration Layer
* #### PostgreSQL Database:
  * Stores computed association rules using the JPA repository pattern.
  * The **SPMFAnalyzer** interacts with the database to store, retrieve, and delete records.

### Interaction Between Components
The interaction between components follows a specific flow for processing orders, extracting transactions, performing association rule mining, and storing results in the database:

#### 1. Spring Boot Application Layer:
* The process begins with **PosRecSysApplication**, which triggers the **SPMFAnalyzer** to start the analysis.

#### 2. Authentication and Data Fetching:
* The **Authenticator** authenticates with the external system and retrieves an access token.
* The **OrderFetcher** uses this token to fetch raw order data from the external system.

#### 3. Data Processing:
* The **OrderProcessor** processes raw order data into a list of transactions. This step converts the JSON data into a structure that the **SPMFAnalyzer** can use.

#### 4. Analysis:
* **SPMFAnalyzer:**
  * Converts transactions into a format compatible with the SPMF algorithm.
  * Calculates frequent itemsets using the FP-Growth algorithm from the SPMF library.
  * To learn more about the Frequent Pattern Growth Algorithm, here is this link: https://www.geeksforgeeks.org/frequent-pattern-growth-algorithm/
  * Extracts association rules based on frequent itemsets.
  * To learn more about the association rules, here is this link: https://www.geeksforgeeks.org/association-rule/
  * Deletes old rules from the **association_rules** table and inserts newly generated rules.

#### 5. Data Persistence:
* **AssociationRuleRepository:**
  * The extracted rules are saved into the PostgreSQL database through the repository interface.
  * The rules are also outputted to a CSV file (**associations.csv**), providing a persistent, human-readable format for further analysis.

---

## Installation & Setup Instructions

### Prerequisites and Dependencies

#### 1. Java Development Kit (JDK):
* Version: JDK 17 or higher.

#### 2. Maven:
* Version: Maven 3.6.0 or higher.

#### 3. PostgreSQL:
* Version: PostgreSQL 16.3 or higher.
* Installation Path: Ensure PostgreSQL is installed and accessible from the command line.

#### 4. SPMF Library:
* Location: Ensure the SPMF library JAR is included in the **libs** directory or added as a dependency in **pom.xml**.

### Installation Steps

#### 1. Clone the Repository:
* **git clone ...etc**

#### 2. Navigate to the Project Directory:
* **cd .\pos_recsys\pos_recsys**

#### 3. Build the Project:
* **mvn clean install**

#### 4. Configure PostgreSQL:
* Ensure PostgreSQL is running.

---

## Usage Instructions

### Running the Application

#### 1. Run the Application:
* Use: **mvn spring-boot:run**
* Alternatively, execute **PosRecSysApplication.java** from your IDE.

#### 2. Viewing Outputs:
* **CSV File:** Generated at the project root as **associations.csv**.
* **Database Records:** Association rules are stored in the **association_rules** table in PostgreSQL.

### Interacting with Outputs

* **CSV:** Open **associations.csv** in any text editor or spreadsheet software.
* **Database:** Query **association_rules** in your PostgreSQL database using any SQL client.

---

## Automated Task Configuration

### ScheduledTasks.java Explanation:
The **ScheduledTasks** class is a **Spring** component responsible for scheduling and executing the **SPMFAnalyzer** at regular intervals.

#### The class executes the analysis every minute!

#### In the Code We Have:
* The **@Scheduled** annotation defines when the task should run, using a **cron expression**. In the code, it is set to execute every minute.
* The **SPMFAnalyzer** is injected into this class and is responsible for performing the **association rule mining**.

### Configuring *minSupport* and *Confidence*:
The **minSupport** and **confidence** parameters can be adjusted to fit your specific analysis needs. **Changing** these parameters significantly **affects** the output:

* **minSupport:** The minimum support threshold determines the least number of transactions a product association must appear in to be considered significant.
* **Confidence:** The confidence threshold indicates the likelihood of the association being accurate.

### Important:
Adjust these values with caution, as lower **minSupport** and **confidence** values can lead to a large number of insignificant or spurious rules, while higher values might result in missing important associations.

---

## Testing & Validation
This section can be expanded upon depending on the testing strategies used in the project. If you plan to include testing, describe the testing framework, test cases, and how to execute tests.

---

## Documentation & REST APIs
Although not covered in detail for this project, if the application exposes any REST APIs, you can document them here. This section can include API endpoints, request/response formats, and authentication methods.

---

## Additional Information

### Commented Code in *PosRecSysApplication.java*:
This part of the code **has been commented** out because the application is configured to **run using ScheduledTasks**.
The *commented* **CommandLineRunner** is an **alternative** approach that allows running the analysis **manually** or at application **startup** with specific parameters for *minSupport* and *confidence*.
This gives the user flexibility in *choosing between* a **scheduled run** or a **manual trigger**.

If you prefer a **manual trigger** instead of *scheduling*, you can **uncomment this section** and set the desired values directly in the *commandLineRunner method*.

### Also *Important*
As mentioned earlier, in the ScheduledTasks.java, you can adjust the minSupport and confidence according to your preferences:

* However, it’s essential to understand that these **parameters**, when **changed**, can significantly **alter** the output.
* Always choose suitable values for **minSupport** and **confidence** based on your domain knowledge and data characteristics.
* **Lowering** these thresholds may **increase** the number of *generated rules*, but might also **include** irrelevant or less significant associations.
* *Conversely*, **increasing** them may yield fewer, but potentially more **meaningful rules**.

---

## This concludes the project documentation.

### This documentation should cover the primary aspects of your project and provide a clear, structured guide for setting up, using, and understanding the application.

### You can customize and expand upon these sections as needed to fit your project’s specific requirements.

---