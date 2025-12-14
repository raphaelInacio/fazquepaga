import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { childAuthService } from "@/services/childAuthService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { toast } from "sonner";
import { Gamepad2, Loader2 } from "lucide-react";

export default function ChildLogin() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [code, setCode] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    async function handleLogin(e: React.FormEvent) {
        e.preventDefault();

        if (!code || code.trim().length === 0) {
            toast.error(t("childLogin.enterCode"));
            return;
        }

        setIsLoading(true);
        try {
            await childAuthService.login(code.trim().toUpperCase());
            toast.success(t("childLogin.welcome"));
            navigate("/child-portal");
        } catch (error) {
            toast.error(t("childLogin.invalidCode"));
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-background via-purple-50/50 to-blue-50/50 dark:via-purple-950/20 dark:to-blue-950/20 p-4 animate-fade-in relative overflow-hidden">
            {/* Playful background elements */}
            <div className="absolute top-20 left-20 w-32 h-32 bg-yellow-400/20 dark:bg-yellow-400/10 rounded-full blur-2xl animate-bounce duration-[3000ms]" />
            <div className="absolute bottom-20 right-20 w-40 h-40 bg-purple-500/20 dark:bg-purple-500/10 rounded-full blur-2xl animate-bounce delay-1000 duration-[4000ms]" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-primary/5 dark:bg-primary/10 rounded-full blur-3xl -z-10" />

            <Card className="w-full max-w-md border-border/50 shadow-glow bg-card/90 dark:bg-card/80 backdrop-blur-sm relative z-10">
                <CardHeader className="space-y-4 pb-6">
                    <div className="mx-auto w-16 h-16 bg-gradient-to-br from-purple-500 to-primary rounded-2xl flex items-center justify-center shadow-lg shadow-primary/25 mb-2 transform rotate-3">
                        <Gamepad2 className="w-8 h-8 text-white" />
                    </div>
                    <div className="text-center space-y-2">
                        <CardTitle className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600 font-heading">
                            {t("auth.childLogin.title")}
                        </CardTitle>
                        <p className="text-muted-foreground">{t("auth.childLogin.instruction")}</p>
                    </div>
                </CardHeader>
                <CardContent className="space-y-6">
                    <div className="space-y-2">
                        <div className="relative">
                            <Input
                                type="text"
                                placeholder={t("auth.childLogin.placeholder")}
                                value={code}
                                onChange={(e) => setCode(e.target.value.toUpperCase())}
                                className="text-center text-3xl font-mono tracking-[0.5em] h-16 uppercase bg-background/50 dark:bg-background/30 border-2 focus:border-primary/50 transition-all rounded-xl shadow-inner placeholder:tracking-normal placeholder:font-sans placeholder:text-lg"
                                maxLength={6}
                            />
                        </div>
                        <p className="text-xs text-center text-muted-foreground">
                            Ask your parent for your special code
                        </p>
                    </div>

                    <Button
                        className="w-full h-12 text-lg font-bold bg-gradient-to-r from-primary to-purple-600 hover:from-primary/90 hover:to-purple-600/90 shadow-lg shadow-primary/20 rounded-xl transition-transform hover:scale-[1.02] active:scale-[0.98]"
                        onClick={handleLogin}
                        disabled={isLoading || code.length < 6}
                    >
                        {isLoading ? (
                            <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                        ) : (
                            <Gamepad2 className="mr-2 h-5 w-5" />
                        )}
                        {t("auth.childLogin.button")}
                    </Button>

                    <div className="text-center">
                        <Button variant="link" onClick={() => navigate("/")} className="text-muted-foreground hover:text-primary">
                            Back to Home
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
