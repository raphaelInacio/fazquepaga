import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import "./i18n";
import { SubscriptionProvider } from "./contexts/SubscriptionContext.tsx";

createRoot(document.getElementById("root")!).render(
    <SubscriptionProvider>
        <App />
    </SubscriptionProvider>
);

