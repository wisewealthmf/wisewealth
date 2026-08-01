# WiseWealth API - Spring Boot Backend

A comprehensive REST API for the WiseWealth financial planning application built with Spring Boot 3.2.0 and Java 21.

## Features

- User authentication and authorization using JWT
- Consultation booking management
- Query/Lead management system
- Admin dashboard APIs
- Role-based access control (User/Admin)
- PostgreSQL database integration
- Exception handling and validation

## Prerequisites

- **Java 21** or higher
- **Maven 3.8.0** or higher
- **PostgreSQL 12** or higher
- **Git**

## Project Setup

### 1. Clone the Repository

```bash
cd /path/to/wisewealth
```

### 2. Database Setup

Create PostgreSQL database and run schema:

```bash
createdb wisewealth_db
psql -U postgres -d wisewealth_db -f ../POSTGRES_SCHEMA.sql
```

### 3. Update Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wisewealth_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 4. Update JWT Secret

Generate a secure JWT secret (minimum 256 bits):

```bash
# Generate random secret
openssl rand -base64 32
```

Update in `application.properties`:

```properties
jwt.secret=your_generated_secret_key_minimum_256_bits
```

## Project Structure

```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/wisewealth/
│   │   │   ├── WisewealthApiApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ConsultationBookingController.java
│   │   │   │   ├── QueryController.java
│   │   │   │   ├── AdminUserController.java
│   │   │   │   ├── AdminConsultationController.java
│   │   │   │   └── AdminQueryController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ConsultationBookingService.java
│   │   │   │   └── QueryService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ConsultationBookingRepository.java
│   │   │   │   └── QueryRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── ConsultationBooking.java
│   │   │   │   ├── Query.java
│   │   │   │   ├── StatusEnum.java
│   │   │   │   └── CategoryEnum.java
│   │   │   ├── dto/
│   │   │   │   ├── UserRegisterRequest.java
│   │   │   │   ├── UserLoginRequest.java
│   │   │   │   ├── UserLoginResponse.java
│   │   │   │   ├── UserDto.java
│   │   │   │   ├── ConsultationBookingDto.java
│   │   │   │   ├── QueryDto.java
│   │   │   │   └── ... (more DTOs)
│   │   │   ├── security/
│   │   │   │   └── JwtTokenProvider.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── README.md
```

## Building the Project

### Build with Maven

```bash
cd backend
mvn clean install
```

### Build Docker Image (Optional)

```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=wisewealth-api:latest
```

## Running the Application

### Run from Maven

```bash
cd backend
mvn spring-boot:run
```

### Run from IDE

1. Open project in IntelliJ IDEA or Eclipse
2. Navigate to `WisewealthApiApplication.java`
3. Right-click and select "Run"

### Run from JAR

```bash
cd backend
mvn clean package
java -jar target/wisewealth-api-1.0.0.jar
```

## API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Authentication Endpoints

**Register User**

```
POST /auth/register
Content-Type: application/json

{
  "name": "Sakshi Kumar",
  "email": "sakshi@example.com",
  "password": "securePassword123",
  "phone": "+91-9876543210"
}
```

**Login**

```
POST /auth/login
Content-Type: application/json

{
  "email": "sakshi@example.com",
  "password": "securePassword123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 3600,
  "userId": 1,
  "name": "Sakshi Kumar",
  "email": "sakshi@example.com"
}
```

### User Endpoints

**Get Current User Profile**

```
GET /users/me
Authorization: Bearer {accessToken}
```

**Update User Profile**

```
PUT /users/me
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Sakshi Kumar",
  "phone": "+91-9876543210"
}
```

**Delete Account**

```
DELETE /users/me
Authorization: Bearer {accessToken}
```

### Consultation Booking Endpoints

**Create Consultation**

```
POST /consultations
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+91-9876543210",
  "financialGoal": "Retire by 55"
}
```

**Get My Consultations**

```
GET /consultations?page=0&size=10&status=New
Authorization: Bearer {accessToken}
```

**Get Consultation by ID**

```
GET /consultations/{consultationId}
Authorization: Bearer {accessToken}
```

**Update Consultation**

```
PUT /consultations/{consultationId}
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "financialGoal": "Updated goal"
}
```

### Query Endpoints

**Create Query**

```
POST /queries
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+91-9876543210",
  "queryText": "How to start SIP?",
  "category": "SIP"
}
```

**Get My Queries**

```
GET /queries?page=0&size=10&status=New
Authorization: Bearer {accessToken}
```

**Get Query by ID**

```
GET /queries/{queryId}
Authorization: Bearer {accessToken}
```

### Admin Endpoints

**List All Users**

```
GET /admin/users?page=0&size=10
Authorization: Bearer {adminToken}
```

**List All Consultations**

```
GET /admin/consultations?page=0&size=10&status=New
Authorization: Bearer {adminToken}
```

**Update Consultation Status**

```
PATCH /admin/consultations/{consultationId}/status
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "status": "APPOINTMENT_CONFIRMED"
}
```

**List All Queries**

```
GET /admin/queries?page=0&size=10
Authorization: Bearer {adminToken}
```

**Reply to Query**

```
POST /admin/queries/{queryId}/reply
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "replyText": "Thank you for your query. Please schedule a call..."
}
```

## Testing

### Run Tests

```bash
mvn test
```

### Test with cURL

```bash
# Register
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Sakshi",
    "email":"sakshi@example.com",
    "password":"Pass123!",
    "phone":"+91-9876543210"
  }'

# Login
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"sakshi@example.com","password":"Pass123!"}'

# Get Profile
curl -X GET "http://localhost:8080/api/v1/users/me" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Environment Variables

Create `.env` file or set environment variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=wisewealth_db
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=your_secret_key
JWT_EXPIRATION=3600000
SERVER_PORT=8080
```

## Configuration Files

### application.properties

Development configuration. Customize database, JWT, and logging settings here.

### application-prod.properties

Create for production environment with appropriate settings.

## Common Issues & Solutions

### Port Already in Use

```bash
# Change port in application.properties
server.port=8081
```

### Database Connection Failed

- Ensure PostgreSQL is running
- Check database credentials
- Verify POSTGRES_SCHEMA.sql was executed

### JWT Token Issues

- Ensure JWT secret is minimum 256 bits
- Check token expiration settings
- Verify Bearer token format in headers

## Deployment

### Docker Deployment

```bash
# Build Docker image
mvn spring-boot:build-image

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/wisewealth_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  wisewealth-api:latest
```

### Kubernetes Deployment

Deploy using provided Kubernetes manifests (create if needed):

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## Performance Tuning

### Enable Query Caching

Add to `application.properties`:

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
```

### Connection Pool Configuration

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
```

## Security Best Practices

1. **Change JWT Secret**: Update with a secure, random key
2. **Enable HTTPS**: Use SSL/TLS in production
3. **Rate Limiting**: Implement rate limiting on public endpoints
4. **CORS Configuration**: Restrict allowed origins
5. **Input Validation**: All inputs are validated server-side
6. **Password Hashing**: BCrypt is used for password storage

## Logging

View logs:

```bash
# Real-time logs
tail -f /var/log/wisewealth-api.log

# Enable debug logging
# Update logging.level.com.wisewealth=DEBUG in application.properties
```

## Contributing

1. Create feature branch
2. Commit changes
3. Push to branch
4. Create Pull Request

## License

Proprietary - WiseWealth

## Support

For issues and support, contact: support@wisewealth.com

## Version History

- **1.0.0** (2026-06-12) - Initial release with user, consultation, and query management
