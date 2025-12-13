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
import { childService } from "@/services/childService";
import { taskService } from "@/services/taskService";
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
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-4xl mx-auto space-y-8">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">{t("dashboard.title")}</h1>
                        <p className="text-gray-500">{t("dashboard.welcome", { name: parentName || "Parent" })}</p>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="outline" onClick={handleLogout} data-testid="logout-button">
                            <LogOut className="mr-2 h-4 w-4" /> {t("dashboard.logout.button")}
                        </Button>
                        <Button variant="outline" onClick={() => navigate("/gift-cards")} data-testid="gift-cards-button">
                            <Gift className="mr-2 h-4 w-4" /> {t("dashboard.rewardsStore")}
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
                    <div className="space-y-8">
                        {/* Pending Approvals Section */}
                        {pendingTasks.length > 0 && (
                            <Card className="border-2 border-yellow-400 bg-yellow-50">
                                <CardHeader>
                                    <CardTitle className="text-xl font-bold text-yellow-800 flex items-center gap-2">
                                        <Sparkles className="h-5 w-5" />
                                        {t("dashboard.pendingApprovals.title")}
                                    </CardTitle>
                                </CardHeader>
                                <CardContent>
                                    <div className="space-y-4">
                                        {pendingTasks.map((item) => (
                                            <div key={item.task.id} className="flex items-center justify-between bg-white p-4 rounded-lg shadow-sm border border-yellow-200">
                                                <div>
                                                    <p className="font-semibold text-gray-800">{item.task.description}</p>
                                                    <p className="text-sm text-gray-500">
                                                        {t("dashboard.pendingApprovals.madeBy")}: <span className="font-medium text-gray-700">{item.childName}</span>
                                                    </p>
                                                    {item.task.proofImageUrl && (
                                                        <a href={item.task.proofImageUrl} target="_blank" rel="noopener noreferrer" className="text-xs text-blue-600 hover:underline">
                                                            {t("dashboard.pendingApprovals.viewProof")}
                                                        </a>
                                                    )}
                                                </div>
                                                <Button
                                                    onClick={() => item.task.id && handleApproveTask(item.childId, item.task.id)}
                                                    className="bg-green-600 hover:bg-green-700 text-white"
                                                >
                                                    {t("dashboard.pendingApprovals.approve")}
                                                </Button>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        )}

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
                                            <div className="flex gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    className="flex-1"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        handleEditChild(child);
                                                    }}
                                                    data-testid="edit-child-button"
                                                >
                                                    <Edit className="mr-2 h-4 w-4" /> {t("common.edit")}
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    className="flex-1 text-red-600 hover:text-red-700"
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setDeletingChild(child);
                                                    }}
                                                    data-testid="delete-child-button"
                                                >
                                                    <Trash2 className="mr-2 h-4 w-4" /> {t("common.delete")}
                                                </Button>
                                            </div>
                                            <Dialog open={selectedChildId === child.id} onOpenChange={(open) => {
                                                if (open) {
                                                    setSelectedChildId(child.id);
                                                    setAllowanceAmount(child.monthlyAllowance?.toString() || "");
                                                } else {
                                                    setSelectedChildId(null);
                                                }
                                            }}>
                                                <DialogTrigger asChild>
                                                    <Button variant="outline" size="sm" className="w-full" data-testid="set-allowance-button">
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
                                                    <Button onClick={handleSetAllowance} data-testid="save-allowance-button">{t("dashboard.save")}</Button>
                                                </DialogContent>
                                            </Dialog>
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                className="w-full"
                                                data-testid="generate-code-button"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleGenerateOnboardingCode(child.id);
                                                }}
                                            >
                                                <QrCode className="mr-2 h-4 w-4" /> {t("dashboard.generateCode")}
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
                    </div>
                )}

                <Dialog open={!!onboardingCode} onOpenChange={() => setOnboardingCode(null)}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>{t("dashboard.onboardingCode.title")}</DialogTitle>
                        </DialogHeader>
                        <div className="text-center p-6 space-y-4">
                            <p className="text-gray-600">
                                {t("dashboard.onboardingCode.instruction")}
                            </p>
                            <div className="text-4xl font-mono font-bold tracking-wider bg-gray-100 p-4 rounded-lg">
                                {onboardingCode}
                            </div>
                        </div>
                        <Button
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
                            <div>
                                <Label htmlFor="edit-name">{t("child.add.name")}</Label>
                                <Input
                                    id="edit-name"
                                    value={editForm.name}
                                    onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                                    placeholder={t("child.add.namePlaceholder")}
                                />
                            </div>
                            <div>
                                <Label htmlFor="edit-age">{t("child.add.age")}</Label>
                                <Input
                                    id="edit-age"
                                    type="number"
                                    value={editForm.age}
                                    onChange={(e) => setEditForm({ ...editForm, age: parseInt(e.target.value) })}
                                    placeholder={t("child.add.agePlaceholder")}
                                />
                            </div>
                            <div>
                                <Label htmlFor="edit-phone">{t("child.add.phone")}</Label>
                                <Input
                                    id="edit-phone"
                                    value={editForm.phoneNumber}
                                    onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                                    placeholder={t("child.add.phonePlaceholder")}
                                />
                            </div>
                        </div>
                        <div className="flex gap-2 justify-end">
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
                        <p className="text-gray-600">{t("dashboard.child.deleteConfirm", { name: deletingChild?.name })}</p>
                        <div className="flex gap-2 justify-end mt-4">
                            <Button variant="outline" onClick={() => setDeletingChild(null)}>{t("common.cancel")}</Button>
                            <Button
                                variant="destructive"
                                onClick={handleDeleteChild}
                                data-testid="confirm-delete-button"
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
