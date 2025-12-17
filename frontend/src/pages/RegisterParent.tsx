import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { parentService } from "@/services/parentService";
import { useSubscription } from "@/contexts/SubscriptionContext";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { toast } from "sonner";
import { CreateParentRequest } from "@/types";

export default function RegisterParent() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { setUser } = useSubscription();
    const [isLoading, setIsLoading] = useState(false);

    const formSchema = z.object({
        name: z.string().min(2, t("validation.nameMin", { min: 2 })),
        email: z.string().email(t("validation.emailInvalid")),
        phoneNumber: z.string().min(8, "Phone number is required"),
        password: z.string().min(6, "Password must be at least 6 characters"),
        confirmPassword: z.string()
    }).refine((data) => data.password === data.confirmPassword, {
        message: "Passwords don't match",
        path: ["confirmPassword"],
    });

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            name: "",
            email: "",
            phoneNumber: "",
            password: "",
            confirmPassword: "",
        },
    });

    async function onSubmit(values: z.infer<typeof formSchema>) {
        setIsLoading(true);
        try {
            const parentData: CreateParentRequest = {
                name: values.name,
                email: values.email,
                phoneNumber: values.phoneNumber,
                password: values.password
            };
            const parent = await parentService.registerParent(parentData);
            toast.success(t("auth.register.success"));
            // Store parent data for demo purposes and sync with SubscriptionContext
            if (parent && parent.id) {
                localStorage.setItem("parentId", parent.id);
                localStorage.setItem("parentName", parent.name);
                localStorage.setItem("parent", JSON.stringify(parent));
                // Sync with SubscriptionContext
                // setUser(parent); // Remove auto-login context sync as we don't have token
            }
            navigate("/login");
        } catch (error) {
            toast.error(t("auth.register.error"));
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-background via-purple-50/50 to-blue-50/50 dark:via-purple-900/10 dark:to-blue-900/10 p-4 animate-fade-in">
            <div className="absolute inset-0 bg-grid-pattern opacity-[0.02] dark:opacity-[0.05] pointer-events-none" />
            <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-primary/5 dark:bg-primary/10 rounded-full blur-3xl -z-10 animate-pulse" />
            <div className="absolute bottom-0 left-0 w-[500px] h-[500px] bg-blue-500/5 dark:bg-blue-500/10 rounded-full blur-3xl -z-10 animate-pulse delay-700" />

            <Card className="w-full max-w-md border-border/50 shadow-glow relative overflow-hidden backdrop-blur-sm bg-card/90 dark:bg-card/80">
                <CardHeader className="text-center space-y-2 pb-6">
                    <div className="mx-auto w-12 h-12 bg-gradient-to-br from-primary to-purple-600 rounded-xl flex items-center justify-center shadow-lg shadow-primary/20 mb-4">
                        <span className="text-2xl font-bold text-white">F</span>
                    </div>
                    <CardTitle className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-600">
                        {t("auth.register.title")}
                    </CardTitle>
                    <p className="text-muted-foreground text-sm">
                        Create your account to start managing tasks
                    </p>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("auth.register.name")}</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder={t("auth.register.namePlaceholder")}
                                                {...field}
                                                className="bg-background/50 dark:bg-background/20"
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="email"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("auth.register.email")}</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder={t("auth.register.emailPlaceholder")}
                                                {...field}
                                                className="bg-background/50 dark:bg-background/20"
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="phoneNumber"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Phone Number</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="+55 ..."
                                                {...field}
                                                className="bg-background/50 dark:bg-background/20"
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="password"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Password</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="password"
                                                placeholder="******"
                                                {...field}
                                                className="bg-background/50 dark:bg-background/20"
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="confirmPassword"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Confirm Password</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="password"
                                                placeholder="******"
                                                {...field}
                                                className="bg-background/50 dark:bg-background/20"
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <Button
                                type="submit"
                                className="w-full"
                                disabled={isLoading}
                                data-testid="register-submit-button"
                            >
                                {isLoading ? t("auth.register.buttonLoading") : t("auth.register.button")}
                            </Button>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
