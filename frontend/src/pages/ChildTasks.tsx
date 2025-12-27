import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from "@/components/ui/card";
import { toast } from "sonner";
import { Task, User } from "@/types";
import { ArrowLeft, Plus, Sparkles, Calendar, CheckCircle2, Trophy, Clock, Target, Check, X, Trash2 } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { FinancialLedger } from "@/components/FinancialLedger";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { childService } from "@/services/childService";
import { taskService } from "@/services/taskService";
import { allowanceService } from "@/services/allowanceService";
import api from "@/lib/api";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import confetti from "canvas-confetti";
import { Mascot } from "@/components/Mascot";

export default function ChildTasks() {
    const { t } = useTranslation();
    const { childId } = useParams<{ childId: string }>();
    const navigate = useNavigate();
    const [child, setChild] = useState<User | null>(null);
    const [tasks, setTasks] = useState<Task[]>([]);
    const [isLoadingTasks, setIsLoadingTasks] = useState(true);
    const [filterStatus, setFilterStatus] = useState<string>("ALL");
    const [predictedAllowance, setPredictedAllowance] = useState<number>(0);
    const [isLoadingAllowance, setIsLoadingAllowance] = useState(false);
    const [isReviewDialogOpen, setIsReviewDialogOpen] = useState(false);
    const [selectedTaskForReview, setSelectedTaskForReview] = useState<Task | null>(null);
    const [isCreateTaskDialogOpen, setIsCreateTaskDialogOpen] = useState(false);
    const [newTask, setNewTask] = useState<Partial<Task>>({
        description: "",
        type: "ONE_TIME",
        requiresProof: false,
        weight: "MEDIUM"
    });
    const [aiSuggestions, setAiSuggestions] = useState<string[]>([]);
    const [isLoadingAI, setIsLoadingAI] = useState(false);
    const [showAISuggestions, setShowAISuggestions] = useState(false);

    useEffect(() => {
        if (!childId) {
            navigate("/dashboard");
            return;
        }

        const fetchChildData = async () => {
            const parentId = localStorage.getItem("parentId");
            if (!parentId) return;
            try {
                const childData = await childService.getChild(childId, parentId);
                setChild(childData);
            } catch (error) {
                toast.error("Failed to load child data");
                console.error(error);
                navigate("/dashboard");
            }
        };

        fetchChildData();
        loadTasks();
    }, [childId, navigate]);

    const loadTasks = async () => {
        if (!childId) return;

        setIsLoadingTasks(true);
        try {
            const tasksData = await taskService.getTasks(childId);
            setTasks(tasksData);
        } catch (error) {
            toast.error("Failed to load tasks");
            console.error(error);
        } finally {
            setIsLoadingTasks(false);
        }
    };

    const loadPredictedAllowance = async () => {
        if (!childId) return;
        setIsLoadingAllowance(true);
        try {
            const data = await allowanceService.getPredictedAllowance(childId);
            setPredictedAllowance(data.predicted_allowance);
        } catch (error) {
            console.error("Failed to load predicted allowance", error);
        } finally {
            setIsLoadingAllowance(false);
        }
    };

    useEffect(() => {
        loadPredictedAllowance();
    }, [childId, tasks]);

    const handleReviewClick = (task: Task) => {
        setSelectedTaskForReview(task);
        setIsReviewDialogOpen(true);
    };

    const handleApproveTask = async () => {
        if (!selectedTaskForReview || !childId) return;

        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await taskService.approveTask(childId, selectedTaskForReview.id!, parentId);
            toast.success("Task approved successfully!");

            confetti({
                particleCount: 50,
                spread: 60,
                origin: { y: 0.7 },
                colors: ['#26ccff', '#a25afd', '#ff5e7e', '#88ff5a', '#fcff42']
            });

            setIsReviewDialogOpen(false);
            setSelectedTaskForReview(null);
            loadTasks();
        } catch (error) {
            toast.error("Failed to approve task");
            console.error(error);
        }
    };

    const handleAcknowledgeTask = async () => {
        if (!selectedTaskForReview || !childId) return;
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await taskService.acknowledgeTask(childId, selectedTaskForReview.id!, parentId);
            toast.success(t("childTasks.review.taskDismissed"));
            setIsReviewDialogOpen(false);
            setSelectedTaskForReview(null);
            loadTasks();
        } catch (error) {
            toast.error(t("childTasks.review.taskDismissError"));
        }
    };

    const handleRejectTask = async () => {
        if (!selectedTaskForReview || !childId) return;
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await taskService.rejectTask(childId, selectedTaskForReview.id!, parentId);
            toast.success(t("childTasks.review.taskRejected"));
            setIsReviewDialogOpen(false);
            setSelectedTaskForReview(null);
            loadTasks();
            loadPredictedAllowance(); // Update balance
        } catch (error) {
            toast.error(t("childTasks.review.taskRejectError"));
        }
    };

    const handleDeleteTask = async () => {
        if (!selectedTaskForReview || !childId) return;
        const parentId = localStorage.getItem("parentId");
        if (!parentId) return;

        try {
            await taskService.deleteTask(childId, selectedTaskForReview.id!, parentId);
            toast.success("Task deleted successfully");
            setIsReviewDialogOpen(false);
            setSelectedTaskForReview(null);
            loadTasks();
            loadPredictedAllowance();
        } catch (error) {
            toast.error("Failed to delete task");
        }
    };

    const handleCreateTask = async () => {
        if (!childId || !newTask.description) return;
        try {
            await taskService.createTask(childId, {
                description: newTask.description!,
                type: newTask.type as any,
                weight: newTask.weight as any,
                requiresProof: newTask.requiresProof || false,
            });
            toast.success("Task created successfully");
            setIsCreateTaskDialogOpen(false);
            setNewTask({
                description: "",
                type: "ONE_TIME",
                requiresProof: false,
                weight: "MEDIUM"
            });
            loadTasks();
        } catch (error) {
            toast.error("Failed to create task");
            console.error(error);
        }
    };

    const handleGetAISuggestions = async () => {
        if (!child?.age) {
            toast.error("Child age not available");
            return;
        }

        setIsLoadingAI(true);
        try {
            const response = await api.get(`/api/v1/ai/tasks/suggestions?age=${child.age}`);
            const suggestions = response.data.suggestions || response.data || [];

            setAiSuggestions(suggestions);
            setShowAISuggestions(true);
            toast.success("AI suggestions generated!");
        } catch (error) {
            toast.error("Failed to load AI suggestions");
            console.error(error);
        } finally {
            setIsLoadingAI(false);
        }
    };

    const handleAddSuggestionAsTask = (suggestion: string) => {
        setNewTask({
            description: suggestion,
            type: "ONE_TIME",
            requiresProof: false,
            weight: "MEDIUM",
            value: 0
        });
        setIsCreateTaskDialogOpen(true);
        setAiSuggestions(prev => prev.filter(s => s !== suggestion));
    };

    const getStatusColor = (status?: string) => {
        switch (status) {
            case "COMPLETED":
                return "bg-green-500 shadow-green-200";
            case "PENDING_APPROVAL":
                return "bg-yellow-500 shadow-yellow-200";
            case "APPROVED":
                return "bg-blue-500 shadow-blue-200";
            default:
                return "bg-gray-400";
        }
    };

    const getWeightVariant = (weight: string) => {
        switch (weight) {
            case "HIGH":
                return "destructive";
            case "MEDIUM":
                return "default";
            case "LOW":
                return "secondary";
            default:
                return "outline";
        }
    };

    const filteredTasks = tasks.filter(task => {
        if (filterStatus === 'ALL') {
            return task.status !== 'COMPLETED'; // Hide completed from main list to keep it clean, or keep them? Let's hide completed to focus on active quests
        }
        return task.status === filterStatus;
    });

    if (!child) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-background">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    return (
        <Dialog open={isReviewDialogOpen} onOpenChange={setIsReviewDialogOpen}>
            <div className="min-h-screen bg-background p-8 animate-fade-in">
                <div className="max-w-6xl mx-auto space-y-8">
                    {/* Header Section */}
                    <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 bg-card p-6 rounded-3xl shadow-soft border border-border/50">
                        <div className="flex items-center gap-4">
                            <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => navigate("/dashboard")}
                                className="rounded-full hover:bg-primary/10"
                            >
                                <ArrowLeft className="h-6 w-6 text-primary" />
                            </Button>
                            <div>
                                <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600 font-heading">
                                    {t("tasks.title", { childName: child.name })}
                                </h1>
                                <div className="flex items-center gap-2 mt-1">
                                    <Badge variant="outline" className="text-muted-foreground border-primary/20">
                                        {t("childTasks.age", { age: child.age || "N/A" })}
                                    </Badge>
                                    <Badge variant="secondary" className="bg-blue-100 text-blue-700 hover:bg-blue-200 border-blue-200">
                                        Level {Math.floor((child.age || 0) / 2) + 1} Explorer
                                    </Badge>
                                </div>
                            </div>
                        </div>
                        <div className="flex gap-3 w-full md:w-auto">
                            <Button
                                onClick={handleGetAISuggestions}
                                disabled={isLoadingAI}
                                className="flex-1 md:flex-none bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white shadow-lg shadow-purple-500/20 rounded-xl transition-all hover:scale-105 active:scale-95"
                            >
                                <Sparkles className="h-4 w-4 mr-2" />
                                {isLoadingAI ? t("childTasks.generating") : t("childTasks.generateAiTasks")}
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => setIsCreateTaskDialogOpen(true)}
                                data-testid="create-task-button"
                                className="flex-1 md:flex-none border-dashed border-2 hover:border-primary hover:text-primary rounded-xl"
                            >
                                <Plus className="h-4 w-4 mr-2" />
                                {t("childTasks.createManualTask")}
                            </Button>
                        </div>
                    </div>


                    {isLoadingAI && (
                        <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex flex-col items-center justify-center animate-in fade-in duration-300">
                            <Mascot state="tech" width={200} className="animate-pulse" />
                            <p className="mt-4 text-xl font-bold text-primary animate-pulse">Generating quests...</p>
                        </div>
                    )}

                    {showAISuggestions && aiSuggestions.length > 0 && (
                        <Card className="border-none bg-gradient-to-br from-purple-500/5 to-blue-500/5 shadow-glow animate-fade-in overflow-hidden relative">
                            <div className="absolute top-0 right-0 p-8 opacity-5">
                                <Sparkles className="w-32 h-32 text-primary" />
                            </div>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2 text-xl text-primary">
                                    <Sparkles className="h-6 w-6" />
                                    {t("childTasks.aiSuggestions.title")}
                                </CardTitle>
                                <CardDescription>
                                    {t("childTasks.aiSuggestions.description")}
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                                    {aiSuggestions.map((suggestion, index) => (
                                        <div
                                            key={index}
                                            className="group flex flex-col justify-between p-5 bg-card rounded-2xl border border-primary/10 hover:border-primary/40 hover:shadow-soft transition-all duration-300"
                                        >
                                            <span className="text-foreground font-medium mb-4 leading-relaxed">{suggestion}</span>
                                            <Button
                                                onClick={() => handleAddSuggestionAsTask(suggestion)}
                                                className="w-full bg-primary/10 hover:bg-primary text-primary hover:text-primary-foreground rounded-lg transition-colors"
                                            >
                                                <Plus className="w-4 h-4 mr-2" />
                                                {t("childTasks.aiSuggestions.add")}
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    <Tabs defaultValue="tasks" className="w-full">
                        <TabsList className="grid w-full grid-cols-2 p-1 bg-card/50 rounded-2xl mb-8">
                            <TabsTrigger
                                value="tasks"
                                className="rounded-xl data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-md transition-all py-3"
                            >
                                <Target className="w-4 h-4 mr-2" />
                                {t("childTasks.tabs.tasks")}
                            </TabsTrigger>
                            <TabsTrigger
                                value="financial"
                                className="rounded-xl data-[state=active]:bg-primary data-[state=active]:text-primary-foreground data-[state=active]:shadow-md transition-all py-3"
                            >
                                <Trophy className="w-4 h-4 mr-2" />
                                {t("childTasks.tabs.financial")}
                            </TabsTrigger>
                        </TabsList>

                        <TabsContent value="tasks" className="animate-fade-in">
                            <Card className="border-none shadow-none bg-transparent">
                                <div className="flex items-center justify-between mb-6">
                                    <h2 className="text-2xl font-bold text-foreground flex items-center gap-2">
                                        <Target className="w-6 h-6 text-primary" />
                                        Task Board
                                    </h2>
                                    <Select value={filterStatus} onValueChange={setFilterStatus}>
                                        <SelectTrigger className="w-[180px] rounded-xl bg-card border-border/50">
                                            <SelectValue placeholder={t("childTasks.filter.all")} />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="ALL">{t("childTasks.filter.all")}</SelectItem>
                                            <SelectItem value="PENDING">{t("childTasks.filter.pending")}</SelectItem>
                                            <SelectItem value="PENDING_APPROVAL">{t("childTasks.filter.pendingApproval")}</SelectItem>
                                            <SelectItem value="APPROVED">{t("childTasks.filter.approved")}</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                {isLoadingTasks ? (
                                    <div className="grid place-items-center py-12">
                                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                                    </div>
                                ) : filteredTasks.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center py-16 bg-card rounded-3xl border-2 border-dashed border-border/50">
                                        <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4 text-muted-foreground">
                                            <Target className="w-8 h-8" />
                                        </div>
                                        <p className="text-lg text-muted-foreground font-medium">
                                            {t("childTasks.noTasks")}
                                        </p>
                                        <Button
                                            variant="link"
                                            onClick={() => setIsCreateTaskDialogOpen(true)}
                                            className="text-primary mt-2"
                                        >
                                            Create First Quest
                                        </Button>
                                    </div>
                                ) : (
                                    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
                                        {filteredTasks.map((task) => (
                                            <div
                                                key={task.id}
                                                className={`
                                                    group relative overflow-hidden bg-card p-5 rounded-2xl border transition-all duration-300 hover:scale-[1.02] cursor-pointer
                                                    ${task.status === 'PENDING_APPROVAL'
                                                        ? 'border-yellow-400 shadow-glow shadow-yellow-500/20'
                                                        : 'border-border/50 hover:border-primary/50 hover:shadow-soft'}
                                                `}
                                                onClick={() => handleReviewClick(task)}
                                            >
                                                <div className="absolute top-0 left-0 w-1 h-full bg-gradient-to-b from-primary to-purple-600 opacity-0 group-hover:opacity-100 transition-opacity" />

                                                <div className="flex justify-between items-start mb-4">
                                                    <Badge variant={getWeightVariant(task.weight)} className="rounded-lg px-2 py-0.5 text-xs font-semibold uppercase tracking-wider">
                                                        {task.weight} Quest
                                                    </Badge>
                                                    <div className={`h-2.5 w-2.5 rounded-full ${getStatusColor(task.status)} shadow-sm ring-2 ring-white dark:ring-gray-900`} />
                                                </div>

                                                <h3 className="font-bold text-lg mb-3 line-clamp-2 text-foreground group-hover:text-primary transition-colors">
                                                    {task.description}
                                                </h3>

                                                <div className="flex flex-col gap-2 text-sm text-muted-foreground">
                                                    <div className="flex items-center gap-2">
                                                        <Calendar className="w-4 h-4 text-primary/70" />
                                                        <span className="font-medium bg-blue-100 px-2 py-0.5 rounded text-xs text-blue-700 border border-blue-200/50">{task.type}</span>
                                                    </div>

                                                    {task.requiresProof && (
                                                        <div className="flex items-center gap-2 text-purple-600 dark:text-purple-400">
                                                            <CheckCircle2 className="w-4 h-4" />
                                                            <span className="text-xs font-medium">{t("childTasks.proofRequired")}</span>
                                                        </div>
                                                    )}

                                                    {task.aiValidated && (
                                                        <div className="flex items-center gap-2 text-green-600 dark:text-green-400">
                                                            <Sparkles className="w-4 h-4" />
                                                            <span className="text-xs font-medium">{t("childTasks.aiValidated")}</span>
                                                        </div>
                                                    )}
                                                </div>

                                                {task.createdAt && (
                                                    <div className="mt-4 pt-4 border-t border-border/30 flex items-center gap-1.5 text-xs text-muted-foreground/60">
                                                        <Clock className="w-3 h-3" />
                                                        {new Date(task.createdAt).toLocaleDateString()}
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </Card>
                        </TabsContent>

                        <TabsContent value="financial" className="animate-fade-in">
                            <div className="grid gap-6">
                                <Card className="border-none bg-gradient-to-br from-green-500/10 to-emerald-500/5 shadow-soft">
                                    <div className="flex flex-col md:flex-row items-center p-8 gap-8">
                                        <div className="p-6 bg-gradient-to-br from-green-400 to-emerald-600 rounded-3xl shadow-lg shadow-green-500/30 text-white">
                                            <Trophy className="w-12 h-12" />
                                        </div>
                                        <div className="text-center md:text-left flex-1">
                                            <p className="text-muted-foreground font-medium mb-1">{t("childTasks.financial.predictedAllowance")}</p>
                                            {isLoadingAllowance ? (
                                                <div className="h-12 w-48 bg-gray-200 animate-pulse rounded-lg" />
                                            ) : (
                                                <h3 className="text-5xl font-bold text-green-600 dark:text-green-400 font-heading tracking-tight">
                                                    {new Intl.NumberFormat('pt-BR', {
                                                        style: 'currency',
                                                        currency: 'BRL'
                                                    }).format(predictedAllowance)}
                                                </h3>
                                            )}
                                            <p className="text-sm text-muted-foreground mt-2 max-w-md">
                                                {t("childTasks.financial.estimatedValue")} • {t("childTasks.financial.thisMonth")}
                                            </p>
                                        </div>
                                    </div>
                                </Card>

                                <Card className="border-border/50 shadow-sm overflow-hidden rounded-2xl">
                                    <CardHeader className="bg-muted/30">
                                        <CardTitle className="flex items-center gap-2">
                                            <Clock className="w-5 h-5 text-gray-500" />
                                            Activity History
                                        </CardTitle>
                                    </CardHeader>
                                    <CardContent className="p-0">
                                        <FinancialLedger childId={childId!} parentId={localStorage.getItem("parentId") || ""} />
                                    </CardContent>
                                </Card>
                            </div>
                        </TabsContent>
                    </Tabs>
                </div>

                <DialogContent className="sm:max-w-xl">
                    <DialogHeader>
                        <DialogTitle>{t("childTasks.review.title")}</DialogTitle>
                        <DialogDescription className="text-lg font-medium text-foreground mt-2">
                            "{selectedTaskForReview?.description}"
                        </DialogDescription>
                    </DialogHeader>

                    <div className="py-4">
                        {selectedTaskForReview?.aiValidated && (
                            <div className="mb-6 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-900 rounded-xl flex items-start gap-3">
                                <Sparkles className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                                <div>
                                    <p className="font-semibold text-green-700 dark:text-green-400">AI Verified</p>
                                    <p className="text-sm text-green-600/80 dark:text-green-500">{t("childTasks.review.aiValidatedMessage")}</p>
                                </div>
                            </div>
                        )}

                        {selectedTaskForReview?.requiresProof ? (
                            <div className="space-y-3">
                                <Label className="text-base">{t("childTasks.review.proof")}</Label>
                                {selectedTaskForReview?.proofImageUrl ? (
                                    <div className="relative rounded-xl overflow-hidden border border-border shadow-sm group">
                                        <img
                                            src={selectedTaskForReview.proofImageUrl}
                                            alt="Task proof"
                                            className="w-full h-64 object-cover hover:scale-105 transition-transform duration-500"
                                        />
                                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors pointer-events-none" />
                                    </div>
                                ) : (
                                    <div className="w-full h-48 bg-muted/50 rounded-xl border-2 border-dashed border-muted flex flex-col items-center justify-center text-muted-foreground gap-2">
                                        <Clock className="w-8 h-8 opacity-50" />
                                        <span>{t("childTasks.review.noProof")}</span>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="p-4 bg-secondary/5 rounded-xl border border-secondary/20 text-center">
                                <p className="text-muted-foreground text-sm">No proof required for this quest.</p>
                            </div>
                        )}
                    </div>

                    <DialogFooter className="flex-col sm:flex-row gap-2 sm:gap-2 justify-between w-full">
                        <Button
                            variant="ghost"
                            onClick={() => setIsReviewDialogOpen(false)}
                            className="rounded-xl text-muted-foreground"
                        >
                            {t("common.close") || "Close"}
                        </Button>

                        <div className="flex gap-2 w-full sm:w-auto justify-end">
                            <Button
                                onClick={handleDeleteTask}
                                variant="outline"
                                className="rounded-xl border-destructive/50 text-destructive hover:bg-destructive/10 hover:text-destructive"
                            >
                                <Trash2 className="w-4 h-4 mr-2" />
                                {t("common.delete") || "Delete"}
                            </Button>

                            {selectedTaskForReview?.status === 'APPROVED' ? (
                                <Button
                                    onClick={handleRejectTask}
                                    variant="destructive"
                                    className="rounded-xl"
                                >
                                    <X className="w-4 h-4 mr-2" />
                                    {t("common.reject") || "Reject"}
                                </Button>
                            ) : selectedTaskForReview?.status === 'PENDING_APPROVAL' ? (
                                <>
                                    <Button
                                        onClick={handleRejectTask}
                                        variant="outline"
                                        className="rounded-xl border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                                    >
                                        <X className="w-4 h-4 mr-2" />
                                        {t("common.reject")}
                                    </Button>
                                    <Button
                                        onClick={handleApproveTask}
                                        className="bg-green-600 hover:bg-green-700 rounded-xl shadow-lg shadow-green-600/20"
                                    >
                                        <CheckCircle2 className="w-4 h-4 mr-2" />
                                        {t("dashboard.pendingApprovals.approve")}
                                    </Button>
                                </>
                            ) : null}
                        </div>
                    </DialogFooter>
                </DialogContent>

                <Dialog open={isCreateTaskDialogOpen} onOpenChange={setIsCreateTaskDialogOpen}>
                    <DialogContent className="sm:max-w-lg">
                        <DialogHeader>
                            <DialogTitle className="text-xl flex items-center gap-2">
                                <Plus className="w-5 h-5 text-primary" />
                                {t("childTasks.create.title")}
                            </DialogTitle>
                            <DialogDescription>
                                {t("childTasks.create.description", { name: child.name })}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-6 py-4">
                            <div className="space-y-2">
                                <Label htmlFor="desc">{t("childTasks.create.label.description")}</Label>
                                <Input
                                    id="desc"
                                    value={newTask.description}
                                    onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
                                    placeholder={t("childTasks.create.placeholder.description")}
                                    className="h-12 text-lg rounded-xl"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label>{t("childTasks.create.label.type")}</Label>
                                    <Select
                                        value={newTask.type}
                                        onValueChange={(value) => setNewTask({ ...newTask, type: value as any })}
                                    >
                                        <SelectTrigger className="h-10 rounded-xl">
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="ONE_TIME">{t("childTasks.create.type.oneTime")}</SelectItem>
                                            <SelectItem value="DAILY">{t("childTasks.create.type.daily")}</SelectItem>
                                            <SelectItem value="WEEKLY">{t("childTasks.create.type.weekly")}</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>
                                <div className="space-y-2">
                                    <Label>{t("childTasks.create.label.weight")}</Label>
                                    <Select
                                        value={newTask.weight}
                                        onValueChange={(value) => setNewTask({ ...newTask, weight: value as any })}
                                    >
                                        <SelectTrigger className="h-10 rounded-xl">
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="LOW">Easy (Low)</SelectItem>
                                            <SelectItem value="MEDIUM">Normal (Medium)</SelectItem>
                                            <SelectItem value="HIGH">Hard (High)</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>

                            <div className="flex items-center space-x-3 p-4 bg-muted/30 rounded-xl border border-border/50">
                                <Checkbox
                                    id="requiresProof"
                                    checked={newTask.requiresProof}
                                    onCheckedChange={(checked) =>
                                        setNewTask({ ...newTask, requiresProof: checked as boolean })
                                    }
                                    className="h-5 w-5 rounded-md"
                                />
                                <div className="grid gap-1.5 leading-none">
                                    <Label htmlFor="requiresProof" className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                                        {t("childTasks.create.requiresProof")}
                                    </Label>
                                    <p className="text-xs text-muted-foreground">
                                        Child must upload a photo to complete this quest.
                                    </p>
                                </div>
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="ghost" onClick={() => setIsCreateTaskDialogOpen(false)} className="rounded-xl">{t("common.cancel")}</Button>
                            <Button onClick={handleCreateTask} className="rounded-xl shadow-lg shadow-primary/20">{t("childTasks.create.submit")}</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>
        </Dialog >
    );
}
