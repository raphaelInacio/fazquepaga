
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { useTranslation } from "react-i18next";

interface AiContextInputProps {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    label?: string;
    helperText?: string;
}

export function AiContextInput({ value, onChange, placeholder, label, helperText }: AiContextInputProps) {
    const { t } = useTranslation();

    return (
        <div className="space-y-2">
            <Label>{label || t("child.aiContext.label") || "Bio / Interesses (IA)"}</Label>
            <Textarea
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder || t("child.aiContext.placeholder") || "Ex: Gosta de dinossauros, tem medo de escuro. Isso ajuda a IA a sugerir tarefas melhores."}
                className="min-h-[100px] resize-none"
            />
            {helperText && (
                <p className="text-sm text-muted-foreground">
                    {helperText}
                </p>
            )}
        </div>
    );
}
