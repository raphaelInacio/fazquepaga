import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "./pages/Index";
import NotFound from "./pages/NotFound";
import RegisterParent from "./pages/RegisterParent";
import AddChild from "./pages/AddChild";
import Dashboard from "./pages/Dashboard";
import ChildTasks from "./pages/ChildTasks";
import ChildLogin from "./pages/ChildLogin";
import ChildPortal from "./pages/ChildPortal";
import Login from "./pages/Login"; // Added
import { AuthProvider } from "./context/AuthContext"; // Added
import { GiftCardStorePage } from "./pages/GiftCardStorePage";
import PricingPage from "./pages/PricingPage";
import { LanguageSwitcher } from "./components/LanguageSwitcher";
import { SubscriptionProvider } from "./contexts/SubscriptionContext";


const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <SubscriptionProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <div className="fixed top-4 right-4 z-50">
            <LanguageSwitcher />
          </div>
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<Index />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<RegisterParent />} />
              <Route path="/add-child" element={<AddChild />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/child/:childId/tasks" element={<ChildTasks />} />
              <Route path="/gift-cards" element={<GiftCardStorePage />} />
              <Route path="/child-login" element={<ChildLogin />} />
              <Route path="/child-portal" element={<ChildPortal />} />
              <Route path="/subscription" element={<PricingPage />} />
              {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </SubscriptionProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;

