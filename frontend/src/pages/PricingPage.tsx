
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { Check, Loader2, Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { subscriptionService } from "@/services/subscriptionService";
import { useSubscription } from "@/contexts/SubscriptionContext";
import { toast } from "sonner";

export default function PricingPage() {
    const { t } = useTranslation();
    const { isPremium } = useSubscription();
    const [isLoading, setIsLoading] = useState(false);

    const handleSubscribe = async () => {
        setIsLoading(true);
        try {
            const response = await subscriptionService.subscribe();
            if (response.checkoutUrl) {
                window.location.href = response.checkoutUrl;
            } else {
                toast.error("Failed to generate checkout link.");
            }
        } catch (error) {
            console.error(error);
            toast.error("An error occurred while starting the subscription.");
        } finally {
            setIsLoading(false);
        }
    };

    const features = [
        "Unlimited Tasks",
        "AI Task Suggestions",
        "AI Image Verification",
        "Gift Card Store Access",
        "Priority Support",
        "Add unlimited children"
    ];

    return (
        <div className="container mx-auto py-10 px-4">
            <div className="text-center mb-10">
                <h1 className="text-4xl font-bold mb-4">Upgrade to Premium</h1>
                <p className="text-xl text-muted-foreground">Unlock the full potential of TaskAndPay for your family.</p>
            </div>

            <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
                {/* Free Plan */}
                <Card className="border-2">
                    <CardHeader>
                        <CardTitle className="text-2xl">Free</CardTitle>
                        <CardDescription>Essential tools to get started</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="text-3xl font-bold">R$ 0<span className="text-sm font-normal text-muted-foreground">/mo</span></div>
                        <ul className="space-y-2">
                            <li className="flex items-center gap-2"><Check className="h-4 w-4 text-green-500" /> Up to 5 recurring tasks</li>
                            <li className="flex items-center gap-2"><Check className="h-4 w-4 text-green-500" /> 1 Child profile</li>
                            <li className="flex items-center gap-2"><Check className="h-4 w-4 text-green-500" /> Basic Allowance tracking</li>
                        </ul>
                    </CardContent>
                    <CardFooter>
                        <Button className="w-full" variant="outline" disabled>Current Plan</Button>
                    </CardFooter>
                </Card>

                {/* Premium Plan */}
                <Card className="border-2 border-primary shadow-lg relative overflow-hidden">
                    <div className="absolute top-0 right-0 bg-primary text-primary-foreground px-3 py-1 text-sm font-medium rounded-bl-lg">
                        Recommended
                    </div>
                    <CardHeader>
                        <CardTitle className="text-2xl flex items-center gap-2">
                            Premium <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                        </CardTitle>
                        <CardDescription>Supercharge your parenting with AI</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="text-3xl font-bold">R$ 19,90<span className="text-sm font-normal text-muted-foreground">/mo</span></div>
                        <ul className="space-y-2">
                            {features.map((feature, i) => (
                                <li key={i} className="flex items-center gap-2">
                                    <Check className="h-4 w-4 text-primary" /> {feature}
                                </li>
                            ))}
                        </ul>
                    </CardContent>
                    <CardFooter>
                        {isPremium() ? (
                            <Button className="w-full" variant="secondary" disabled>Active</Button>
                        ) : (
                            <Button className="w-full" onClick={handleSubscribe} disabled={isLoading}>
                                {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                                Upgrade Now
                            </Button>
                        )}
                    </CardFooter>
                </Card>
            </div>
        </div>
    );
}
