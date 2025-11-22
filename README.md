# TaskAndPay

**TaskAndPay** is a Software as a Service (SaaS) platform designed for parents and children (under 18) to manage tasks and allowances in a modern and engaging way. The system allows parents to assign value to their children's activities, track their completion, and automate the calculation of their allowance. It uniquely integrates AI for task suggestions and validation, with a simple interface for children via WhatsApp.

The core problem it solves is the difficulty of consistently managing and encouraging children's responsibilities while teaching them financial literacy.

## 🚀 Key Features

The application is divided into focused domain modules:

*   **User Management**: Secure registration and profile management for parents and children.
*   **Task Management with Multiple Types**:
    *   **Daily Tasks**: Recurring tasks that happen every day.
    *   **Weekly Tasks**: Activities scheduled for specific days of the week.
    *   **One-time Tasks**: Single goals or special events.
*   **Allowance Calculation Engine**:
    *   Parents define a total monthly allowance.
    *   Parents assign a weight (e.g., Low, Medium, High) to each task.
    *   The system automatically calculates the value of each task based on its weight.
*   **Artificial Intelligence Features**:
    *   **Task Suggestion**: An LLM provides parents with ideas for age-appropriate tasks.
    *   **Image Validation**: A vision-capable LLM performs a preliminary check on photos sent via WhatsApp to confirm they match the completed task.
*   **Completion Flow**:
    *   **WhatsApp Integration**: Children can send a photo to a specific number to mark a visual task as complete.
    *   **Manual Approval**: Parents can manually approve non-visual tasks or override the AI's validation through the web dashboard.
*   **Financial Record**:
    *   A simple and clear statement showing completed tasks and the allowance earned.
*   **Plans and Monetization (Freemium)**:
    *   **Free Plan**: Limited to 5 recurring tasks, 1 child, and manual task approval.
    *   **Premium Plan**: Unlimited recurring tasks, AI-powered features (task suggestions, visual validation), and a Reward Store to exchange balance for Gift Cards.

## 🛠️ Tech Stack

### Backend
*   **Language**: Java 17
*   **Framework**: Spring Boot
*   **Database**: Google Cloud Firestore (NoSQL)
*   **Messaging**: Google Cloud Pub/Sub
*   **AI**: Spring AI with Google Vertex AI (Gemini)
*   **Integration**: Twilio (WhatsApp)
*   **Build**: Maven
*   **Containerization**: Docker & Docker Compose

### Frontend
*   **Framework**: React
*   **Build Tool**: Vite
*   **Language**: TypeScript
*   **Styling**: Tailwind CSS
*   **UI Components**: shadcn-ui

## 🏃‍♂️ Getting Started

### Prerequisites

*   Java 17+
*   Node.js & npm
*   Docker & Docker Compose

### How to Run Locally

1.  **Clone the Repository**:
    ```bash
    git clone <YOUR_GIT_URL>
    cd fazquepaga
    ```

2.  **Start the Backend Infrastructure (Emulators)**:
    Navigate to the `backend` directory and use Docker Compose to start the Firestore and Pub/Sub emulators.
    ```bash
    cd backend
    docker-compose up -d
    ```

3.  **Run the Backend Application**:
    In the `backend` directory, run the application using the Maven wrapper.
    ```bash
    ./mvnw spring-boot:run
    ```
    The backend API will be available at `http://localhost:8080`.

4.  **Run the Frontend Application**:
    In a new terminal, navigate to the `frontend` directory, install dependencies, and start the development server.
    ```bash
    cd frontend
    npm install
    npm run dev
    ```
    The frontend application will be available at `http://localhost:5173` and will connect to the backend API.

## 📂 Project Structure

The project is a monorepo with two main parts:

```
.
├── backend/      # Spring Boot modular monolith
│   ├── src/main/java/com/fazquepaga/taskandpay
│   │   ├── ai/           # AI integration (Gemini)
│   │   ├── allowance/    # Allowance calculation logic
│   │   ├── identity/     # User management
│   │   ├── tasks/        # Task management
│   │   └── whatsapp/     # Twilio/WhatsApp integration
│   ├── pom.xml
│   └── docker-compose.yml
└── frontend/     # React web application
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   └── services/
    └── package.json
```
