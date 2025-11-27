import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from "@/components/ui/card";
import { toast } from "sonner";
import { Task, User } from "@/types";
import { ArrowLeft, Plus, Sparkles, QrCode, Calendar } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, DialogTrigger } from "@/components/ui/dialog";
import { FinancialLedger } from "@/components/FinancialLedger";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { childService } from "@/services/childService";
import { taskService } from "@/services/taskService";
import { allowanceService } from "@/services/allowanceService";
import api from "@/lib/api";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";

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
        weight: "MEDIUM",
        value: 0
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
            setIsReviewDialogOpen(false);
            setSelectedTaskForReview(null);
            loadTasks();
        } catch (error) {
            toast.error("Failed to approve task");
            console.error(error);
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
        // Pre-fill dialog with AI suggestion and open it for customization
        setNewTask({
            description: suggestion,
            type: "ONE_TIME",
            requiresProof: false,
            weight: "MEDIUM",
            value: 0
        });
        setIsCreateTaskDialogOpen(true);
        // Remove suggestion from list after user clicks to add it
        setAiSuggestions(prev => prev.filter(s => s !== suggestion));
    };

    const getStatusColor = (status?: string) => {
        switch (status) {
            case "COMPLETED":
                return "bg-green-500";
            case "PENDING_APPROVAL":
                return "bg-yellow-500";
            case "APPROVED":
                return "bg-blue-500";
            default:
                return "bg-gray-500";
        }
    };

    const getWeightColor = (weight: string) => {
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
            return task.status !== 'COMPLETED';
        }
        return task.status === filterStatus;
    });

    if (!child) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    return (
        <Dialog open={isReviewDialogOpen} onOpenChange={setIsReviewDialogOpen}>
            <div className="min-h-screen bg-gray-50 p-8">
                <div className="max-w-6xl mx-auto space-y-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <Button variant="ghost" onClick={() => navigate("/dashboard")}>
                                <ArrowLeft className="h-4 w-4" />
                            </Button>
                            <div>
                                <h1 className="text-3xl font-bold text-gray-900">{child.name}'s Tasks</h1>
                                <p className="text-gray-500">Age: {child.age || "N/A"} years old</p>
                            </div>
                        </div>
                        <div className="flex gap-2">
                            <Button
                                onClick={handleGetAISuggestions}
                                disabled={isLoadingAI}
                                className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white shadow-lg"
                            >
                                <Sparkles className="h-4 w-4 mr-2" />
                                {isLoadingAI ? "Generating..." : "✨ Gerar Tarefas com IA"}
                            </Button>
                            <Button variant="outline" onClick={() => setIsCreateTaskDialogOpen(true)} data-testid="create-task-button">
                                <Plus className="h-4 w-4 mr-2" />
                                Criar Tarefa Manual
                            </Button>
                        </div>
                    </div>

                    {showAISuggestions && aiSuggestions.length > 0 && (
                        <Card className="border-2 border-purple-200 bg-gradient-to-br from-purple-50 to-blue-50">
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Sparkles className="h-5 w-5 text-purple-600" />
                                    AI Task Suggestions
                                </CardTitle>
                                <CardDescription>
                                    Click "Adicionar" to create any of these age-appropriate tasks
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="grid gap-3">
                                    {aiSuggestions.map((suggestion, index) => (
                                        <div
                                            key={index}
                                            className="flex items-center justify-between p-4 bg-white rounded-lg border border-purple-100 hover:border-purple-300 transition-colors"
                                        >
                                            <span className="text-gray-700">{suggestion}</span>
                                            <Button
                                                onClick={() => handleAddSuggestionAsTask(suggestion)}
                                                size="sm"
                                                className="bg-purple-600 hover:bg-purple-700"
                                            >
                                                ➕ Adicionar
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    <Tabs defaultValue="tasks" className="w-full">
                        <TabsList className="grid w-full grid-cols-2">
                            <TabsTrigger value="tasks">Tasks</TabsTrigger>
                            <TabsTrigger value="financial">Financial</TabsTrigger>
                        </TabsList>
                        <TabsContent value="tasks">
                            <Card>
                                <CardHeader>
                                    <div className="flex items-center justify-between">
                                        <CardTitle>Tasks</CardTitle>
                                        <Select value={filterStatus} onValueChange={setFilterStatus}>
                                            <SelectTrigger className="w-[180px]">
                                                <SelectValue placeholder="Filter by status" />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="ALL">All Tasks</SelectItem>
                                                <SelectItem value="PENDING">Pending</SelectItem>
                                                <SelectItem value="PENDING_APPROVAL">Pending Approval</SelectItem>
                                                <SelectItem value="APPROVED">Approved</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    {isLoadingTasks ? (
                                        <div className="text-center py-8 text-gray-500">Loading tasks...</div>
                                    ) : filteredTasks.length === 0 ? (
                                        <div className="text-center py-8 text-gray-500">
                                            No tasks found. Create your first task!
                                        </div>
                                    ) : (
                                        <div className="space-y-4">
                                            {filteredTasks.map((task) => (
                                                <div
                                                    key={task.id}
                                                    className={`p-4 border rounded-lg hover:shadow-md transition-shadow cursor-pointer ${task.status === 'PENDING_APPROVAL' ? 'border-yellow-400 bg-yellow-50' : ''
                                                        }`}
                                                    onClick={() => handleReviewClick(task)}
                                                >
                                                    <div className="flex items-start justify-between">
                                                        <div className="flex-1">
                                                            <div className="flex items-center gap-2 mb-2">
                                                                <h3 className="font-semibold">{task.description}</h3>
                                                                <Badge variant={getWeightColor(task.weight)}>
                                                                    {task.weight}
                                                                </Badge>
                                                            </div>
                                                            <div className="flex items-center gap-4 text-sm text-gray-600">
                                                                <span className="flex items-center gap-1">
                                                                    <Calendar className="h-3 w-3" />
                                                                    {task.type}
                                                                </span>
                                                                {task.requiresProof && (
                                                                    <span className="text-purple-600">📸 Proof Required</span>
                                                                )}
                                                                {task.aiValidated && (
                                                                    <span className="text-green-600">✓ AI Validated</span>
                                                                )}
                                                            </div>
                                                        </div>
                                                        <div className="flex flex-col items-end gap-2">
                                                            <div className={`px-3 py-1 rounded-full text-white text-xs ${getStatusColor(task.status)}`}>
                                                                {task.status || "PENDING"}
                                                            </div>
                                                            {task.createdAt && (
                                                                <span className="text-xs text-gray-400">
                                                                    {new Date(task.createdAt).toLocaleDateString()}
                                                                </span>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </CardContent>
                            </Card>
                        </TabsContent>
                        <TabsContent value="financial">
                            <Card className="mb-6">
                                <CardHeader>
                                    <CardTitle>Mesada Prevista</CardTitle>
                                    <CardDescription>
                                        Valor estimado baseado nas tarefas pendentes e aprovadas
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    {isLoadingAllowance ? (
                                        <div className="text-center py-4">
                                            <span className="text-gray-500">Calculando...</span>
                                        </div>
                                    ) : (
                                        <div className="text-center">
                                            <div className="text-4xl font-bold text-green-600">
                                                {new Intl.NumberFormat('pt-BR', {
                                                    style: 'currency',
                                                    currency: 'BRL'
                                                }).format(predictedAllowance)}
                                            </div>
                                            <p className="text-sm text-gray-500 mt-2">
                                                Este mês
                                            </p>
                                        </div>
                                    )}
                                </CardContent>
                            </Card>
                            <FinancialLedger childId={childId!} parentId={localStorage.getItem("parentId") || ""} />
                        </TabsContent>
                    </Tabs>
                </div>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Review Task</DialogTitle>
                        <DialogDescription>{selectedTaskForReview?.description}</DialogDescription>
                    </DialogHeader>
                    {selectedTaskForReview?.aiValidated && (
                        <div className="my-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">
                            <p>✅ Our AI has pre-validated this task and believes it has been completed.</p>
                        </div>
                    )}
                    {selectedTaskForReview?.requiresProof && (
                        <div className="my-4">
                            <p className="font-semibold mb-2">Proof:</p>
                            {selectedTaskForReview?.proofImageUrl ? (
                                <img
                                    src={selectedTaskForReview.proofImageUrl}
                                    alt="Task proof"
                                    className="w-full max-h-96 object-contain rounded-md border border-gray-300"
                                />
                            ) : (
                                <div className="w-full h-48 bg-gray-200 rounded-md flex items-center justify-center">
                                    <span className="text-gray-500">No proof image submitted yet</span>
                                </div>
                            )}
                        </div>
                    )}
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsReviewDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleApproveTask}>Approve</Button>
                    </DialogFooter>
                </DialogContent>
            </div>
            <Dialog open={isCreateTaskDialogOpen} onOpenChange={setIsCreateTaskDialogOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Create New Task</DialogTitle>
                        <DialogDescription>Add a new task for {child.name}</DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label>Description</Label>
                            <Input
                                name="description"
                                value={newTask.description}
                                onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
                                placeholder="e.g. Clean room"
                            />
                        </div>
                        <div className="space-y-2">
                            <Label>Type</Label>
                            <Select
                                value={newTask.type}
                                onValueChange={(value) => setNewTask({ ...newTask, type: value as any })}
                            >
                                <SelectTrigger data-testid="task-type-select">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="ONE_TIME">One Time</SelectItem>
                                    <SelectItem value="DAILY">Daily</SelectItem>
                                    <SelectItem value="WEEKLY">Weekly</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="space-y-2">
                            <Label>Weight</Label>
                            <Select
                                value={newTask.weight}
                                onValueChange={(value) => setNewTask({ ...newTask, weight: value as any })}
                            >
                                <SelectTrigger data-testid="task-weight-select">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="LOW">Low</SelectItem>
                                    <SelectItem value="MEDIUM">Medium</SelectItem>
                                    <SelectItem value="HIGH">High</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="space-y-2">
                            <Label>Valor (R$)</Label>
                            <Input
                                name="value"
                                type="number"
                                step="0.01"
                                min="0"
                                value={newTask.value || 0}
                                onChange={(e) => setNewTask({ ...newTask, value: parseFloat(e.target.value) || 0 })}
                                placeholder="ex: 10.00"
                            />
                        </div>
                        <div className="flex items-center space-x-2">
                            <Checkbox
                                id="requiresProof"
                                checked={newTask.requiresProof}
                                onCheckedChange={(checked) =>
                                    setNewTask({ ...newTask, requiresProof: checked as boolean })
                                }
                            />
                            <Label htmlFor="requiresProof">Requires Proof?</Label>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setIsCreateTaskDialogOpen(false)}>Cancel</Button>
                        <Button onClick={handleCreateTask} data-testid="create-task-submit-button">Create Task</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </Dialog >
    );
}
