# Group Insurance Microservices (Spring Boot 4.0.6)

A multi-module Spring Boot 4.0.6 microservice project engineered for dual running modes:
1. **Standalone Spring Application** (in IntelliJ IDEA, Eclipse, or `java -jar`) using embedded Tomcat.
2. **External Deployment on Apache Tomcat 10.1.0** as a WAR archive utilizing Jakarta EE 10 (`jakarta.servlet.*`).

---

## 1. Project Architecture

```
group-insurance/
├── pom.xml                                 # Parent POM (vn.com.group.insurance:root)
├── common/                                 # Common Library Module
│   ├── pom.xml                             # Reusable JAR packaging
│   └── src/main/java/
│       ├── jp/xet/sparwings/spring/data/repository/
│       │   ├── DbRepository.java           # Base DbRepository<E, ID extends Serializable>
│       │   ├── ScannableRepository.java    # Scannable repository interface
│       │   ├── BatchReadableRepository.java# Batch readable repository interface
│       │   └── BatchWritableRepository.java# Batch writable repository interface
│       └── vn/com/group/insurance/common/
│           ├── constant/CommonConstants.java
│           ├── dto/ApiResponse.java        # Standard REST response wrapper
│           ├── dto/ErrorDetail.java
│           ├── dto/PageResponse.java
│           ├── entity/BaseEntity.java      # MappedSuperclass with audit & versioning
│           ├── exception/BaseException.java
│           ├── exception/ResourceNotFoundException.java
│           ├── exception/BadRequestException.java
│           ├── exception/GlobalExceptionHandler.java
│           └── util/DateTimeUtils.java & JsonUtils.java
└── proposal/                               # Proposal Microservice Module
    ├── pom.xml                             # Executable WAR packaging
    └── src/
        ├── main/
        │   ├── java/vn/com/group/insurance/proposal/
        │   │   ├── ProposalApplication.java# Extends SpringBootServletInitializer
        │   │   ├── config/
        │   │   │   ├── SqlServerDataSourceConfig.java # Primary DB: Microsoft SQL Server
        │   │   │   ├── Db2DataSourceConfig.java       # Secondary DB: IBM DB2
        │   │   │   └── WebMvcConfig.java
        │   │   ├── dto/
        │   │   │   ├── ProposalCreateRequest.java
        │   │   │   ├── ProposalUpdateRequest.java
        │   │   │   ├── ProposalResponse.java
        │   │   │   ├── PolicyRecordDto.java
        │   │   │   └── BatchProposalRequest.java
        │   │   ├── entity/
        │   │   │   ├── sqlserver/ProposalEntity.java
        │   │   │   └── db2/PolicyRecordEntity.java
        │   │   ├── repository/
        │   │   │   ├── sqlserver/ProposalRepository.java  # Extends DbRepository
        │   │   │   └── db2/PolicyRecordRepository.java    # Extends DbRepository
        │   │   ├── service/
        │   │   │   ├── ProposalService.java
        │   │   │   └── impl/ProposalServiceImpl.java
        │   │   └── controller/
        │   │       └── ProposalController.java
        │   └── resources/
        │       ├── application.yml         # Production/Default config (SQL Server + DB2)
        │       └── application-local.yml   # Standalone local development profile
        └── test/
            ├── java/vn/com/group/insurance/proposal/
            │   └── ProposalApplicationTests.java
            └── resources/application.yml
```

---

## 2. DbRepository & `jp.xet.sparwings.spring.data.repository`

The repository contract complies directly with the requested specification:

```java
package jp.xet.sparwings.spring.data.repository;

import org.springframework.data.repository.NoRepositoryBean;
import java.io.Serializable;

@NoRepositoryBean
public interface DbRepository<E, ID extends Serializable>
        extends ScannableRepository<E, ID>,
                BatchReadableRepository<E, ID>,
                BatchWritableRepository<E, ID> {
}
```

* **`ScannableRepository<E, ID>`**: Provides `findAll()`, `findAll(Sort)`, and `findAll(Pageable)`.
* **`BatchReadableRepository<E, ID>`**: Provides `findById(ID)`, `existsById(ID)`, `findAllById(Iterable<ID>)`, and `count()`.
* **`BatchWritableRepository<E, ID>`**: Provides `save(S)`, `saveAll(Iterable<S>)`, `deleteById(ID)`, `delete(E)`, `deleteAllById(Iterable<ID>)`, `deleteAll(Iterable<E>)`, and `deleteAll()`.

Spring Data JPA auto-implements all these methods at runtime on any repository interface extending `DbRepository` (e.g. [`ProposalRepository`](file:///D:/Development/Projects/group-insurance/proposal/src/main/java/vn/com/group/insurance/proposal/repository/sqlserver/ProposalRepository.java) and [`PolicyRecordRepository`](file:///D:/Development/Projects/group-insurance/proposal/src/main/java/vn/com/group/insurance/proposal/repository/db2/PolicyRecordRepository.java)).

---

## 3. Multiple DataSources (SQL Server & DB2)

* **Primary Datasource (SQL Server)**:
  * Configured in [`SqlServerDataSourceConfig`](file:///D:/Development/Projects/group-insurance/proposal/src/main/java/vn/com/group/insurance/proposal/config/SqlServerDataSourceConfig.java)
  * Base packages scanned: `vn.com.group.insurance.proposal.repository.sqlserver`
  * Entity packages scanned: `vn.com.group.insurance.proposal.entity.sqlserver`
  * Uses HikariCP: `SqlServerHikariPool`
  * Transaction Manager: `sqlServerTransactionManager` (Primary)

* **Secondary Datasource (IBM DB2)**:
  * Configured in [`Db2DataSourceConfig`](file:///D:/Development/Projects/group-insurance/proposal/src/main/java/vn/com/group/insurance/proposal/config/Db2DataSourceConfig.java)
  * Base packages scanned: `vn.com.group.insurance.proposal.repository.db2`
  * Entity packages scanned: `vn.com.group.insurance.proposal.entity.db2`
  * Uses HikariCP: `Db2HikariPool`
  * Transaction Manager: `db2TransactionManager`

---

## 4. How to Run

### Mode A: Standalone Execution (IntelliJ IDEA / Eclipse)

The application can be run directly using the `main` method in [`ProposalApplication.java`](file:///D:/Development/Projects/group-insurance/proposal/src/main/java/vn/com/group/insurance/proposal/ProposalApplication.java):

1. **IntelliJ IDEA**:
   - Right click `ProposalApplication.java` -> **Run 'ProposalApplication.main()'**.
   - Optional: In Run Configuration, set Active Profiles to `local` (uses local in-memory databases without requiring live external SQL Server/DB2).
2. **Eclipse**:
   - Right click `ProposalApplication.java` -> **Run As** -> **Java Application** or **Spring Boot App**.
3. **Command Line Standalone**:
   ```bash
   # Run with live SQL Server & DB2 configured in application.yml
   .\mvnw.cmd spring-boot:run -pl proposal

   # Or run with local profile for offline development
   java "-Dspring.profiles.active=local" -jar proposal/target/proposal.war
   ```

### Mode B: Deploy to Apache Tomcat 10.1.0

Apache Tomcat 10.1.x implements Jakarta EE 10 (`jakarta.servlet.*`), which matches Spring Boot 4:

1. Build the production WAR package:
   ```bash
   .\mvnw.cmd clean package
   ```
2. The generated WAR is located at:
   `proposal/target/proposal.war`
3. Copy `proposal.war` into the Tomcat `webapps/` directory:
   ```bash
   cp proposal/target/proposal.war /path/to/apache-tomcat-10.1.0/webapps/
   ```
4. Start Apache Tomcat (`bin/startup.bat` or `bin/startup.sh`).
5. The application will be accessible at:
   `http://localhost:8080/proposal/api/v1/proposals`

---

## 5. REST API Endpoints

| HTTP Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/proposals` | Create a new proposal in SQL Server |
| `GET` | `/api/v1/proposals` | Get all proposals (using `ScannableRepository.findAll`) |
| `GET` | `/api/v1/proposals/{id}` | Get proposal by ID (using `BatchReadableRepository.findById`) |
| `GET` | `/api/v1/proposals/number/{proposalNumber}` | Find proposal by unique number |
| `PUT` | `/api/v1/proposals/{id}` | Update proposal |
| `DELETE` | `/api/v1/proposals/{id}` | Delete proposal (using `BatchWritableRepository.deleteById`) |
| `POST` | `/api/v1/proposals/batch` | Batch insert proposals (using `BatchWritableRepository.saveAll`) |
| `GET` | `/api/v1/proposals/db2/policies/{policyNumber}` | Retrieve policy record from IBM DB2 |
| `GET` | `/api/v1/proposals/db2/customers/{customerCode}/policies` | Retrieve policies from IBM DB2 by customer |
| `POST` | `/api/v1/proposals/{id}/link-db2-policy/{policyNumber}` | Cross-database linking between SQL Server & DB2 |
