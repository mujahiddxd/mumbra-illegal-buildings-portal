# Illegal Buildings Documentation Portal

A Spring Boot application for documenting and managing illegal buildings in Mumbra.

## Features

- Interactive map for visualizing illegal buildings
- Building documentation with images and details
- Search and filter functionality
- User authentication and authorization
- RESTful API for building management

## Prerequisites

- Java 11 or higher
- Maven 3.6.3 or higher
- MySQL 8.0 or higher
- Node.js and npm (for frontend assets)

## Installation

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd illegal-buildings
   ```

2. **Set up the database**
   - Create a MySQL database named `illegal_buildings`
   - Update the database configuration in `src/main/resources/application.properties`

3. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open `http://localhost:8080` in your web browser
   - Use the following default credentials:
     - Username: admin
     - Password: admin123

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/mumbra/illegalbuildings/
│   │       ├── config/           # Spring configuration
│   │       ├── controller/       # Controllers
│   │       ├── model/            # Entity classes
│   │       ├── repository/       # JPA repositories
│   │       ├── service/          # Service layer
│   │       ├── security/         # Security configuration
│   │       └── IllegalBuildingsApplication.java
│   └── resources/
│       ├── static/              # Static files (JS, CSS, images)
│       ├── templates/           # Thymeleaf templates
│       └── application.properties
└── test/                       # Test files
```

## API Endpoints

### Buildings API

- `GET /api/buildings` - Get all buildings
- `GET /api/buildings/{id}` - Get a building by ID
- `GET /api/buildings/severity/{severity}` - Get buildings by severity
- `GET /api/buildings/search?query={query}` - Search buildings
- `POST /api/buildings` - Create a new building
- `PUT /api/buildings/{id}` - Update a building
- `DELETE /api/buildings/{id}` - Delete a building

## Frontend

The frontend is built with:
- HTML5, CSS3, and JavaScript
- Bootstrap 5 for responsive design
- Leaflet.js for interactive maps
- Thymeleaf for server-side rendering

## Security

- Spring Security for authentication and authorization
- BCrypt password encoding
- CSRF protection
- Session management

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
