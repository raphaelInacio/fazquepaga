import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";
import "./i18n";
import "./lib/firebase";

createRoot(document.getElementById("root")!).render(
    <App />
);

