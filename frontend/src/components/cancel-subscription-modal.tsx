import { useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { Loader2, AlertTriangle } from "lucide-react";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { subscriptionService, CancellationReason } from "@/services/subscriptionService";

interface CancelSubscriptionModalProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    onSuccess: () => void;
}

export function CancelSubscriptionModal({ open, onOpenChange, onSuccess }: CancelSubscriptionModalProps) {
    const { t } = useTranslation();
    const [step, setStep] = useState<1 | 2>(1);
    const [reason, setReason] = useState<CancellationReason | "">("");
    const [details, setDetails] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleNext = () => {
        if (!reason) return;
        setStep(2);
    };

    const handleCancel = async () => {
        if (!reason) return;
        setIsLoading(true);
        try {
            await subscriptionService.cancelSubscription({
                reason: reason as CancellationReason,
                reasonDetails: reason === "OTHER" ? details : undefined,
            });
            toast.success(t("settings.subscription.cancelSuccess") || "Subscription successfully canceled");
            onSuccess();
            onOpenChange(false);
            setStep(1);
            setReason("");
            setDetails("");
        } catch (error) {
            console.error("Failed to cancel subscription", error);
            toast.error(t("settings.subscription.cancelError") || "Failed to cancel subscription. Try again later.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={(val) => {
            if (!val) setStep(1);
            onOpenChange(val);
        }}>
            <DialogContent className="sm:max-w-[500px]">
                {step === 1 ? (
                    <>
                        <DialogHeader>
                            <DialogTitle>{t("settings.subscription.cancelSurveyTitle") || "Why are you canceling?"}</DialogTitle>
                            <DialogDescription>
                                {t("settings.subscription.cancelSurveyDesc") || "Please let us know why you are leaving so we can improve."}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="py-4 space-y-4">
                            <RadioGroup value={reason} onValueChange={(val) => setReason(val as CancellationReason)}>
                                <div className="flex items-center space-x-2">
                                    <RadioGroupItem value="TOO_EXPENSIVE" id="r1" />
                                    <Label htmlFor="r1">{t("settings.subscription.reasons.TOO_EXPENSIVE") || "Too expensive"}</Label>
                                </div>
                                <div className="flex items-center space-x-2">
                                    <RadioGroupItem value="NOT_USING_FEATURES" id="r2" />
                                    <Label htmlFor="r2">{t("settings.subscription.reasons.NOT_USING_FEATURES") || "Not using Premium features"}</Label>
                                </div>
                                <div className="flex items-center space-x-2">
                                    <RadioGroupItem value="FOUND_ALTERNATIVE" id="r3" />
                                    <Label htmlFor="r3">{t("settings.subscription.reasons.FOUND_ALTERNATIVE") || "Found a better alternative"}</Label>
                                </div>
                                <div className="flex items-center space-x-2">
                                    <RadioGroupItem value="WILL_RETURN_LATER" id="r4" />
                                    <Label htmlFor="r4">{t("settings.subscription.reasons.WILL_RETURN_LATER") || "Will return later"}</Label>
                                </div>
                                <div className="flex items-center space-x-2">
                                    <RadioGroupItem value="OTHER" id="r5" />
                                    <Label htmlFor="r5">{t("settings.subscription.reasons.OTHER") || "Other"}</Label>
                                </div>
                            </RadioGroup>
                            {reason === "OTHER" && (
                                <Textarea 
                                    placeholder={t("settings.subscription.reasons.OTHER_details") || "Please elaborate..."} 
                                    maxLength={500}
                                    value={details}
                                    onChange={(e) => {
                                        const val = e.target.value;
                                        setDetails(val.slice(0, 500));
                                    }}
                                />
                            )}
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => onOpenChange(false)}>{t("common.cancel") || "Cancel"}</Button>
                            <Button onClick={handleNext} disabled={!reason}>{t("common.next") || "Next"}</Button>
                        </DialogFooter>
                    </>
                ) : (
                    <>
                        <DialogHeader>
                            <DialogTitle className="flex items-center text-destructive">
                                <AlertTriangle className="mr-2 h-5 w-5 text-amber-500" />
                                {t("settings.subscription.cancelConfirmTitle") || "Are you sure?"}
                            </DialogTitle>
                            <DialogDescription>
                                {t("settings.subscription.cancelConfirmDesc") || "If you cancel, you will lose access to the following at the end of your billing cycle:"}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="py-4 space-y-2 text-sm bg-amber-500/10 text-amber-700 p-4 rounded-md border border-amber-500/20">
                            <ul className="list-disc pl-5 space-y-2">
                                <li>{t("settings.subscription.impact.children") || "Child limit reduced from unlimited to 1"}</li>
                                <li>{t("settings.subscription.impact.tasks") || "Recurring task limit reduced from unlimited to 5"}</li>
                                <li>{t("settings.subscription.impact.ai") || "Loss of AI task suggestions"}</li>
                                <li>{t("settings.subscription.impact.giftcards") || "Loss of access to Gift Card store"}</li>
                            </ul>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" disabled={isLoading} onClick={() => setStep(1)}>{t("common.back") || "Back"}</Button>
                            <Button variant="destructive" onClick={handleCancel} disabled={isLoading}>
                                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                                {t("settings.subscription.confirmCancel") || "Confirm Cancellation"}
                            </Button>
                        </DialogFooter>
                    </>
                )}
            </DialogContent>
        </Dialog>
    );
}
