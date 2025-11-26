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
import { GiftCardStorePage } from "./pages/GiftCardStorePage";
import AIFeatures from "./pages/AIFeatures";
import { LanguageSwitcher } from "./components/LanguageSwitcher";


const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <div className="fixed top-4 right-4 z-50">
        <LanguageSwitcher />
      </div>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Index />} />
          <Route path="/register" element={<RegisterParent />} />
          <Route path="/add-child" element={<AddChild />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/child/:childId/tasks" element={<ChildTasks />} />
          <Route path="/gift-cards" element={<GiftCardStorePage />} />
          <Route path="/ai-suggestions" element={<AIFeatures />} />
          {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;

