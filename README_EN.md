<h1 align="center">房屋租赁市场系统 | House Rental Market System</h1>

> A house rental market system built with Spring Boot + Spring Security + WebSocket, supporting house booking for tenants, appointment management for landlords, real-time notifications, and admin backend.

<br/>

<!-- Language Switch Buttons -->
<p align="center">
  <a href="README.md">
    <img src="https://img.shields.io/badge/中文版本-点击查看-red?style=for-the-badge&logo=markdown&logoColor=white" />
  </a>

  <a href="README_EN.md">
    <img src="https://img.shields.io/badge/English-Version-blue?style=for-the-badge&logo=markdown&logoColor=white" />
  </a>
</p>

<br/>

<!-- Tech Stack Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" />
  <img src="https://img.shields.io/badge/SpringBoot-3.2.4-green?style=flat-square&logo=springboot" />
  <img src="https://img.shields.io/badge/Spring_Security-6-6DB33F?style=flat-square&logo=springsecurity" />
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql" />
  <img src="https://img.shields.io/badge/WebSocket-STOMP-purple?style=flat-square&logo=websocket" />
  <img src="https://img.shields.io/badge/JWT-Ready-black?style=flat-square&logo=jsonwebtokens" />
</p>

<br/>
<br/>


# House Rental Market System

A house rental market system based on Spring Boot and modern web technologies, supporting tenants booking houses and landlords managing appointments.

## 🚀 Project Overview

This project is a comprehensive house rental market system designed to connect tenants and landlords, providing convenient house booking and management services. The system adopts a front-end and back-end separation architecture, supports real-time communication, and features a modern UI design with a smooth user experience.

## 🛠️ Technology Stack

### Backend Technologies
- **Spring Boot 3.2.4** - Enterprise-grade Java application development framework
- **Java 21** - Latest LTS version of the Java programming language
- **MyBatis Plus** - Powerful ORM framework that simplifies database operations
- **Spring Security** - Provides JWT authentication and authorization
- **JWT (JSON Web Token)** - Used for user authentication and session management
- **WebSocket (STOMP)** - Enables real-time communication for appointment status updates
- **RESTful API** - Standardized API design
- **MySQL 8.0+** - Database management system
- **Hibernate** - Java persistence framework
- **Apache Commons** - Provides common utility classes
- **Swagger/OpenAPI** - API documentation generation and testing tool

### Frontend Technologies
- **HTML5** - Page structure
- **CSS3** - Style design, including animations and responsive layout
- **JavaScript** - Interactive logic and WebSocket client
- **Font Awesome** - Icon library
- **SockJS & STOMP** - WebSocket communication protocol
- **WebJars** - Frontend dependency management

## ✨ Core Features

### User Authentication & Authorization
- **Multi-role System**: Supports three roles: Tenant (TENANT), Landlord (LANDLORD), and Admin (ADMIN)
- **JWT Token Authentication**: Secure stateless authentication mechanism
- **Role-based Access Control**: Different roles have different functional permissions
- **CAPTCHA System**: Prevents bot registration and login
- **User Registration & Login**: Supports secure account creation and access

### House Listing Management
- **Listing Publication**: Landlords can publish listing information (title, type, area, price, address, description, etc.)
- **Listing Display**: Supports image display, detailed information, and filtering
- **Listing Search**: Search by keywords, type, price range, area range, address, and more
- **Favorites**: Tenants can bookmark listings they are interested in
- **Listing Details**: View complete listing information and images

### Appointment System
- **Tenant Features**:
  - Browse listings and initiate appointment requests
  - View appointment history and status
  - Receive real-time appointment status notifications
  - Cancel appointment requests

- **Landlord Features**:
  - View all appointment requests
  - Approve or reject appointments
  - Manage appointment history
  - Receive real-time new appointment notifications
  - View tenant information

### Real-time Communication
- STOMP protocol based on WebSocket
- Landlords receive real-time notifications when tenants submit appointments
- Tenants receive real-time status updates when landlords process appointments
- Real-time data sync without page refresh

### Admin Features
- User Management: View, edit, and delete user information
- Listing Management: Review listings and manage listing information
- Appointment Management: Monitor all appointment statuses
- System Statistics: View platform operational data

### UI/UX Design
- Modern gradient backgrounds and animation effects
- Responsive design, adaptable to various devices
- Smooth interactive animations
- Intuitive status display (color-coded badges)

## 📦 Project Structure

```
SpringBoot-HouseMarket/
├── src/
│   ├── main/
│   │   ├── java/com/springboot/springboothousemarket/
│   │   │   ├── Config/              # Configuration classes (Security, WebSocket, MyBatis, etc.)
│   │   │   ├── Controller/          # Controller layer (API endpoints)
│   │   │   ├── Entity/              # Entity classes (database mapping)
│   │   │   ├── Mapper/              # MyBatis mapper interfaces
│   │   │   ├── Service/             # Service layer (business logic)
│   │   │   ├── Util/                # Utility classes (JWT tools, etc.)
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   └── SpringBootHouseMarketApplication.java # Application entry point
│   │   └── resources/
│   │       ├── mapper/              # MyBatis XML mapping files
│   │       ├── static/              # Static resources (HTML, CSS, JS)
│   │       │   ├── assets/          # Asset files (js)
│   │       │   ├── css/             # CSS style files
│   │       │   ├── admin.html       # Admin interface
│   │       │   ├── house-detail.html # Listing detail page
│   │       │   ├── landlord.html    # Landlord interface
│   │       │   ├── login.html       # Login interface
│   │       │   ├── register.html    # Registration interface
│   │       │   └── tenant.html      # Tenant interface
│   │       ├── application.yml      # Application configuration file
│   │       └── application-private.yml # Private configuration file for local sensitive info
│   └── test/                        # Test code
├── pom.xml                          # Maven dependency management
└── README.md                        # Project documentation
```

## 🔧 Quick Start

### Requirements
- Java 21
- Maven 3.8+
- MySQL 8.0+
- Node.js (optional, for frontend development)

### Installation Steps

1. **Clone the Project**
   ```bash
   git clone <repository-url>
   cd SpringBoot-HouseMarket
   ```

2. **Configure Database**
   - Create a MySQL database (e.g., HouseMarket)
   - Modify the database connection info in [application.yml]
   - Set environment variables DB_USERNAME and DB_PASSWORD, or configure database username and password directly in the config file

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run the Project**
   ```bash
   mvn spring-boot:run
   ```

   Or package and run:
   ```bash
   mvn package
   java -jar target/SpringBoot-HouseMarket-0.0.1-SNAPSHOT.jar
   ```

5. **Access the System**
   - Open your browser and visit `http://localhost:8082`
   - The system will automatically open the login page at `http://localhost:8082/login.html`
   - Log in with your registered account

### Database Table Structure
The system uses the following main data tables:

- `users` - User information table
- `houses` - House listing information table
- `appointments` - Appointment information table
- `favorites` - Favorites information table


## 🔍 Core Feature Details

### 1. User Authentication Flow

**Registration Flow**:
1. User fills in registration information (username, password, role)
2. System validates information format and uniqueness
3. Password is encrypted and stored in the database
4. User status is set to pending review or directly activated

**Login Flow**:
1. User enters username, password, and role
2. System verifies user credentials
3. Generates JWT token
4. Returns user information and access token

**JWT Token Mechanism**:
- Token validity: 10 hours
- Contains user role information
- Each request must carry `Authorization: Bearer <token>` in the Header
- Re-login required after token expiration

### 2. Appointment Flow

**Tenant Side**:
1. Browse listing list
2. Select a listing of interest
3. Fill in appointment information (time, location, etc.)
4. Submit appointment request
5. Receive real-time appointment status updates

**Landlord Side**:
1. Log into the system
2. View pending appointment requests
3. Approve or reject appointments
4. View appointment history
5. Receive real-time new appointment notifications

### 3. WebSocket Real-time Communication

The system uses Spring Boot's WebSocket support with the STOMP protocol for real-time communication:
- Configured `/ws` endpoint for WebSocket connections
- Uses `/topic` and `/queue` prefixes for message routing
- Tenants and landlords subscribe to different message channels
- Real-time push notifications via WebSocket when appointment status changes

### 4. Security Design

- JWT authentication, stateless design
- Role-based access control (Tenant/Landlord/Admin)
- WebSocket connections are also authenticated
- Encrypted password storage
- Request rate limiting and anti-brute-force mechanisms
- SQL injection prevention (MyBatis Plus built-in)
- XSS attack prevention

## 🎨 Interface Design

- **Modern Gradient Background**: Purple gradient for a premium feel
- **Particle Animation Effects**: Enhanced visual experience
- **Responsive Layout**: Adapts to desktop, tablet, and mobile devices
- **Smooth Transition Animations**: Hover effects, slide animations, etc.
- **Intuitive Status Indicators**: Color-coded badges for appointment status
- **User-friendly Navigation**: Clear menu structure and page navigation

## 📋 Implemented Features

1. **User System**: Registration, login, role management, JWT authentication
2. **Listing Management**: Publish, edit, delete, search, detail display
3. **Appointment System**: Appointment creation, status management, real-time notifications
4. **Favorites**: Tenants can bookmark listings of interest
5. **Real-time Communication**: WebSocket-based appointment status updates
6. **Frontend Interfaces**: Tenant side, landlord side, admin side
7. **Security Mechanisms**: JWT authentication, role-based access control
8. **API Documentation**: Auto-generated API docs via Swagger
9. **File Upload**: Listing image upload functionality
10. **Data Statistics**: Basic user and listing statistics

## 📋 Unimplemented Features

The following features are planned for future releases:

1. **Payment System**: Support online deposit or rent payment
2. **Review System**: Mutual reviews between tenants and landlords
3. **Messaging System**: Instant messaging between tenants and landlords
4. **Advanced Search & Filtering**: Multi-condition search by price, area, location, etc.
5. **Multi-image Upload**: Support uploading and previewing multiple images
6. **Map Integration**: Visualize listing locations
7. **Notification Center**: Unified management of all notifications
8. **Data Statistics & Reports**: Landlord viewing listing appointment statistics
9. **Email Notifications**: Email alerts for important events
10. **Phone Verification**: Registration and password recovery via SMS

## 🔮 Future Plans

- Microservices architecture transformation
- Introduce frontend frameworks (React/Vue)
- Mobile app development
- Cloud deployment support
- Big data analytics for intelligent recommendations
- AI-driven listing recommendation system
- Intelligent customer service integration

## 📊 API Documentation

The system provides a complete RESTful API interface, accessible via Swagger UI:
- Access URL: `http://localhost:8082/swagger-ui.html`
- Provides online documentation and testing for all APIs

Main API Modules:
- `/api/v1/auth` - Authentication related endpoints
- `/api/houses` - Listing management endpoints
- `/api/appointments` - Appointment management endpoints
- `/api/favorites` - Favorites management endpoints
- `/api/users` - User management endpoints

## 🧪 Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

## 🤝 Contribution Guide

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**The project is continuously being improved. Stay tuned and get involved!** 🚀
