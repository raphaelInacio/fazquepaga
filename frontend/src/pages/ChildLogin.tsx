import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { childAuthService } from "@/services/childAuthService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { toast } from "sonner";

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
        <div className="min-h-screen flex items-center justify-center p-4"
            style={{ background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)" }}>
            <Card className="w-full max-w-md shadow-2xl">
                <CardHeader className="text-center pb-4">
                    <div className="text-6xl mb-4">🎮</div>
                    <CardTitle className="text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                        {t("childLogin.title")}
                    </CardTitle>
                    <p className="text-gray-600 mt-2 text-lg">
                        {t("childLogin.instruction")}
                    </p>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleLogin} className="space-y-6">
                        <div>
                            <Input
                                type="text"
                                placeholder={t("childLogin.placeholder")}
                                value={code}
                                onChange={(e) => setCode(e.target.value)}
                                className="text-center text-2xl font-bold uppercase tracking-widest h-16"
                                maxLength={6}
                                autoFocus
                            />
                        </div>
                        <Button
                            type="submit"
                            className="w-full h-14 text-xl font-bold bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600"
                            disabled={isLoading}
                        >
                            {isLoading ? t("childLogin.buttonLoading") : t("childLogin.button")}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}
