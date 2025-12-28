# TaskAndPay

**TaskAndPay** is a Software as a Service (SaaS) platform designed for parents and children (under 18) to manage tasks and allowances in a modern and engaging way. The system allows parents to assign value to their children's activities, track their completion, and automate the calculation of their allowance. It uniquely integrates AI for task suggestions and validation, with a simple interface for children via WhatsApp.

The core problem it solves is the difficulty of consistently managing and encouraging children's responsibilities while teaching them financial literacy.

## 🚀 Key Features

The application is divided into focused domain modules:

*   **User Management**: Secure registration and profile management for parents and children.
*   **Task Management**:
    *   **Types**: Daily, Weekly, and One-time tasks.
    *   **Recurring Config**: Daily (reset every morning) and Weekly tasks.
    *   **Parent Approval**: Dedicated UI for parents to review and approve tasks completed by children.
    *   **Proof Requirement**: Option to require proof (e.g., photo) for task completion.
*   **Allowance Calculation Engine**:
    *   Automatic calculation of task values based on total monthly allowance and task weight.
*   **Artificial Intelligence Features**:
    *   **Task Suggestion**: AI-powered suggestions for age-appropriate tasks.
    *   **Image Validation**: AI analysis of photos sent via WhatsApp to verify task completion.
    *   **Adventure Mode**: Gamified task descriptions for children using AI.
    *   **Goal Coach**: AI-driven financial planning advice for children's savings goals.
*   **Child Portal**:
    *   Mobile-first interface for children to view tasks, track balance, and complete tasks.
    *   **Gamification**: Adventure Mode and fun stats.
*   **Financial Record**:
    *   **Ledger**: Detailed transaction history (credits and debits).
    *   **AI Financial Insights**: Smart analysis of spending and saving habits.
    *   **Gift Card Store**: (Premium) Redeem balance for real-world rewards.
*   **Plans and Monetization (Freemium)**:
    *   **Free Plan**: Limited to 5 recurring tasks, 1 child.
    *   **Premium Plan**: Unlimited tasks, AI features, Gift Card Store.

## 🛠️ Tech Stack

### Backend
*   **Language**: Java 17 (OpenJDK)
*   **Framework**: Spring Boot 3.5.7
*   **Database**: Google Cloud Firestore (NoSQL)
*   **Messaging**: Google Cloud Pub/Sub (Spring Cloud GCP 4.10.0)
    *   **Task Reset**: Daily recurring tasks are reset via a Pub/Sub message with payload `{"action": "RESET_TASKS"}` sent to the `task-reset` topic.
*   **AI**: Spring AI 1.1.0 with Google GenAI (Gemini)
*   **Payments**: Asaas (checkout redirect, webhooks)
*   **Integration**: Twilio (WhatsApp)
*   **Build**: Maven
*   **Containerization**: Docker & Docker Compose

### Frontend
*   **Framework**: React 18
*   **Build Tool**: Vite
*   **Language**: TypeScript
*   **Styling**: Tailwind CSS
*   **UI Components**: shadcn-ui
*   **i18n**: react-i18next (pt/en)

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
    The frontend application will be available at `http://localhost:5173`.

## 🌐 Frontend Routes

| Route | Description |
| :--- | :--- |
| `/` | Landing Page |
| `/login` | Parent Login |
| `/register` | Parent Registration |
| `/add-child` | Add Child Form |
| `/dashboard` | Parent Dashboard (Main Hub) |
| `/child/:childId/tasks` | Child Task Management (Parent View) |
| `/gift-cards` | Gift Card Store (Redemption) |
| `/child-login` | Child Login (via Code) |
| `/child-portal` | Child Portal (Task Completion & Gamification) |
| `/subscription` | Pricing & Subscription Management |

## 📂 Project Structure

The project is a monorepo with two main parts:

```
.
├── backend/      # Spring Boot modular monolith
│   ├── src/main/java/com/fazquepaga/taskandpay
│   │   ├── ai/           # AI integration (Gemini)
│   │   ├── allowance/    # Allowance calculation & Ledger
│   │   ├── config/       # App configuration (Security, CORS)
│   │   ├── controller/   # REST Controllers
│   │   ├── giftcard/     # Gift Card Store logic
│   │   ├── identity/     # User management (Parent/Child)
│   │   ├── notification/ # Notification Hub (Pub/Sub events)
│   │   ├── payment/      # Asaas integration (subscriptions)
│   │   ├── security/     # Spring Security setup
│   │   ├── shared/       # Shared utilities & exceptions
│   │   ├── subscription/ # Plan limits & logic
│   │   ├── tasks/        # Task management domain
│   │   └── whatsapp/     # Twilio/WhatsApp integration
│   ├── pom.xml
│   └── docker-compose.yml
└── frontend/     # React web application
    ├── src/
    │   ├── components/   # Reusable UI components
    │   ├── context/      # AuthContext
    │   ├── contexts/     # SubscriptionContext
    │   ├── hooks/        # Custom React hooks
    │   ├── lib/          # Utilities (API client, utils)
    │   ├── locales/      # i18n translations (pt/en)
    │   ├── pages/        # Application pages/routes
    │   ├── services/     # API service layers
    │   └── types/        # TypeScript definitions
    └── package.json
```
