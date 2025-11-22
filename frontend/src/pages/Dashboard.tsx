import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Plus, User, Gift } from "lucide-react";
import { ChildWithLocalData } from "@/types";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { childService } from "@/services/childService";
import { toast } from "sonner";

export default function Dashboard() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [children, setChildren] = useState<ChildWithLocalData[]>([]);
    const [parentName, setParentName] = useState("");
    const parentId = localStorage.getItem("parentId");
    const [selectedChildId, setSelectedChildId] = useState<string | null>(null);
    const [allowanceAmount, setAllowanceAmount] = useState("");
    const [isAllowanceDialogOpen, setIsAllowanceDialogOpen] = useState(false);

    useEffect(() => {
        if (!parentId) {
            navigate("/register");
            return;
        }

        // Load parent name from localStorage
        const storedName = localStorage.getItem("parentName");
        if (storedName) setParentName(storedName);

        // Load children from localStorage
        const childrenData = localStorage.getItem("children");
        if (childrenData) {
            try {
                const parsedChildren = JSON.parse(childrenData);
                setChildren(parsedChildren);
            } catch (error) {
                console.error("Failed to parse children data", error);
            }
        }
    }, [parentId, navigate]);

    const handleSetAllowance = async () => {
        if (!selectedChildId || !allowanceAmount) return;

        try {
            const updatedChild = await childService.updateAllowance(selectedChildId, parseFloat(allowanceAmount));
            toast.success("Allowance updated successfully!");
            setIsAllowanceDialogOpen(false);
            setAllowanceAmount("");
            setSelectedChildId(null);

            // Update local state
            const updatedChildren = children.map(c =>
                c.id === selectedChildId ? { ...c, monthlyAllowance: updatedChild.monthlyAllowance } : c
            );
            setChildren(updatedChildren);

            // Update localStorage
            localStorage.setItem("children", JSON.stringify(updatedChildren));

        } catch (error) {
            toast.error("Failed to update allowance");
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
                        <Button variant="outline" onClick={() => navigate("/gift-cards")}>
                            <Gift className="mr-2 h-4 w-4" /> Loja de Recompensas
                        </Button>
                        <Button onClick={() => navigate("/add-child")}>
                            <Plus className="mr-2 h-4 w-4" /> {t("dashboard.addChild")}
                        </Button>
                    </div>
                </div>

                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {children.map((child) => (
                        <Card
                            key={child.id}
                            className="hover:shadow-lg transition-shadow cursor-pointer"
                            onClick={() => navigate(`/child/${child.id}/tasks`)}
                        >
                            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                <CardTitle className="text-xl font-medium">{child.name}</CardTitle>
                                <User className="h-4 w-4 text-muted-foreground" />
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
                                <div className="mt-4" onClick={(e) => e.stopPropagation()}>
                                    <Dialog open={isAllowanceDialogOpen && selectedChildId === child.id} onOpenChange={(open) => {
                                        setIsAllowanceDialogOpen(open);
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
                                </div>
                            </CardContent>
                        </Card>
                    ))}

                    {children.length === 0 && (
                        <div className="col-span-full text-center py-12 text-gray-500">
                            <p>{t("dashboard.noChildren")}</p>
                            <p className="mt-2">{t("dashboard.addChild")}</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
