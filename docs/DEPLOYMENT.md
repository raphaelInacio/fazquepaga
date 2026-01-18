# Deployment Guide

This guide details how to build and deploy the application.

## Prerequisites
-   Node.js (v20+)
-   Java 17 (JDK)
-   Maven
-   Firebase CLI (`npm install -g firebase-tools`)
-   Google Cloud SDK (`gcloud`)

## Frontend (Firebase Hosting)

The frontend is a static React application hosted on Firebase.

### 1. Build
Navigate to the frontend directory:
```bash
cd frontend
npm install
npm run build
```
This generates the `frontend/dist` directory.

### 2. Deploy
Deploy to Firebase Hosting:
```bash
firebase deploy --only hosting
```

### Validation
Visit your Firebase Hosting URL (e.g., `https://your-project-id.web.app`) to verify the deployment.

---

## Backend (Cloud Run)

The backend is a Spring Boot application running on Cloud Run.

### 1. Build
Navigate to the backend directory:
```bash
cd backend
mvn clean package -DskipTests
```
This generates the JAR file in `backend/target`.
*Note: We skip tests here assuming they ran in CI.*

### 2. Containerize & Deploy
You can use Google Cloud Build packs or a Dockerfile.

#### Option A: Source Deploy (easiest)
```bash
gcloud run deploy taskandpay-backend \
  --source . \
  --region us-central1 \
  --allow-unauthenticated
```

### Configuration
Ensure your Cloud Run service has the following environment variables if needed (or through Secret Manager):
-   `SPRING_PROFILES_ACTIVE=prod`

### Swagger UI
Swagger UI is **disabled** in the `prod` profile for security.
To verify API status, use health checks or authorized endpoints.

## GitHub Actions Configuration

To enable the CI/CD pipelines, you must configure the following **Secrets** in your GitHub Repository settings (Settings > Secrets and variables > Actions):

### Common
-   `GCP_CREDENTIALS`: The Service Account JSON key for Google Cloud authentication (must have permissions for Cloud Run and Firebase Hosting).
-   `GCP_PROJECT_ID`: Your Google Cloud Project ID.

### Frontend (Firebase) env vars
These are used to build the frontend with the correct Firebase config.
-   `VITE_FIREBASE_API_KEY`: Your Firebase API Key.
-   `VITE_FIREBASE_AUTH_DOMAIN`: `your-project.firebaseapp.com`
-   `VITE_FIREBASE_PROJECT_ID`: `your-project-id`
-   `VITE_FIREBASE_STORAGE_BUCKET`: `your-project.appspot.com`
-   `VITE_FIREBASE_MESSAGING_SENDER_ID`: Your Messaging Sender ID.
-   `VITE_FIREBASE_APP_ID`: Your App ID.
-   `VITE_FIREBASE_MEASUREMENT_ID`: Your Analytics Measurement ID.

