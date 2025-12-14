import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { childAuthService } from "@/services/childAuthService";
import { childService } from "@/services/childService";
import { taskService } from "@/services/taskService";
import { aiService, AdventureTask } from "@/services/aiService";
import { Task } from "@/types";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { Sparkles, Trophy, LogOut, Rocket, Gamepad2, Target, Sword, CheckCircle2, Coins } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import confetti from "canvas-confetti";

export default function ChildPortal() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [child, setChild] = useState<any>(null);
    const [tasks, setTasks] = useState<Task[]>([]);
    const [adventureTasks, setAdventureTasks] = useState<AdventureTask[]>([]);
    const [isAdventureMode, setIsAdventureMode] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    // Goal Coach state
    const [goalDescription, setGoalDescription] = useState("");
    const [targetAmount, setTargetAmount] = useState("");
    const [goalPlan, setGoalPlan] = useState("");
    const [isLoadingGoal, setIsLoadingGoal] = useState(false);
    const [isCreatingPlan, setIsCreatingPlan] = useState(false);

    useEffect(() => {
        const currentChild = childAuthService.getCurrentChild();
        if (!currentChild) {
            navigate("/child-login");
            return;
        }
        setChild(currentChild);
        loadTasks(currentChild.id);
    }, [navigate]);

    async function loadTasks(childId: string) {
        setIsLoading(true);
        try {
            const fetchedTasks = await taskService.getTasks(childId);
            // Filter only pending tasks
            const pendingTasks = fetchedTasks.filter((t: Task) => t.status === "PENDING");
            setTasks(pendingTasks);
        } catch (error) {
            toast.error(t("childPortal.loadError"));
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    async function refreshChildData() {
        if (!child) return;
        try {
            const updatedChild = await childService.getChild(child.id, child.parentId);
            setChild(updatedChild);
            localStorage.setItem('fazquepaga_child', JSON.stringify(updatedChild));
        } catch (error) {
            console.error("Failed to refresh child data", error);
        }
    }

    async function handleCompleteTask(taskId: string | undefined) {
        if (!child || !taskId) return;

        try {
            await taskService.completeTask(taskId, child.id);
            toast.success(t("childPortal.taskCompleted"));

            // Celebration!
            confetti({
                particleCount: 150,
                spread: 80,
                origin: { y: 0.6 },
                colors: ['#FFD700', '#FFA500', '#FF4500', '#8A2BE2']
            });

            // Update local state
            setTasks(tasks.map(t => t.id === taskId ? { ...t, status: 'PENDING_APPROVAL' } : t));
            loadTasks(child.id);
            refreshChildData();
        } catch (error) {
            toast.error(t("childPortal.taskCompletionError"));
            console.error(error);
        }
    }

    async function toggleAdventureMode(checked: boolean) {
        if (checked && tasks.length > 0) {
            try {
                const adventures = await aiService.getAdventureTasks(tasks);
                setAdventureTasks(adventures);
                setIsAdventureMode(true);
                toast.success(t("childPortal.adventureModeActivated"));
                confetti({
                    particleCount: 50,
                    spread: 60,
                    origin: { y: 0.8 },
                    colors: ['#8A2BE2', '#FFFFFF']
                });
            } catch (error) {
                toast.error(t("childPortal.adventureModeError"));
                console.error(error);
                setIsAdventureMode(false);
            }
        } else {
            setIsAdventureMode(false);
            toast.info(t("childPortal.normalModeActivated"));
        }
    }

    async function handleCreatePlan() {
        if (!child || !goalDescription || !targetAmount) {
            toast.error(t("childPortal.fillGoal"));
            return;
        }

        setIsCreatingPlan(true);
        try {
            const response = await aiService.getGoalCoachPlan(
                child.id,
                goalDescription,
                parseFloat(targetAmount)
            );
            setGoalPlan(response.plan);
            toast.success(t("childPortal.planCreated"));
            setGoalDescription("");
            setTargetAmount("");

            confetti({
                particleCount: 100,
                spread: 100,
                origin: { y: 0.5 },
                colors: ['#00BFFF', '#1E90FF']
            });

        } catch (error) {
            toast.error(t("childPortal.planError"));
            console.error(error);
        } finally {
            setIsCreatingPlan(false);
        }
    }

    function handleLogout() {
        childAuthService.logout();
        navigate("/child-login");
    }

    function getTaskDescription(task: Task): string {
        if (isAdventureMode && task.id) {
            const adventureTask = adventureTasks.find(at => at.id === task.id);
            return adventureTask?.adventureDescription || task.description || "";
        }
        return task.description || "";
    }

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background via-purple-50 to-blue-50">
                <div className="flex flex-col items-center gap-4">
                    <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin" />
                    <div className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600">
                        {t("common.loading")}
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-background via-purple-50/50 to-blue-50/50 p-4 pb-20 overflow-x-hidden relative">
            {/* Playful background elements */}
            <div className="absolute top-20 left-20 w-32 h-32 bg-yellow-400/10 rounded-full blur-3xl animate-pulse" />
            <div className="absolute bottom-40 right-10 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl animate-pulse delay-700" />
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-primary/5 rounded-full blur-3xl -z-10" />

            {/* Header */}
            <div className="max-w-4xl mx-auto mb-8 relative z-10">
                <Card className="bg-card/80 backdrop-blur-md border border-white/20 shadow-glow rounded-3xl overflow-hidden">
                    <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-primary via-purple-500 to-blue-500" />
                    <CardContent className="p-6 md:p-8">
                        <div className="flex flex-col md:flex-row justify-between items-center gap-6">
                            <div className="flex items-center gap-4">
                                <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-primary to-purple-600 p-[2px] shadow-lg shadow-primary/20">
                                    <div className="h-full w-full rounded-[14px] bg-card flex items-center justify-center">
                                        <Trophy className="w-8 h-8 text-primary" />
                                    </div>
                                </div>
                                <div>
                                    <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600 font-heading">
                                        {t("childPortal.hello", { name: child?.name })}
                                    </h1>
                                    <div className="flex items-center gap-2 mt-1">
                                        <div className="px-3 py-1 rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 font-bold text-sm flex items-center gap-1.5 border border-green-200 dark:border-green-800">
                                            <Coins className="w-4 h-4" />
                                            {t("childPortal.balance", { balance: child?.balance?.toFixed(2) || "0.00" })}
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <Button
                                onClick={handleLogout}
                                variant="outline"
                                className="rounded-full border-2 hover:bg-destructive/5 hover:text-destructive hover:border-destructive/30 transition-all font-semibold"
                            >
                                <LogOut className="w-4 h-4 mr-2" />
                                {t("childPortal.logout")}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>

            <div className="max-w-4xl mx-auto grid gap-8 md:grid-cols-2 relative z-10">
                {/* Tasks Section */}
                <div className="md:col-span-2 space-y-6">
                    <div className="flex items-center justify-between px-2">
                        <h2 className="text-2xl font-bold text-foreground flex items-center gap-2">
                            {isAdventureMode ? <Sword className="w-6 h-6 text-orange-500" /> : <Gamepad2 className="w-6 h-6 text-primary" />}
                            {t("childPortal.myTasks")}
                        </h2>
                        <div className="flex items-center gap-3 bg-card p-2 rounded-xl border shadow-sm">
                            <Label htmlFor="adventure-mode" className={`font-bold text-sm cursor-pointer ${isAdventureMode ? "text-orange-500" : "text-muted-foreground"}`}>
                                {isAdventureMode ? t("childPortal.adventureModeOn") : t("childPortal.adventureMode")}
                            </Label>
                            <Switch
                                checked={isAdventureMode}
                                onCheckedChange={toggleAdventureMode}
                                id="adventure-mode"
                                className="data-[state=checked]:bg-orange-500"
                            />
                        </div>
                    </div>

                    <div className="grid gap-4">
                        {tasks.length === 0 ? (
                            <Card className="border-2 border-dashed border-border/60 bg-card/50 shadow-sm">
                                <CardContent className="flex flex-col items-center justify-center py-16 text-center space-y-4">
                                    <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center animate-bounce">
                                        <Trophy className="h-10 w-10 text-green-600" />
                                    </div>
                                    <div className="space-y-2">
                                        <p className="text-2xl font-bold text-foreground">{t("childPortal.allTasksCompleted")}</p>
                                        <p className="text-muted-foreground">{t("childPortal.congratulations")}</p>
                                    </div>
                                </CardContent>
                            </Card>
                        ) : (
                            tasks.map((task) => (
                                <Card
                                    key={task.id}
                                    className={`group overflow-hidden border-2 transition-all duration-300 hover:scale-[1.01] hover:shadow-lg ${isAdventureMode
                                            ? "border-orange-200 hover:border-orange-400 bg-orange-50/30 dark:bg-orange-950/20"
                                            : "border-border hover:border-primary/50 bg-card"
                                        }`}
                                >
                                    <CardContent className="p-0">
                                        <div className="flex items-stretch min-h-[5rem]">
                                            {/* Left decoration */}
                                            <div className={`w-3 ${isAdventureMode ? "bg-gradient-to-b from-orange-400 to-red-500" : "bg-gradient-to-b from-primary to-purple-600"}`} />

                                            <div className="flex-1 p-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
                                                <div className="space-y-1">
                                                    <h3 className={`text-xl font-bold ${isAdventureMode ? "text-orange-800 dark:text-orange-300 font-heading" : "text-foreground"}`}>
                                                        {getTaskDescription(task)}
                                                    </h3>
                                                    {task.value && task.value > 0 && (
                                                        <div className="flex items-center gap-1.5 text-green-600 dark:text-green-400 font-bold bg-green-100 dark:bg-green-900/30 w-fit px-2 py-0.5 rounded-md text-sm">
                                                            <Coins className="w-3.5 h-3.5" />
                                                            R$ {task.value.toFixed(2)}
                                                        </div>
                                                    )}
                                                </div>

                                                <Button
                                                    onClick={() => handleCompleteTask(task.id)}
                                                    className={`h-12 px-8 font-bold text-white shadow-md rounded-xl transition-all active:scale-95 text-lg ${isAdventureMode
                                                            ? "bg-gradient-to-r from-orange-500 to-red-600 hover:from-orange-600 hover:to-red-700 shadow-orange-500/20"
                                                            : "bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 shadow-green-500/20"
                                                        }`}
                                                >
                                                    <CheckCircle2 className="w-5 h-5 mr-2" />
                                                    {t("childPortal.alreadyDone")}
                                                </Button>
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))
                        )}
                    </div>
                </div>

                {/* Goal Coach Section */}
                <Card className="shadow-lg border-2 border-primary/20 bg-gradient-to-br from-card to-background rounded-3xl overflow-hidden relative group">
                    <div className="absolute top-0 right-0 p-8 opacity-5 group-hover:opacity-10 transition-opacity">
                        <Rocket className="w-32 h-32 text-primary" />
                    </div>
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-cyan-500 flex items-center gap-2">
                            <Rocket className="w-6 h-6 text-blue-500" />
                            {t("childPortal.financialCoach")}
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-6 relative">
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label className="text-sm font-bold text-muted-foreground uppercase tracking-wider">
                                    {t("childPortal.goalQuestion")}
                                </Label>
                                <Input
                                    placeholder={t("childPortal.goalPlaceholder")}
                                    value={goalDescription}
                                    onChange={(e) => setGoalDescription(e.target.value)}
                                    className="text-lg h-12 rounded-xl bg-background/50 border-input/50 focus:border-blue-500/50"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label className="text-sm font-bold text-muted-foreground uppercase tracking-wider">
                                    {t("childPortal.costQuestion")}
                                </Label>
                                <Input
                                    type="number"
                                    placeholder={t("childPortal.costPlaceholder")}
                                    value={targetAmount}
                                    onChange={(e) => setTargetAmount(e.target.value)}
                                    className="text-lg h-12 rounded-xl bg-background/50 border-input/50 focus:border-blue-500/50"
                                />
                            </div>
                        </div>

                        <Button
                            onClick={handleCreatePlan}
                            disabled={isCreatingPlan}
                            className="w-full h-12 text-lg font-bold bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 shadow-lg shadow-blue-500/20 rounded-xl"
                        >
                            {isCreatingPlan ? (
                                <>
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2" />
                                    {t("childPortal.creatingPlan")}
                                </>
                            ) : (
                                <>
                                    <Sparkles className="w-5 h-5 mr-2" />
                                    {t("childPortal.createPlan")}
                                </>
                            )}
                        </Button>

                        {goalPlan && (
                            <div className="mt-6 bg-blue-50/50 dark:bg-blue-900/10 p-4 rounded-xl border border-blue-100 dark:border-blue-900/50 animate-fade-in">
                                <p className="text-sm font-medium text-blue-800 dark:text-blue-300 whitespace-pre-wrap leading-relaxed">
                                    {goalPlan}
                                </p>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Fun Stats Card */}
                <Card className="shadow-lg border-2 border-yellow-400/20 bg-gradient-to-br from-card to-background rounded-3xl overflow-hidden">
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-yellow-500 to-amber-600 flex items-center gap-2">
                            <Target className="w-6 h-6 text-amber-500" />
                            {t("childPortal.achievements")}
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-4 bg-gradient-to-r from-yellow-50 to-amber-50 dark:from-yellow-900/10 dark:to-amber-900/10 rounded-2xl border border-yellow-100 dark:border-yellow-900/20">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-yellow-100 dark:bg-yellow-900/30 rounded-lg">
                                        <Target className="w-5 h-5 text-yellow-600 dark:text-yellow-400" />
                                    </div>
                                    <span className="font-semibold text-foreground">{t("childPortal.pendingTasks")}</span>
                                </div>
                                <span className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{tasks.length}</span>
                            </div>

                            <div className="flex justify-between items-center p-4 bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/10 dark:to-emerald-900/10 rounded-2xl border border-green-100 dark:border-green-900/20">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-lg">
                                        <Coins className="w-5 h-5 text-green-600 dark:text-green-400" />
                                    </div>
                                    <span className="font-semibold text-foreground">{t("childPortal.currentBalance")}</span>
                                </div>
                                <span className="text-2xl font-bold text-green-600 dark:text-green-400">
                                    R$ {child?.balance?.toFixed(2) || "0.00"}
                                </span>
                            </div>

                            <div className="flex justify-between items-center p-4 bg-gradient-to-r from-purple-50 to-violet-50 dark:from-purple-900/10 dark:to-violet-900/10 rounded-2xl border border-purple-100 dark:border-purple-900/20">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
                                        <Trophy className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                                    </div>
                                    <span className="font-semibold text-foreground">{t("childPortal.monthlyAllowance")}</span>
                                </div>
                                <span className="text-2xl font-bold text-purple-600 dark:text-purple-400">
                                    R$ {child?.monthlyAllowance?.toFixed(2) || "0.00"}
                                </span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
