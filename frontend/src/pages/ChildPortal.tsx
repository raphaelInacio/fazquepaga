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
import { Sparkles, Trophy, LogOut, Rocket } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";

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
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-100 to-pink-100">
                <div className="text-2xl font-bold text-purple-600">{t("common.loading")}</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 via-pink-50 to-blue-100 p-4 pb-20">
            {/* Header */}
            <div className="max-w-4xl mx-auto mb-6">
                <Card className="bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-xl">
                    <CardContent className="p-6">
                        <div className="flex justify-between items-center">
                            <div>
                                <h1 className="text-3xl font-bold mb-2">{t("childPortal.hello", { name: child?.name })}</h1>
                                <div className="flex items-center gap-2 text-xl">
                                    <Trophy className="w-6 h-6" />
                                    <span className="font-bold">{t("childPortal.balance", { balance: child?.balance?.toFixed(2) || "0.00" })}</span>
                                </div>
                            </div>
                            <Button
                                onClick={handleLogout}
                                variant="secondary"
                                className="bg-white text-purple-600 hover:bg-gray-100"
                            >
                                <LogOut className="w-4 h-4 mr-2" />
                                {t("childPortal.logout")}
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            </div>

            <div className="max-w-4xl mx-auto grid gap-6 md:grid-cols-2">
                {/* Tasks Section */}
                <div className="md:col-span-2">
                    <Card className="shadow-lg">
                        <CardHeader className="flex flex-row items-center justify-between">
                            <CardTitle className="text-2xl font-bold text-purple-700">
                                {t("childPortal.myTasks")}
                            </CardTitle>
                            <div className="flex items-center space-x-2">
                                <Switch
                                    checked={isAdventureMode}
                                    onCheckedChange={toggleAdventureMode}
                                    id="adventure-mode"
                                />
                                <Label htmlFor="adventure-mode" className="font-bold text-purple-700">
                                    {isAdventureMode ? t("childPortal.adventureModeOn") : t("childPortal.adventureMode")}
                                </Label>
                            </div>
                        </CardHeader>
                        <CardContent>
                            {tasks.length === 0 ? (
                                <div className="text-center py-12 text-gray-500">
                                    <div className="text-6xl mb-4">🎉</div>
                                    <p className="text-xl font-semibold">{t("childPortal.allTasksCompleted")}</p>
                                    <p className="text-lg">{t("childPortal.congratulations")}</p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {tasks.map((task) => (
                                        <Card key={task.id} className="border-2 border-purple-200 hover:border-purple-400 transition-all">
                                            <CardContent className="p-4">
                                                <div className="flex justify-between items-center gap-4">
                                                    <div className="flex-1">
                                                        <p className="text-lg font-semibold text-gray-800">
                                                            {getTaskDescription(task)}
                                                        </p>
                                                        <p className="text-sm text-green-600 font-bold mt-1">
                                                            💰 R$ {task.value?.toFixed(2) || "0.00"}
                                                        </p>
                                                    </div>
                                                    <Button
                                                        onClick={() => handleCompleteTask(task.id)}
                                                        className={`w-full font-bold text-white shadow-md transform transition hover:scale-105 ${isAdventureMode
                                                                ? "bg-gradient-to-r from-yellow-400 to-orange-500 hover:from-yellow-500 hover:to-orange-600"
                                                                : "bg-green-500 hover:bg-green-600"
                                                            }`}
                                                    >
                                                        {t("childPortal.alreadyDone")}
                                                    </Button>
                                                </div>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>

                {/* Goal Coach Section */}
                <Card className="shadow-lg border-2 border-blue-200">
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold text-blue-700 flex items-center gap-2">
                            <Rocket className="w-6 h-6" />
                            {t("childPortal.financialCoach")}
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <label className="text-sm font-semibold text-gray-700 block mb-2">
                                {t("childPortal.goalQuestion")}
                            </label>
                            <Input
                                placeholder={t("childPortal.goalPlaceholder")}
                                value={goalDescription}
                                onChange={(e) => setGoalDescription(e.target.value)}
                                className="text-lg"
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-gray-700 block mb-2">
                                {t("childPortal.costQuestion")}
                            </label>
                            <Input
                                type="number"
                                placeholder={t("childPortal.costPlaceholder")}
                                value={targetAmount}
                                onChange={(e) => setTargetAmount(e.target.value)}
                                className="text-lg"
                            />
                        </div>
                        <Button
                            onClick={handleCreatePlan}
                            disabled={isCreatingPlan}
                            className="w-full bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 font-bold h-12"
                        >
                            {isCreatingPlan ? t("childPortal.creatingPlan") : t("childPortal.createPlan")}
                        </Button>

                        {goalPlan && (
                            <Card className="bg-gradient-to-r from-blue-50 to-cyan-50 border-2 border-blue-300">
                                <CardContent className="p-4">
                                    <p className="text-sm font-semibold text-blue-900 whitespace-pre-wrap">
                                        {goalPlan}
                                    </p>
                                </CardContent>
                            </Card>
                        )}
                    </CardContent>
                </Card>

                {/* Fun Stats Card */}
                <Card className="shadow-lg border-2 border-yellow-200">
                    <CardHeader>
                        <CardTitle className="text-2xl font-bold text-yellow-700">
                            {t("childPortal.achievements")}
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-3 bg-yellow-50 rounded-lg">
                                <span className="font-semibold text-gray-700">{t("childPortal.pendingTasks")}</span>
                                <span className="text-2xl font-bold text-yellow-600">{tasks.length}</span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
                                <span className="font-semibold text-gray-700">{t("childPortal.currentBalance")}</span>
                                <span className="text-2xl font-bold text-green-600">
                                    R$ {child?.balance?.toFixed(2) || "0.00"}
                                </span>
                            </div>
                            <div className="flex justify-between items-center p-3 bg-purple-50 rounded-lg">
                                <span className="font-semibold text-gray-700">{t("childPortal.monthlyAllowance")}</span>
                                <span className="text-2xl font-bold text-purple-600">
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
