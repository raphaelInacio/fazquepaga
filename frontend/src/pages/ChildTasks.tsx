import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { taskService } from "@/services/taskService";
import { aiService } from "@/services/aiService";
import { childService } from "@/services/childService";
import { allowanceService } from "@/services/allowanceService";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
    FormDescription,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from "@/components/ui/card";
import { toast } from "sonner";
import { Task, CreateTaskRequest, ChildWithLocalData } from "@/types";
import { ArrowLeft, Plus, Sparkles, QrCode, Calendar } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";

const formSchema = z.object({
    description: z.string().min(3, "Description must be at least 3 characters"),
    type: z.enum(["DAILY", "WEEKLY", "ONE_TIME"]),
    weight: z.enum(["LOW", "MEDIUM", "HIGH"]),
    requiresProof: z.boolean().default(false),
    dayOfWeek: z.number().min(0).max(6).optional(),
    scheduledDate: z.string().optional(),
});

export default function ChildTasks() {
    const { t } = useTranslation();
    const { childId } = useParams<{ childId: string }>();
    const navigate = useNavigate();
    const [child, setChild] = useState<ChildWithLocalData | null>(null);
    const [tasks, setTasks] = useState<Task[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingTasks, setIsLoadingTasks] = useState(true);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [aiSuggestions, setAiSuggestions] = useState<string[]>([]);
    const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
    const [onboardingCode, setOnboardingCode] = useState<string | null>(null);
    const [filterStatus, setFilterStatus] = useState<string>("ALL");
    const [predictedAllowance, setPredictedAllowance] = useState<number>(0);
    const [isLoadingAllowance, setIsLoadingAllowance] = useState(false);

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            description: "",
            type: "DAILY",
            weight: "MEDIUM",
            requiresProof: false,
        },
    });

    useEffect(() => {
        if (!childId) {
            navigate("/dashboard");
            return;
        }

        // Load child from localStorage
        const childrenData = localStorage.getItem("children");
        if (childrenData) {
            const children: ChildWithLocalData[] = JSON.parse(childrenData);
            const foundChild = children.find((c) => c.id === childId);
            if (foundChild) {
                setChild(foundChild);
            } else {
                toast.error("Child not found");
                navigate("/dashboard");
                return;
            }
        }

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

    const loadAiSuggestions = async () => {
        if (!child?.age) {
            toast.error("Child age is required for AI suggestions");
            return;
        }

        setIsLoadingSuggestions(true);
        try {
            const suggestions = await aiService.getTaskSuggestions(child.age);
            setAiSuggestions(suggestions);
            toast.success("AI suggestions loaded!");
        } catch (error) {
            toast.error("Failed to load AI suggestions");
            console.error(error);
        } finally {
            setIsLoadingSuggestions(false);
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
    }, [childId, tasks]); // Reload when tasks change

    const generateOnboardingCode = async () => {
        if (!childId) return;

        try {
            const response = await childService.generateOnboardingCode(childId);
            setOnboardingCode(response.code || JSON.stringify(response));
            toast.success("Onboarding code generated!");
        } catch (error) {
            toast.error("Failed to generate onboarding code");
            console.error(error);
        }
    };

    const onSubmit = async (values: z.infer<typeof formSchema>) => {
        if (!childId) return;

        setIsLoading(true);
        try {
            const taskData: CreateTaskRequest = {
                description: values.description,
                type: values.type,
                weight: values.weight,
                requiresProof: values.requiresProof,
                dayOfWeek: values.dayOfWeek,
                scheduledDate: values.scheduledDate,
            };

            await taskService.createTask(childId, taskData);
            toast.success("Task created successfully!");
            form.reset();
            setShowCreateForm(false);
            loadTasks();
        } catch (error) {
            toast.error("Failed to create task");
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    };

    const useSuggestion = (suggestion: string) => {
        form.setValue("description", suggestion);
        setShowCreateForm(true);
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

    const filteredTasks = filterStatus === "ALL"
        ? tasks
        : tasks.filter(task => task.status === filterStatus);

    if (!child) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-6xl mx-auto space-y-6">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <Button variant="ghost" onClick={() => navigate("/dashboard")}>
                            <ArrowLeft className="h-4 w-4" />
                        </Button>
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">{child.name}'s Tasks</h1>
                            <p className="text-gray-500">Age: {child.age} years old</p>
                        </div>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="outline" onClick={generateOnboardingCode}>
                            <QrCode className="mr-2 h-4 w-4" />
                            Generate Code
                        </Button>
                        <Button onClick={() => setShowCreateForm(!showCreateForm)}>
                            <Plus className="mr-2 h-4 w-4" />
                            Create Task
                        </Button>
                    </div>
                </div>

                {/* Predicted Allowance & Onboarding Code */}
                <div className="grid gap-6 md:grid-cols-2">
                    {/* Predicted Allowance */}
                    <Card className="bg-green-50 border-green-200">
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2 text-green-700">
                                <span className="text-2xl">💰</span>
                                {t("tasks.allowance")}
                            </CardTitle>
                            <CardDescription className="text-green-600">
                                {t("tasks.monthlyOverview")}
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div>
                                <p className="text-sm text-green-600 font-medium">{t("tasks.predictedEarnings")}</p>
                                <div className="text-3xl font-bold text-green-700">
                                    {isLoadingAllowance ? (
                                        t("tasks.loading")
                                    ) : (
                                        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(predictedAllowance)
                                    )}
                                </div>
                            </div>

                            {child?.monthlyAllowance !== undefined && (
                                <div className="pt-4 border-t border-green-200">
                                    <p className="text-sm text-green-600 font-medium">{t("tasks.totalMonthlyAllowance")}</p>
                                    <div className="text-xl font-semibold text-green-800">
                                        {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(child.monthlyAllowance)}
                                    </div>
                                </div>
                            )}
                        </CardContent>
                    </Card>

                    {/* Onboarding Code Display */}
                    {onboardingCode ? (
                        <Card className="bg-blue-50 border-blue-200">
                            <CardContent className="pt-6">
                                <div className="text-center">
                                    <p className="text-sm text-gray-600 mb-2">Onboarding Code:</p>
                                    <p className="text-2xl font-mono font-bold text-blue-600">{onboardingCode}</p>
                                </div>
                            </CardContent>
                        </Card>
                    ) : (
                        <Card className="bg-gray-50 border-gray-200 flex items-center justify-center p-6">
                            <Button variant="outline" onClick={generateOnboardingCode}>
                                <QrCode className="mr-2 h-4 w-4" />
                                Generate Onboarding Code
                            </Button>
                        </Card>
                    )}
                </div>

                {/* AI Suggestions */}
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Sparkles className="h-5 w-5 text-purple-500" />
                            AI Task Suggestions
                        </CardTitle>
                        <CardDescription>
                            Get age-appropriate task suggestions powered by AI
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <Button
                                onClick={loadAiSuggestions}
                                disabled={isLoadingSuggestions}
                                variant="outline"
                            >
                                {isLoadingSuggestions ? "Loading..." : "Get Suggestions"}
                            </Button>
                            {aiSuggestions.length > 0 && (
                                <div className="grid gap-2">
                                    {aiSuggestions.map((suggestion, index) => (
                                        <div
                                            key={index}
                                            className="flex items-center justify-between p-3 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors"
                                        >
                                            <span className="text-sm">{suggestion}</span>
                                            <Button
                                                size="sm"
                                                onClick={() => useSuggestion(suggestion)}
                                            >
                                                Use
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </CardContent>
                </Card>

                {/* Create Task Form */}
                {
                    showCreateForm && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Create New Task</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <Form {...form}>
                                    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                                        <FormField
                                            control={form.control}
                                            name="description"
                                            render={({ field }) => (
                                                <FormItem>
                                                    <FormLabel>Description</FormLabel>
                                                    <FormControl>
                                                        <Input placeholder="Clean your room" {...field} />
                                                    </FormControl>
                                                    <FormMessage />
                                                </FormItem>
                                            )}
                                        />

                                        <div className="grid grid-cols-2 gap-4">
                                            <FormField
                                                control={form.control}
                                                name="type"
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>Type</FormLabel>
                                                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                            <FormControl>
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder="Select type" />
                                                                </SelectTrigger>
                                                            </FormControl>
                                                            <SelectContent>
                                                                <SelectItem value="DAILY">Daily</SelectItem>
                                                                <SelectItem value="WEEKLY">Weekly</SelectItem>
                                                                <SelectItem value="ONE_TIME">One Time</SelectItem>
                                                            </SelectContent>
                                                        </Select>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />

                                            <FormField
                                                control={form.control}
                                                name="weight"
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>Weight</FormLabel>
                                                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                                                            <FormControl>
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder="Select weight" />
                                                                </SelectTrigger>
                                                            </FormControl>
                                                            <SelectContent>
                                                                <SelectItem value="LOW">Low</SelectItem>
                                                                <SelectItem value="MEDIUM">Medium</SelectItem>
                                                                <SelectItem value="HIGH">High</SelectItem>
                                                            </SelectContent>
                                                        </Select>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />
                                        </div>

                                        <FormField
                                            control={form.control}
                                            name="requiresProof"
                                            render={({ field }) => (
                                                <FormItem className="flex flex-row items-start space-x-3 space-y-0">
                                                    <FormControl>
                                                        <Checkbox
                                                            checked={field.value}
                                                            onCheckedChange={field.onChange}
                                                        />
                                                    </FormControl>
                                                    <div className="space-y-1 leading-none">
                                                        <FormLabel>Requires Proof</FormLabel>
                                                        <FormDescription>
                                                            Child must provide photo evidence
                                                        </FormDescription>
                                                    </div>
                                                </FormItem>
                                            )}
                                        />

                                        {form.watch("type") === "WEEKLY" && (
                                            <FormField
                                                control={form.control}
                                                name="dayOfWeek"
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>Day of Week</FormLabel>
                                                        <Select
                                                            onValueChange={(value) => field.onChange(parseInt(value))}
                                                            defaultValue={field.value?.toString()}
                                                        >
                                                            <FormControl>
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder="Select day" />
                                                                </SelectTrigger>
                                                            </FormControl>
                                                            <SelectContent>
                                                                <SelectItem value="0">Sunday</SelectItem>
                                                                <SelectItem value="1">Monday</SelectItem>
                                                                <SelectItem value="2">Tuesday</SelectItem>
                                                                <SelectItem value="3">Wednesday</SelectItem>
                                                                <SelectItem value="4">Thursday</SelectItem>
                                                                <SelectItem value="5">Friday</SelectItem>
                                                                <SelectItem value="6">Saturday</SelectItem>
                                                            </SelectContent>
                                                        </Select>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />
                                        )}

                                        {form.watch("type") === "ONE_TIME" && (
                                            <FormField
                                                control={form.control}
                                                name="scheduledDate"
                                                render={({ field }) => (
                                                    <FormItem>
                                                        <FormLabel>Scheduled Date</FormLabel>
                                                        <FormControl>
                                                            <Input type="datetime-local" {...field} />
                                                        </FormControl>
                                                        <FormMessage />
                                                    </FormItem>
                                                )}
                                            />
                                        )}

                                        <div className="flex gap-2">
                                            <Button type="submit" disabled={isLoading}>
                                                {isLoading ? "Creating..." : "Create Task"}
                                            </Button>
                                            <Button
                                                type="button"
                                                variant="outline"
                                                onClick={() => setShowCreateForm(false)}
                                            >
                                                Cancel
                                            </Button>
                                        </div>
                                    </form>
                                </Form>
                            </CardContent>
                        </Card>
                    )
                }

                {/* Task List */}
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
                                    <SelectItem value="COMPLETED">Completed</SelectItem>
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
                                        className="p-4 border rounded-lg hover:shadow-md transition-shadow"
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
            </div >
        </div >
    );
}
