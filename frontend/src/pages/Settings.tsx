import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/context/AuthContext";
import api from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { Loader2, LogOut, ChevronLeft, Shield, Monitor } from "lucide-react";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

export default function Settings() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { logout } = useAuth();
    const [isLoggingOutAll, setIsLoggingOutAll] = useState(false);

    const handleLogoutAll = async () => {
        setIsLoggingOutAll(true);
        try {
            await api.post("/api/v1/auth/logout-all");
            toast.success(t("settings.logoutAll.success") || "All sessions have been logged out");

            // Logout locally after revoking all tokens
            logout();
            navigate("/login");
        } catch (error) {
            console.error("Failed to logout all sessions", error);
            toast.error(t("settings.logoutAll.error") || "Failed to logout from all devices");
        } finally {
            setIsLoggingOutAll(false);
        }
    };

    return (
        <div className="min-h-screen bg-background p-8">
            <div className="max-w-2xl mx-auto space-y-8 animate-fade-in">
                {/* Header */}
                <div className="flex items-center gap-4">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => navigate("/dashboard")}
                        className="hover:bg-primary/10"
                    >
                        <ChevronLeft className="h-5 w-5" />
                    </Button>
                    <div>
                        <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600">
                            {t("settings.title") || "Settings"}
                        </h1>
                        <p className="text-muted-foreground">
                            {t("settings.subtitle") || "Manage your account and security settings"}
                        </p>
                    </div>
                </div>

                {/* Security Section */}
                <Card className="border-none shadow-soft">
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Shield className="h-5 w-5 text-primary" />
                            {t("settings.security.title") || "Security"}
                        </CardTitle>
                        <CardDescription>
                            {t("settings.security.description") || "Manage your account security settings"}
                        </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {/* Logout from all devices */}
                        <div className="flex items-center justify-between p-4 bg-muted/50 rounded-lg border border-border/50">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-destructive/10 rounded-lg">
                                    <Monitor className="h-5 w-5 text-destructive" />
                                </div>
                                <div>
                                    <p className="font-medium">
                                        {t("settings.logoutAll.title") || "Logout from all devices"}
                                    </p>
                                    <p className="text-sm text-muted-foreground">
                                        {t("settings.logoutAll.description") || "This will sign you out from all devices and browsers"}
                                    </p>
                                </div>
                            </div>
                            <AlertDialog>
                                <AlertDialogTrigger asChild>
                                    <Button
                                        variant="destructive"
                                        size="sm"
                                        disabled={isLoggingOutAll}
                                        data-testid="logout-all-button"
                                    >
                                        {isLoggingOutAll ? (
                                            <Loader2 className="h-4 w-4 animate-spin" />
                                        ) : (
                                            <>
                                                <LogOut className="mr-2 h-4 w-4" />
                                                {t("settings.logoutAll.button") || "Logout All"}
                                            </>
                                        )}
                                    </Button>
                                </AlertDialogTrigger>
                                <AlertDialogContent>
                                    <AlertDialogHeader>
                                        <AlertDialogTitle>
                                            {t("settings.logoutAll.confirmTitle") || "Are you sure?"}
                                        </AlertDialogTitle>
                                        <AlertDialogDescription>
                                            {t("settings.logoutAll.confirmDescription") ||
                                                "This action will log you out from all devices and browsers. You will need to login again on each device."}
                                        </AlertDialogDescription>
                                    </AlertDialogHeader>
                                    <AlertDialogFooter>
                                        <AlertDialogCancel>
                                            {t("common.cancel") || "Cancel"}
                                        </AlertDialogCancel>
                                        <AlertDialogAction
                                            onClick={handleLogoutAll}
                                            className="bg-destructive hover:bg-destructive/90"
                                            data-testid="confirm-logout-all-button"
                                        >
                                            {t("settings.logoutAll.confirm") || "Yes, logout all devices"}
                                        </AlertDialogAction>
                                    </AlertDialogFooter>
                                </AlertDialogContent>
                            </AlertDialog>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
