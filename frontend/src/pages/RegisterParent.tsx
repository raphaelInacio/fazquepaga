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
    });

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            name: "",
            email: "",
        },
    });

    async function onSubmit(values: z.infer<typeof formSchema>) {
        setIsLoading(true);
        try {
            const parentData: CreateParentRequest = {
                name: values.name,
                email: values.email,
            };
            const parent = await parentService.registerParent(parentData);
            toast.success(t("auth.register.success"));
            // Store parent data for demo purposes and sync with SubscriptionContext
            if (parent && parent.id) {
                localStorage.setItem("parentId", parent.id);
                localStorage.setItem("parentName", parent.name);
                localStorage.setItem("parent", JSON.stringify(parent));
                // Sync with SubscriptionContext
                setUser(parent);
            }
            navigate("/dashboard");
        } catch (error) {
            toast.error(t("auth.register.error"));
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <CardTitle className="text-2xl text-center">{t("auth.register.title")}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("auth.register.name")}</FormLabel>
                                        <FormControl>
                                            <Input placeholder={t("auth.register.namePlaceholder")} {...field} />
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
                                            <Input placeholder={t("auth.register.emailPlaceholder")} {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <Button type="submit" className="w-full" disabled={isLoading}>
                                {isLoading ? t("auth.register.buttonLoading") : t("auth.register.button")}
                            </Button>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
