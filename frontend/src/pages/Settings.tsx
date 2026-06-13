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
import { CancelSubscriptionModal } from "@/components/cancel-subscription-modal";
import { AlertTriangle } from "lucide-react";

export default function Settings() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { user, logout, updateUser } = useAuth();
    const [isLoggingOutAll, setIsLoggingOutAll] = useState(false);
    const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);

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
                        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 bg-muted/30 rounded-xl border border-border/60 hover:bg-muted/50 transition-all duration-300">
                            <div className="flex items-start gap-4">
                                <div className="p-2.5 bg-destructive/10 rounded-xl flex-shrink-0">
                                    <Monitor className="h-5 w-5 text-destructive" />
                                </div>
                                <div className="space-y-1">
                                    <p className="font-semibold text-foreground">
                                        {t("settings.logoutAll.title") || "Logout from all devices"}
                                    </p>
                                    <p className="text-sm text-muted-foreground leading-relaxed">
                                        {t("settings.logoutAll.description") || "This will sign you out from all devices and browsers"}
                                    </p>
                                </div>
                            </div>
                            <AlertDialog>
                                <AlertDialogTrigger asChild>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        disabled={isLoggingOutAll}
                                        data-testid="logout-all-button"
                                        className="border-destructive/30 text-destructive hover:bg-destructive hover:text-destructive-foreground flex-shrink-0 shadow-sm transition-all duration-300 font-medium self-end sm:self-center"
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

                {/* Subscription Section */}
                {user?.subscriptionTier === 'PREMIUM' && (user?.subscriptionStatus === 'ACTIVE' || user?.subscriptionStatus === 'PENDING_CANCELLATION') && (
                    <Card className="border-none shadow-soft mt-8 border-destructive/20">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2 text-destructive">
                                <AlertTriangle className="h-5 w-5" />
                                {t("settings.subscription.title") || "Subscription"}
                            </CardTitle>
                            <CardDescription>
                                {t("settings.subscription.description") || "Manage your Premium subscription"}
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {user?.subscriptionStatus === 'ACTIVE' ? (
                                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 bg-destructive/5 rounded-xl border border-destructive/10 hover:bg-destructive/10 transition-all duration-300">
                                    <div className="flex items-start gap-4">
                                        <div className="p-2.5 bg-destructive/10 rounded-xl flex-shrink-0 animate-pulse">
                                            <AlertTriangle className="h-5 w-5 text-destructive" />
                                        </div>
                                        <div className="space-y-1">
                                            <p className="font-semibold text-destructive">
                                                {t("settings.subscription.cancelTitle") || "Cancel Subscription"}
                                            </p>
                                            <p className="text-sm text-muted-foreground leading-relaxed">
                                                {t("settings.subscription.cancelDescription") || "You will lose access to Premium features at the end of your billing cycle"}
                                            </p>
                                        </div>
                                    </div>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => setIsCancelModalOpen(true)}
                                        data-testid="cancel-subscription-button"
                                        className="border-destructive/30 text-destructive hover:bg-destructive hover:text-destructive-foreground flex-shrink-0 shadow-sm transition-all duration-300 font-medium self-end sm:self-center"
                                    >
                                        {t("settings.subscription.cancelButton") || "Cancel Subscription"}
                                    </Button>
                                </div>
                            ) : (
                                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 bg-yellow-50 dark:bg-yellow-950/20 rounded-xl border border-yellow-200 dark:border-yellow-900/50 hover:bg-yellow-100/50 dark:hover:bg-yellow-950/30 transition-all duration-300">
                                    <div className="flex items-start gap-4">
                                        <div className="p-2.5 bg-yellow-100 dark:bg-yellow-900/30 rounded-xl flex-shrink-0">
                                            <AlertTriangle className="h-5 w-5 text-yellow-600 dark:text-yellow-500" />
                                        </div>
                                        <div className="space-y-1">
                                            <p className="font-semibold text-yellow-800 dark:text-yellow-400">
                                                {t("settings.subscription.pendingCancellationTitle") || "Cancellation Pending"}
                                            </p>
                                            <p className="text-sm text-yellow-700/80 dark:text-yellow-400/80 leading-relaxed">
                                                {t("settings.subscription.pendingCancellationDescription") || "Your subscription will remain active as Premium until the end of the billing period."}
                                            </p>
                                        </div>
                                    </div>
                                    <Button
                                        variant="default"
                                        size="sm"
                                        onClick={() => navigate("/subscription")}
                                        data-testid="resubscribe-button"
                                        className="bg-yellow-600 hover:bg-yellow-700 text-white dark:bg-yellow-500 dark:hover:bg-yellow-600 flex-shrink-0 shadow-sm transition-all duration-300 font-medium self-end sm:self-center"
                                    >
                                        {t("settings.subscription.resubscribeButton") || "Resubscribe"}
                                    </Button>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                )}

                <CancelSubscriptionModal 
                    open={isCancelModalOpen} 
                    onOpenChange={setIsCancelModalOpen} 
                    onSuccess={() => {
                        // Reload user to update status
                        if (user) {
                            updateUser({
                                ...user,
                                subscriptionStatus: "PENDING_CANCELLATION"
                            });
                        }
                    }} 
                />
            </div>
        </div>
    );
}
