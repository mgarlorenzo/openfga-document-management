# OpenFGA Authorization Implementation

## Overview

Esta implementación añade controles de autorización reales usando OpenFGA al sistema de gestión de documentos. Ahora los usuarios solo pueden ver y acceder a documentos para los cuales tienen permisos específicos según las reglas definidas en el modelo OpenFGA.

## Reglas de Autorización Implementadas

### 1. Documentos Personales
- **Propietario**: Acceso completo
- **Manager del propietario**: Acceso de lectura
- **Usuarios de HR**: Acceso de lectura a todos los documentos

### 2. Documentos Departamentales
- **Propietario**: Acceso completo
- **Miembros del departamento**: Acceso de lectura
- **Manager del propietario**: Acceso de lectura
- **Usuarios de HR**: Acceso de lectura a todos los documentos

## Componentes Implementados

### 1. OpenFGAConfig
- Configuración del cliente OpenFGA
- Conexión con servidor OpenFGA usando configuración de `application.properties`

### 2. AuthorizationService
- `canReadDocument()`: Verifica si un empleado puede leer un documento específico
- `createEmployeeTuples()`: Crea tuplas de relación cuando se crea un empleado
- `createDocumentTuples()`: Crea tuplas de relación cuando se crea un documento

### 3. DocumentService (Modificado)
- `getAccessibleDocuments()`: Ahora filtra documentos basado en permisos OpenFGA
- `getDocumentById()`: Nueva versión que verifica permisos antes de devolver documento
- `createDocument()`: Crea tuplas OpenFGA automáticamente al crear documentos

### 4. DocumentResolver (Modificado)
- Los endpoints GraphQL ahora verifican autenticación y autorización
- Solo devuelve documentos para los cuales el usuario tiene permisos

### 5. EmployeeResolver (Modificado)
- Crea tuplas OpenFGA automáticamente al crear empleados

### 6. DataSeeder (Modificado)
- Crea tuplas OpenFGA para todos los datos de prueba al inicializar

## Comandos curl Actualizados

### Configuración del Servidor OpenFGA

Antes de usar la aplicación, asegúrate de que OpenFGA esté ejecutándose:

```bash
# Ejecutar OpenFGA localmente (puerto 9080)
docker run --rm -p 9080:8080 openfga/openfga run
```

### 1. Obtener Lista de Empleados (Sin cambios - no requiere autorización)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { employees { id name surname email department { name } manager { name surname } } }"
  }'
```

### 2. Obtener Documentos Accesibles por Usuario

**Usuario HR (ve todos los documentos):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrBoss:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Manager de HR (ve documentos de HR + subordinados):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrManager1:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Empleado de HR (ve documentos de HR):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Jefe de Ventas (ve documentos de su departamento + subordinados):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'salesBoss:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

**Empleado de Marketing (ve solo documentos de su departamento):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'random0_marketing:password123' | base64)" \
  -d '{
    "query": "query { documents { id title classificationLevel department { name } owner { name surname } } }"
  }'
```

### 3. Obtener Documento Específico por ID

**Con autorización (verifica permisos):**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "query { document(id: 1) { id title content classificationLevel department { name } owner { name surname } } }"
  }'
```

### 4. Crear Nuevo Documento (Crea tuplas OpenFGA automáticamente)

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'hrEmployee1:password123' | base64)" \
  -d '{
    "query": "mutation($input: CreateDocumentInput!, $employeeId: ID!) { createDocument(input: $input, employeeId: $employeeId) { id title classificationLevel owner { name } department { name } } }",
    "variables": {
      "input": {
        "title": "Nuevo Documento de Prueba",
        "content": "Contenido del documento",
        "departmentId": 1,
        "classificationLevel": "personal"
      },
      "employeeId": "5"
    }
  }'
```

### 5. Crear Nuevo Empleado (Crea tuplas OpenFGA automáticamente)

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

## Comportamiento Esperado

### Usuario HR
- **Ve**: Todos los documentos del sistema
- **Razón**: HR tiene acceso especial a todos los documentos según las reglas

### Manager de Departamento
- **Ve**: Documentos de su departamento + documentos de sus subordinados
- **Razón**: Los managers tienen acceso a documentos de empleados que supervisan

### Empleado Regular
- **Ve**: Documentos de su departamento + sus propios documentos
- **Razón**: Los empleados tienen acceso a documentos departamentales y personales

### Usuarios de Otros Departamentos
- **Ve**: Solo documentos de su propio departamento
- **Razón**: Sin relación jerárquica o departamental, acceso limitado

## Logs de Verificación

La aplicación registra información detallada sobre:
- Creación de tuplas OpenFGA
- Verificaciones de permisos
- Acceso denegado/concedido a documentos
- Errores de autorización

Revisar los logs de la aplicación para verificar el funcionamiento correcto:

```bash
# Ver logs de la aplicación
docker logs <container-id>

# O si está ejecutándose localmente
# Revisar la consola donde se ejecuta ./mvnw spring-boot:run
```

## Configuración Requerida

Asegúrate de que `application.properties` tenga:

```properties
# OpenFGA Configuration
openfga.api-url=http://localhost:9080
openfga.store-id=01JQ8M4HYK516V41AEBA42PFCR
openfga.api-token=mysecret
```

## Troubleshooting

### Error: "OpenFGA server not reachable"
- Verificar que OpenFGA esté ejecutándose en puerto 9080
- Verificar configuración en application.properties

### Error: "No tuples created"
- Verificar logs de la aplicación
- Confirmar que el store-id es correcto
- Verificar autenticación con OpenFGA

### Documentos no filtrados correctamente
- Verificar que las tuplas se crearon correctamente
- Revisar logs de AuthorizationService
- Confirmar que el modelo OpenFGA está cargado

## Próximos Pasos

Para mejorar la implementación se puede:
1. Añadir cache para verificaciones de permisos
2. Implementar bulk permission checks
3. Añadir métricas de rendimiento
4. Implementar cleanup de tuplas al eliminar entidades
5. Añadir tests de integración para autorización 