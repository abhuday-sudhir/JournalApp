# JournalApp

A full-stack Journal application with:

- **Backend:** Spring Boot (JWT authentication, MongoDB, Journal CRUD)
- **Frontend:** React + Vite (Login/Register and Journal UI)

This project supports:

- User registration and login using JWT
- Create, read, update, delete journal entries
- Protected APIs for authenticated users

---

## Tech Stack

### Backend

- Java 17
- Spring Boot
- Spring Security (JWT)
- MongoDB
- Maven

### Frontend

- React
- Vite
- JavaScript (ES6+)

---

## Project Structure

```text
JournalApp/
├── src/                  # Spring Boot backend source
├── frontend/             # React frontend app
├── pom.xml               # Backend dependencies and build config
└── README.md
```

---

## Prerequisites

- Java 17+
- Maven (or use `./mvnw`)
- Node.js 18+ and npm
- MongoDB running locally (or configured connection)

---

## Backend Setup (Spring Boot)

From the project root:

```bash
./mvnw spring-boot:run
```

Backend default URL:

- `http://localhost:8080`

---

## Frontend Setup (React)

From the project root:

```bash
npm --prefix frontend install
npm --prefix frontend run dev
```

Frontend default URL:

- `http://localhost:5173`

The frontend is configured to call the backend APIs (JWT login/register + journal CRUD).

---

## Main API Endpoints

### Public

- `POST /public/signup` - Register new user
- `POST /public/login` - Login and receive JWT token

### Journal (JWT required)

- `GET /journal` - Get current user's journal entries
- `POST /journal` - Create journal entry
- `GET /journal/id/{id}` - Get entry by id
- `PUT /journal/id/{id}` - Update entry by id
- `DELETE /journal/id/{id}` - Delete entry by id

---

## Authentication Flow

1. Register using `/public/signup`
2. Login using `/public/login`
3. Backend returns JWT token
4. Frontend stores token and sends:

```http
Authorization: Bearer <token>
```

for protected routes.

---

## Notes

- Google authentication is not used in the frontend flow.
- Frontend currently focuses on:
  - Login/Register
  - Journal CRUD operations
- Ensure both backend and frontend are running for full functionality.

