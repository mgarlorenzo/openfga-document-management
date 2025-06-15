# Document Management System

A Spring Boot application that manages documents with fine-grained access control using OpenFGA.

## Features

- Document management with personal and department documents
- Role-based access control using OpenFGA
- Department organization with hierarchical structure
- Manager hierarchy for document access
- HR department with special access to all documents
- GraphQL API for document operations

## Data Structure

### Department Hierarchy

The system implements a hierarchical structure with the following levels:

1. **HR Department**
   - Melissa Garcia (Department Boss)
     - Rosa Martinez (Manager)
       - David Lopez (Employee)

2. **Sales Department**
   - Maria Rodriguez (Manager)
     - Juan Sanchez (Manager)
       - Pedro Fernandez (Employee)
       - Pablo Gonzalez (Employee)

3. **Marketing Department**
   - Sergio Perez (Manager)
     - Ana Ruiz (Employee)
     - Xavi Moreno (Employee)

## Authorization Rules

### 1. Personal Documents
- **Owner**: Full access
- **Owner's Manager**: Read access
- **HR Department Users**: Read access to all documents

### 2. Department Documents
- **Owner**: Full access
- **Department Members**: Read access
- **Owner's Manager**: Read access
- **HR Department Users**: Read access to all documents

## OpenFGA Implementation

### Authorization Model Types

1. User Type:
   ```json
   {
      "type": "user",
      "relations": {
        "manager": { "this": {} }
      },
      "metadata": {
        "relations": {
          "manager": {
            "directly_related_user_types": [
              { "type": "user" }
            ]
          }
        }
      }
    }
   ```

2. Department Type:
   ```json
   {
      "type": "department",
      "relations": {
        "member": { "this": {} }
      },
      "metadata": {
        "relations": {
          "member": {
            "directly_related_user_types": [
              { "type": "user" }
            ]
          }
        }
      }
    }
   ```

3. Document Type:
   ```json
  {
      "type": "document",
      "relations": {
        "owner": {
          "this": {}
        },
        "reader": {
          "union": {
            "child": [
              { "this": {} },
              {
                "tupleToUserset": {
                  "tupleset": {
                    "relation": "owner"
                  },
                  "computedUserset": {
                    "relation": "manager"
                  }
                }
              }
            ]
          }
        },
        "email_visible_to": {
          "union": {
            "child": [
              { "this": {} },
              {
                "tupleToUserset": {
                  "tupleset": {
                    "relation": "owner"
                  },
                  "computedUserset": {
                    "relation": "manager"
                  }
                }
              }
            ]
          }
        }
      },
      "metadata": {
        "relations": {
          "owner": {
            "directly_related_user_types": [
              { "type": "user" }
            ]
          },
          "reader": {
            "directly_related_user_types": [
              { "type": "user" },
              { "type": "department", "relation": "member" }
            ]
          },
          "email_visible_to": {
            "directly_related_user_types": [
              { "type": "user" }
            ]
          }
        }
      }
    }
   ```

### Components

1. **OpenFGAConfig**
   - OpenFGA client configuration
   - Connection with OpenFGA server using `application.properties`

2. **AuthorizationService**
   - `canReadDocument()`: Verifies if an employee can read a specific document
   - `createEmployeeTuples()`: Creates relation tuples when an employee is created
   - `createDocumentTuples()`: Creates relation tuples when a document is created

3. **DocumentService (Modified)**
   - `getAccessibleDocuments()`: Now filters documents based on OpenFGA permissions
   - `getDocumentById()`: New version that verifies permissions before returning document
   - `createDocument()`: Creates OpenFGA tuples automatically when creating documents

4. **DocumentResolver (Modified)**
   - GraphQL endpoints now verify authentication and authorization
   - Only returns documents for which the user has permissions

5. **EmployeeResolver (Modified)**
   - Creates OpenFGA tuples automatically when creating employees

6. **DataSeeder (Modified)**
   - Creates OpenFGA tuples for all test data on initialization

## Getting Started

1. Clone the repository
2. Configure environment variables:
   ```properties
   # OpenFGA Configuration
   openfga.api-url=http://localhost:9080
   openfga.store-id=01JQ8M4HYK516V41AEBA42PFCR
   openfga.api-token=mysecret
   ```
3. Run OpenFGA server:
   ```bash
   docker run --rm -p 9080:8080 openfga/openfga run
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

### 1. Get All Employees (No authorization required)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { employees { id name surname email department { name } manager { name surname } } }"
  }'
```

### 2. Get Accessible Documents by User

**HR User (sees all documents):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrBoss:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**HR Manager (sees HR documents + subordinates):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrManager1:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**HR Employee (sees HR documents):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Sales Boss (sees department documents + subordinates):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'salesBoss:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Marketing Employee (sees only department documents):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'random0_marketing:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

### 3. Get Specific Document by ID

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "query { document(id: 1) { id title content classificationLevel department { name } owner { name surname } } }"
  }'
```

### 4. Create New Document (Creates OpenFGA tuples automatically)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "mutation($input: CreateDocumentInput!, $employeeId: ID!) { createDocument(input: $input, employeeId: $employeeId) { id title classificationLevel owner { name } department { name } } }",
    "variables": {
      "input": {
        "title": "New Test Document",
        "content": "Document content",
        "departmentId": 1,
        "classificationLevel": "personal"
      },
      "employeeId": "5"
    }
  }'
```

### 5. Create New Employee (Creates OpenFGA tuples automatically)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation($input: CreateUserInput!) { createEmployee(input: $input) { id name surname email department { name } } }",
    "variables": {
      "input": {
        "nationalIdType": "DNI",
        "nationalIdNumber": "12345678Z",
        "issuingCountry": "Spain",
        "name": "Juan",
        "surname": "Pérez",
        "address": "Calle Test 123",
        "username": "jperez",
        "email": "jperez@company.com",
        "location": "Madrid",
        "departmentId": 1
      }
    }
  }'
```

## Expected Behavior

### HR User
- **Sees**: All system documents
- **Reason**: HR has special access to all documents according to rules

### Department Manager
- **Sees**: Department documents + subordinate documents
- **Reason**: Managers have access to documents of employees they supervise

### Regular Employee
- **Sees**: Department documents + own documents
- **Reason**: Employees have access to departmental and personal documents

### Users from Other Departments
- **Sees**: Only documents from their own department
- **Reason**: No hierarchical or departmental relationship, limited access

## Troubleshooting

### Error: "OpenFGA server not reachable"
- Verify OpenFGA is running on port 9080
- Check configuration in application.properties

### Error: "No tuples created"
- Check application logs
- Confirm store-id is correct
- Verify OpenFGA authentication

### Documents not filtered correctly
- Verify tuples were created correctly
- Check AuthorizationService logs
- Confirm OpenFGA model is loaded

## Next Steps

To improve the implementation:
1. Add cache for permission checks
2. Implement bulk permission checks
3. Add performance metrics
4. Implement tuple cleanup when deleting entities
5. Add integration tests for authorization
