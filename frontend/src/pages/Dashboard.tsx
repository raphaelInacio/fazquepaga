import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { User as UserIcon, Gift, Loader2, QrCode, Plus, Sparkles, LogOut, Edit, Trash2 } from "lucide-react";
import { User, Task } from "@/types";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { childService } from "@/services/childService";
import { taskService } from "@/services/taskService";
import { toast } from "sonner";
import confetti from "canvas-confetti";

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
    const [editingChild, setEditingChild] = useState<User | null>(null);
    const [deletingChild, setDeletingChild] = useState<User | null>(null);
    const [editForm, setEditForm] = useState({ name: "", age: 0, phoneNumber: "" });
    const [pendingTasks, setPendingTasks] = useState<{ childId: string, childName: string, task: Task }[]>([]);

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

                // Fetch pending tasks for all children
                const allPendingTasks: { childId: string, childName: string, task: Task }[] = [];
                for (const child of childrenData) {
                    if (child.id) {
                        const tasks = await taskService.getTasks(child.id);
                        const pending = tasks.filter(t => t.status === 'PENDING_APPROVAL');
                        pending.forEach(task => {
                            allPendingTasks.push({
                                childId: child.id!,
                                childName: child.name,
                                task
                            });
                        });
                    }
                }
                setPendingTasks(allPendingTasks);

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

            // Celebration effect
            confetti({
                particleCount: 100,
                spread: 70,
                origin: { y: 0.6 }
            });

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

    const handleApproveTask = async (childId: string, taskId: string) => {
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await taskService.approveTask(childId, taskId, parentId);
            toast.success(t("dashboard.pendingApprovals.success"));

            // Celebration effect for task approval
            confetti({
                particleCount: 50,
                spread: 60,
                origin: { y: 0.7 },
                colors: ['#26ccff', '#a25afd', '#ff5e7e', '#88ff5a', '#fcff42', '#ffa62d', '#ff36ff']
            });

            // Remove from local state
            setPendingTasks(prev => prev.filter(item => item.task.id !== taskId));

            // Refresh children to update balance if needed (optional, but good for consistency)
            const childrenData = await childService.getChildren(parentId);
            setChildren(childrenData);

        } catch (error) {
            toast.error(t("dashboard.pendingApprovals.error"));
            console.error(error);
        }
    };

    const handleLogout = () => {
        // Clear all authentication data
        localStorage.removeItem("parentId");
        localStorage.removeItem("parentName");
        localStorage.removeItem("children");

        toast.success(t("dashboard.logout.success"));
        navigate("/");
    };

    const handleEditChild = (child: User) => {
        setEditingChild(child);
        setEditForm({
            name: child.name,
            age: child.age || 0,
            phoneNumber: child.phoneNumber || ""
        });
    };

    const handleSaveEdit = async () => {
        if (!editingChild) return;
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await childService.updateChild(editingChild.id, editForm, parentId);
            toast.success(t("dashboard.child.updateSuccess"));
            setEditingChild(null);

            // Refresh children list
            const childrenData = await childService.getChildren(parentId);
            setChildren(childrenData);
        } catch (error) {
            toast.error(t("dashboard.child.updateError"));
            console.error(error);
        }
    };

    const handleDeleteChild = async () => {
        if (!deletingChild) return;
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await childService.deleteChild(deletingChild.id, parentId);
            toast.success(t("dashboard.child.deleteSuccess"));
            setDeletingChild(null);

            // Refresh children list
            const childrenData = await childService.getChildren(parentId);
            setChildren(childrenData);
        } catch (error) {
            toast.error(t("dashboard.child.deleteError"));
            console.error(error);
        }
    };

    return (
        <div className="min-h-screen bg-background p-8">
            <div className="max-w-5xl mx-auto space-y-8 animate-fade-in">
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 bg-card p-6 rounded-2xl shadow-soft border border-border/50">
                    <div>
                        <h1 className="text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600 font-heading">
                            {t("dashboard.title")}
                        </h1>
                        <p className="text-muted-foreground mt-1 text-lg">
                            {t("dashboard.welcome", { name: parentName || "Parent" })}
                        </p>
                    </div>
                    <div className="flex gap-3 flex-wrap">
                        <Button
                            variant="outline"
                            onClick={() => navigate("/gift-cards")}
                            data-testid="gift-cards-button"
                            className="hover:border-accent hover:text-accent transition-all duration-300"
                        >
                            <Gift className="mr-2 h-4 w-4" /> {t("dashboard.rewardsStore")}
                        </Button>
                        <Button
                            onClick={() => navigate("/add-child")}
                            data-testid="add-child-button"
                            className="bg-primary hover:bg-primary/90 shadow-lg shadow-primary/20 hover:shadow-primary/40 transition-all duration-300"
                        >
                            <Plus className="mr-2 h-4 w-4" /> {t("dashboard.addChild")}
                        </Button>
                        <Button
                            variant="ghost"
                            onClick={handleLogout}
                            data-testid="logout-button"
                            className="text-muted-foreground hover:text-destructive"
                        >
                            <LogOut className="h-5 w-5" />
                        </Button>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center py-20">
                        <Loader2 className="h-12 w-12 animate-spin text-primary" />
                    </div>
                ) : (
                    <div className="space-y-10">
                        {/* Pending Approvals Section */}
                        {pendingTasks.length > 0 && (
                            <Card className="border-none bg-gradient-to-br from-amber-50 to-orange-50 dark:from-amber-950/30 dark:to-orange-950/30 shadow-glow relative overflow-hidden group">
                                <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                                    <Sparkles className="h-24 w-24 text-amber-500" />
                                </div>
                                <CardHeader>
                                    <CardTitle className="text-2xl font-bold text-amber-700 dark:text-amber-400 flex items-center gap-3">
                                        <div className="p-2 bg-amber-100 dark:bg-amber-900/50 rounded-lg">
                                            <Sparkles className="h-6 w-6 text-amber-600 dark:text-amber-400" />
                                        </div>
                                        {t("dashboard.pendingApprovals.title")}
                                    </CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <div className="space-y-4">
                                        {pendingTasks.map((item) => (
                                            <div key={item.task.id} className="flex items-center justify-between bg-card/80 backdrop-blur-sm p-4 rounded-xl shadow-sm border border-amber-200/50 dark:border-amber-800/50 hover:shadow-md transition-all">
                                                <div>
                                                    <p className="font-bold text-lg text-foreground">{item.task.description}</p>
                                                    <p className="text-sm text-muted-foreground mt-1">
                                                        {t("dashboard.pendingApprovals.madeBy")}: <span className="font-semibold text-primary">{item.childName}</span>
                                                    </p>
                                                    {item.task.proofImageUrl && (
                                                        <a href={item.task.proofImageUrl} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 mt-2 text-xs font-medium text-blue-500 hover:text-blue-600 hover:underline">
                                                            View Proof
                                                        </a>
                                                    )}
                                                </div>
                                                <Button
                                                    onClick={() => item.task.id && handleApproveTask(item.childId, item.task.id)}
                                                    className="bg-green-500 hover:bg-green-600 text-white shadow-md hover:shadow-green-500/20 rounded-full px-6"
                                                >
                                                    {t("dashboard.pendingApprovals.approve")}
                                                </Button>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
                            {children.map((child) => (
                                <Card
                                    key={child.id}
                                    className="hover:shadow-glow transition-all duration-300 cursor-pointer group border-transparent hover:border-primary/20 bg-card overflow-hidden"
                                    onClick={() => navigate(`/child/${child.id}/tasks`)}
                                >
                                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4 bg-gradient-to-r from-primary/5 to-purple-500/5">
                                        <CardTitle className="text-2xl font-bold group-hover:text-primary transition-colors">{child.name}</CardTitle>
                                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                                            <UserIcon className="h-5 w-5 text-primary" />
                                        </div>
                                    </CardHeader>
                                    <CardContent className="pt-6">
                                        <div className="flex justify-between items-end mb-6">
                                            <div>
                                                <p className="text-sm text-muted-foreground">Idade</p>
                                                <p className="text-xl font-bold">{child.age || "N/A"}</p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-sm text-muted-foreground">{t("dashboard.allowance")}</p>
                                                {child.monthlyAllowance !== undefined ? (
                                                    <div className="text-xl font-bold text-green-600 dark:text-green-400">
                                                        {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(child.monthlyAllowance)}
                                                    </div>
                                                ) : (
                                                    <span className="text-sm text-muted-foreground">Not set</span>
                                                )}
                                            </div>
                                        </div>

                                        <div className="mt-6 space-y-3" onClick={(e) => e.stopPropagation()}>
                                            <div className="grid grid-cols-2 gap-3">
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    className="w-full hover:bg-primary/5 border-dashed"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        handleEditChild(child);
                                                    }}
                                                    data-testid="edit-child-button"
                                                >
                                                    <Edit className="mr-2 h-3 w-3" /> {t("common.edit")}
                                                </Button>
                                                <Dialog open={selectedChildId === child.id} onOpenChange={(open) => {
                                                    if (open) {
                                                        setSelectedChildId(child.id);
                                                        setAllowanceAmount(child.monthlyAllowance?.toString() || "");
                                                    } else {
                                                        setSelectedChildId(null);
                                                    }
                                                }}>
                                                    <DialogTrigger asChild>
                                                        <Button variant="outline" size="sm" className="w-full hover:bg-green-50 hover:text-green-700 hover:border-green-200 border-dashed" data-testid="set-allowance-button">
                                                            Mesada
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
                                                                    className="col-span-3 text-lg"
                                                                    placeholder="0.00"
                                                                    autoFocus
                                                                />
                                                            </div>
                                                        </div>
                                                        <Button onClick={handleSetAllowance} className="w-full bg-green-600 hover:bg-green-700" data-testid="save-allowance-button">{t("dashboard.save")}</Button>
                                                    </DialogContent>
                                                </Dialog>
                                            </div>

                                            <Button
                                                variant="secondary"
                                                size="sm"
                                                className="w-full bg-sky-100 text-sky-700 hover:bg-sky-200 border-none dark:bg-sky-900/30 dark:text-sky-300 dark:hover:bg-sky-900/50 shadow-sm"
                                                data-testid="generate-code-button"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleGenerateOnboardingCode(child.id);
                                                }}
                                            >
                                                <QrCode className="mr-2 h-4 w-4" /> {t("dashboard.generateCode")}
                                            </Button>

                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                className="w-full text-muted-foreground hover:text-destructive hover:bg-destructive/5 text-xs h-8 mt-2"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    setDeletingChild(child);
                                                }}
                                                data-testid="delete-child-button"
                                            >
                                                <Trash2 className="mr-2 h-3 w-3" /> {t("common.delete")}
                                            </Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))}

                            {children.length === 0 && !isLoading && (
                                <div className="col-span-full flex flex-col items-center justify-center py-16 text-muted-foreground bg-card/50 rounded-2xl border-2 border-dashed border-border">
                                    <div className="h-16 w-16 bg-muted rounded-full flex items-center justify-center mb-4">
                                        <Plus className="h-8 w-8 text-muted-foreground" />
                                    </div>
                                    <p className="text-lg font-medium">{t("dashboard.noChildren")}</p>
                                    <Button variant="link" onClick={() => navigate("/add-child")} className="mt-2 text-primary">
                                        {t("dashboard.addChild")}
                                    </Button>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                <Dialog open={!!onboardingCode} onOpenChange={() => setOnboardingCode(null)}>
                    <DialogContent className="sm:max-w-md">
                        <DialogHeader>
                            <DialogTitle>{t("dashboard.onboardingCode.title")}</DialogTitle>
                        </DialogHeader>
                        <div className="text-center p-6 space-y-6">
                            <p className="text-muted-foreground">
                                {t("dashboard.onboardingCode.instruction")}
                            </p>
                            <div className="text-5xl font-mono font-bold tracking-widest text-primary bg-primary/5 p-8 rounded-2xl border-2 border-primary/20 border-dashed">
                                {onboardingCode}
                            </div>
                        </div>
                        <Button
                            className="w-full"
                            onClick={() => {
                                if (onboardingCode) {
                                    navigator.clipboard.writeText(onboardingCode);
                                    toast.success(t("dashboard.onboardingCode.copied"));
                                }
                            }}
                        >
                            {t("dashboard.onboardingCode.copy")}
                        </Button>
                    </DialogContent>
                </Dialog>

                {/* Edit Child Dialog */}
                <Dialog open={!!editingChild} onOpenChange={() => setEditingChild(null)}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>{t("dashboard.child.editTitle", { name: editingChild?.name })}</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4 py-4">
                            <div className="space-y-2">
                                <Label htmlFor="edit-name">{t("child.add.name")}</Label>
                                <Input
                                    id="edit-name"
                                    value={editForm.name}
                                    onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                                    placeholder={t("child.add.namePlaceholder")}
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="edit-age">{t("child.add.age")}</Label>
                                    <Input
                                        id="edit-age"
                                        type="number"
                                        value={editForm.age}
                                        onChange={(e) => setEditForm({ ...editForm, age: parseInt(e.target.value) })}
                                        placeholder={t("child.add.agePlaceholder")}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="edit-phone">{t("child.add.phone")}</Label>
                                    <Input
                                        id="edit-phone"
                                        value={editForm.phoneNumber}
                                        onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                                        placeholder={t("child.add.phonePlaceholder")}
                                    />
                                </div>
                            </div>
                        </div>
                        <div className="flex gap-3 justify-end">
                            <Button variant="outline" onClick={() => setEditingChild(null)}>{t("common.cancel")}</Button>
                            <Button onClick={handleSaveEdit} data-testid="save-edit-button">{t("common.save")}</Button>
                        </div>
                    </DialogContent>
                </Dialog>

                {/* Delete Confirmation Dialog */}
                <Dialog open={!!deletingChild} onOpenChange={() => setDeletingChild(null)}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>{t("dashboard.child.deleteTitle")}</DialogTitle>
                        </DialogHeader>
                        <div className="bg-destructive/5 p-4 rounded-lg border border-destructive/10 text-center my-2">
                            <p className="text-destructive font-medium">{t("dashboard.child.deleteConfirm", { name: deletingChild?.name })}</p>
                        </div>
                        <div className="flex gap-3 justify-end mt-4">
                            <Button variant="outline" onClick={() => setDeletingChild(null)}>{t("common.cancel")}</Button>
                            <Button
                                variant="destructive"
                                onClick={handleDeleteChild}
                                data-testid="confirm-delete-button"
                                className="shadow-lg shadow-destructive/20"
                            >
                                {t("common.delete")}
                            </Button>
                        </div>
                    </DialogContent>
                </Dialog>
            </div>
        </div>
    );
}
