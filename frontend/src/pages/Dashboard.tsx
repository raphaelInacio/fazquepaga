import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { User as UserIcon, Gift, Loader2, QrCode, Plus, Sparkles } from "lucide-react";
import { User } from "@/types";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { childService } from "@/services/childService";
import { toast } from "sonner";

export default function Dashboard() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [children, setChildren] = useState<User[]>([]);
    const [parentName, setParentName] = useState("");
    const [selectedChildId, setSelectedChildId] = useState<string | null>(null);
    const [allowanceAmount, setAllowanceAmount] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [onboardingCode, setOnboardingCode] = useState<string | null>(null);
    const [selectedChildForCode, setSelectedChildForCode] = useState<string | null>(null);

    useEffect(() => {
        const parentId = localStorage.getItem("parentId");
        if (!parentId) {
            navigate("/register");
            return;
        }

        const storedName = localStorage.getItem("parentName");
        if (storedName) setParentName(storedName);

        const fetchChildren = async () => {
            try {
                const childrenData = await childService.getChildren(parentId);
                setChildren(childrenData);
            } catch (error) {
                console.error("Failed to fetch children", error);
                toast.error("Failed to load your children's data.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchChildren();
    }, [navigate]);

    const handleSetAllowance = async () => {
        if (!selectedChildId || !allowanceAmount) return;

        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await childService.updateAllowance(selectedChildId, parseFloat(allowanceAmount), parentId);
            toast.success("Allowance updated successfully!");
            setAllowanceAmount("");
            setSelectedChildId(null);

            // Refetch children to update the UI
            setIsLoading(true);
            const childrenData = await childService.getChildren(parentId);
            setChildren(childrenData);
            setIsLoading(false);

        } catch (error) {
            toast.error("Failed to update allowance");
            console.error(error);
        }
    };

    const handleGenerateOnboardingCode = async (childId: string) => {
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            const response = await childService.generateOnboardingCode(childId, parentId);
            setOnboardingCode(response.code);
            setSelectedChildForCode(childId);
            toast.success("Onboarding code generated!");
        } catch (error) {
            toast.error("Failed to generate onboarding code");
            console.error(error);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-4xl mx-auto space-y-8">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">{t("dashboard.title")}</h1>
                        <p className="text-gray-500">{t("dashboard.welcome", { name: parentName || "Parent" })}</p>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="outline" onClick={() => navigate("/gift-cards")} data-testid="gift-cards-button">
                            <Gift className="mr-2 h-4 w-4" /> Loja de Recompensas
                        </Button>
                        <Button onClick={() => navigate("/add-child")} data-testid="add-child-button">
                            <Plus className="mr-2 h-4 w-4" /> {t("dashboard.addChild")}
                        </Button>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center py-12">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    </div>
                ) : (
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {children.map((child) => (
                            <Card
                                key={child.id}
                                className="hover:shadow-lg transition-shadow cursor-pointer"
                                onClick={() => navigate(`/child/${child.id}/tasks`)}
                            >
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-xl font-medium">{child.name}</CardTitle>
                                    <UserIcon className="h-4 w-4 text-muted-foreground" />
                                </CardHeader>
                                <CardContent>
                                    <div className="text-2xl font-bold">{child.age || "N/A"} years old</div>
                                    {child.monthlyAllowance !== undefined && (
                                        <div className="text-sm text-green-600 font-medium mt-1">
                                            {t("dashboard.allowance")}: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(child.monthlyAllowance)}
                                        </div>
                                    )}
                                    <p className="text-xs text-muted-foreground mt-1">
                                        {t("dashboard.viewTasks")}
                                    </p>
                                    <div className="mt-4 space-y-2" onClick={(e) => e.stopPropagation()}>
                                        <Dialog open={selectedChildId === child.id} onOpenChange={(open) => {
                                            if (open) {
                                                setSelectedChildId(child.id);
                                                setAllowanceAmount(child.monthlyAllowance?.toString() || "");
                                            } else {
                                                setSelectedChildId(null);
                                            }
                                        }}>
                                            <DialogTrigger asChild>
                                                <Button variant="outline" size="sm" className="w-full">
                                                    {t("dashboard.setAllowance")}
                                                </Button>
                                            </DialogTrigger>
                                            <DialogContent>
                                                <DialogHeader>
                                                    <DialogTitle>{t("dashboard.setAllowanceTitle", { name: child.name })}</DialogTitle>
                                                </DialogHeader>
                                                <div className="grid gap-4 py-4">
                                                    <div className="grid grid-cols-4 items-center gap-4">
                                                        <Label htmlFor="allowance" className="text-right">
                                                            {t("dashboard.amount")}
                                                        </Label>
                                                        <Input
                                                            id="allowance"
                                                            type="number"
                                                            value={allowanceAmount}
                                                            onChange={(e) => setAllowanceAmount(e.target.value)}
                                                            className="col-span-3"
                                                            placeholder="0.00"
                                                        />
                                                    </div>
                                                </div>
                                                <Button onClick={handleSetAllowance}>{t("dashboard.save")}</Button>
                                            </DialogContent>
                                        </Dialog>
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            className="w-full"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleGenerateOnboardingCode(child.id);
                                            }}
                                        >
                                            <QrCode className="mr-2 h-4 w-4" /> Gerar Código WhatsApp
                                        </Button>
                                    </div>
                                </CardContent>
                            </Card>
                        ))}

                        {children.length === 0 && !isLoading && (
                            <div className="col-span-full text-center py-12 text-gray-500">
                                <p>{t("dashboard.noChildren")}</p>
                                <p className="mt-2">{t("dashboard.addChild")}</p>
                            </div>
                        )}
                    </div>
                )}

                <Dialog open={!!onboardingCode} onOpenChange={() => setOnboardingCode(null)}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Código de Cadastro WhatsApp</DialogTitle>
                        </DialogHeader>
                        <div className="text-center p-6 space-y-4">
                            <p className="text-gray-600">
                                A criança deve enviar este código para o WhatsApp do sistema para completar o cadastro.
                            </p>
                            <div className="text-4xl font-mono font-bold tracking-wider bg-gray-100 p-4 rounded-lg">
                                {onboardingCode}
                            </div>
                        </div>
                        <Button
                            onClick={() => {
                                if (onboardingCode) {
                                    navigator.clipboard.writeText(onboardingCode);
                                    toast.success("Código copiado!");
                                }
                            }}
                        >
                            Copiar Código
                        </Button>
                    </DialogContent>
                </Dialog>
            </div>
        </div>
    );
}
