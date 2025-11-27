import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { childService } from "@/services/childService";
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
import { CreateChildRequest } from "@/types";

export default function AddChild() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const parentId = localStorage.getItem("parentId");

    const formSchema = z.object({
        name: z.string().min(2, t("validation.nameMin", { min: 2 })),
        phoneNumber: z.string().min(10, t("validation.phoneMin", { min: 10 })),
        age: z.number().min(1, t("validation.ageMin", { min: 1 })).max(18, t("validation.ageMax", { max: 18 })),
    });

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            name: "",
            phoneNumber: "",
            age: 10,
        },
    });

    async function onSubmit(values: z.infer<typeof formSchema>) {
        if (!parentId) {
            toast.error(t("child.add.errorParentNotFound"));
            navigate("/register");
            return;
        }

        setIsLoading(true);
        try {
            const childData: CreateChildRequest = {
                name: values.name,
                phoneNumber: values.phoneNumber,
                parentId: parentId,
                age: values.age,
            };
            const createdChild = await childService.addChild(childData, parentId);

            // Store child in localStorage
            const childrenData = localStorage.getItem("children");
            const children = childrenData ? JSON.parse(childrenData) : [];
            children.push({
                id: createdChild.id,
                name: values.name,
                age: values.age,
                phoneNumber: values.phoneNumber,
                parentId: parentId,
            });
            localStorage.setItem("children", JSON.stringify(children));

            toast.success(t("child.add.success"));
            navigate("/dashboard");
        } catch (error) {
            toast.error(t("child.add.error"));
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <CardTitle className="text-2xl text-center">{t("child.add.title")}</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <FormField
                                control={form.control}
                                name="name"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("child.add.name")}</FormLabel>
                                        <FormControl>
                                            <Input placeholder={t("child.add.namePlaceholder")} {...field} />
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
                                        <FormLabel>{t("child.add.phone")}</FormLabel>
                                        <FormControl>
                                            <Input placeholder={t("child.add.phonePlaceholder")} {...field} />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <FormField
                                control={form.control}
                                name="age"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>{t("child.add.age")}</FormLabel>
                                        <FormControl>
                                            <Input
                                                type="number"
                                                placeholder={t("child.add.agePlaceholder")}
                                                {...field}
                                                onChange={(e) => field.onChange(parseInt(e.target.value))}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                            <Button type="submit" className="w-full" disabled={isLoading}>
                                {isLoading ? t("child.add.buttonLoading") : t("child.add.button")}
                            </Button>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
