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

### Access Control Rules
1. Personal Documents:
   - Owner has full access
   - Owner's manager has read access
   - HR department users have read access

2. Department Documents:
   - Owner has full access
   - Department members have read access
   - Owner's manager has read access
   - HR department users have read access


## OpenFGA Configuration

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

## Getting Started

1. Clone the repository
2. Configure environment variables:
   ```properties
   OPENFGA_API_URL=http://localhost:9090
   OPENFGA_API_TOKEN=your_api_token
   OPENFGA_STORE_ID=your_store_id
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

### GraphQL Queries

1. Get All Users:
   ```graphql
   query {
     users {
       id
       name
       department {
         name
       }
       manager {
         name
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { users { id name department { name } manager { name } } }"
     }'
   ```
   # Returns:
   # - List of all users in the system
   # - Each user's department information
   # - Each user's manager information

2. Get User's Documents:
   ```graphql
   query {
     user(id: "1") {
       name
       documents {
         id
         title
         description
         department {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { user(id: \"1\") { name documents { id title description department { name } } } }"
     }'
   ```

3. Get Department Documents:
   ```graphql
   query {
     department(id: "1") {
       name
       documents {
         id
         title
         description
         owner {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { department(id: \"1\") { name documents { id title description owner { name } } } }"
     }'
   ```

### Example Permission Verification

1. Regular Employee Access:
   ```graphql
   # Login as a regular employee (e.g., Pedro Fernandez)
   query {
     user(id: "6") {
       name
       documents {
         id
         title
         description
         department {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { user(id: \"6\") { name documents { id title description department { name } } } }"
     }'
   ```
   # Should see:
   # - Their personal document
   # - Documents from the Sales department
   # - Documents owned by their manager (Juan Sanchez)

2. Manager Access:
   ```graphql
   # Login as a manager (e.g., Juan Sanchez)
   query {
     user(id: "5") {
       name
       documents {
         id
         title
         description
         department {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { user(id: \"5\") { name documents { id title description department { name } } } }"
     }'
   ```
   # Should see:
   # - Their own documents
   # - Documents of their subordinates (Pedro and Pablo)
   # - All Sales department documents

3. HR Department Access:
   ```graphql
   # Login as an HR user (e.g., Rosa Martinez)
   query {
     user(id: "3") {
       name
       documents {
         id
         title
         description
         department {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { user(id: \"3\") { name documents { id title description department { name } } } }"
     }'
   ```
   # Should see:
   # - All documents across all departments
   # - Documents from HR, Sales, and Marketing departments
   # - Documents owned by all users in the system

4. Department Boss Access:
   ```graphql
   # Login as a department boss (e.g., Melissa Garcia)
   query {
     user(id: "1") {
       name
       documents {
         id
         title
         description
         department {
           name
         }
       }
     }
   }
   ```
   ```bash
   curl -X POST http://localhost:9090/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "query { user(id: \"1\") { name documents { id title description department { name } } } }"
     }'
   ```
   # Should see:
   # - All documents in the HR department
   # - Documents of all HR department subordinates (Rosa and David)
   # - Their own personal documents

## Data Seeding

The application automatically seeds data on startup:

1. Departments:
   - Sales
   - HR
   - Product

2. Users per Department:
   - 1 Department Boss (LEVEL3)
   - 2 Managers (LEVEL2)
   - 7 Regular Employees (LEVEL1)

3. Documents per User:
   - 2 Personal Documents
   - 8 Department Documents

4. Special Access:
   - HR department users get read access to all documents
   - Department members get read access to department documents
   - Managers get read access to their subordinates' documents

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request 
## Deployment to Google Cloud Run

A GitHub Actions workflow is included to build and deploy the application. Configure these repository secrets:
- `GCP_PROJECT`: your Google Cloud project ID
- `GCP_REGION`: target Cloud Run region
- `WORKLOAD_IDENTITY_PROVIDER`: Workload Identity Provider name
- `SERVICE_ACCOUNT`: service account email with Cloud Run permissions

With the secrets in place, pushes to `main` automatically trigger a deployment.
