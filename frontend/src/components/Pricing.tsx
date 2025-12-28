import { Check, Gift } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export const Pricing = () => {
    const navigate = useNavigate();

    return (
        <section id="pricing" className="py-20 bg-gradient-to-b from-white to-gray-50">
            <div className="container mx-auto px-4">
                <div className="text-center mb-16">
                    <h2 className="text-4xl font-bold mb-4">
                        Comece Grátis por 3 Dias
                    </h2>
                    <p className="text-xl text-gray-600">
                        Experimente todas as funcionalidades sem compromisso
                    </p>
                </div>

                {/* Single Plan - Free Trial + Premium */}
                <div className="max-w-lg mx-auto">
                    <div className="bg-gradient-to-br from-purple-50 to-indigo-50 rounded-2xl shadow-xl p-8 border-2 border-purple-400 relative hover:shadow-2xl transition-all">
                        <div className="absolute -top-4 right-8">
                            <span className="bg-gradient-to-r from-purple-500 to-indigo-500 text-white px-4 py-1 rounded-full text-sm font-semibold flex items-center gap-1">
                                <Gift className="w-4 h-4" /> OFERTA BETA TESTERS
                            </span>
                        </div>

                        <div className="mb-6 text-center">
                            <h3 className="text-2xl font-bold mb-2">Plano Premium</h3>
                            <div className="flex items-center justify-center gap-2 mb-2">
                                <span className="text-gray-400 line-through text-2xl">R$ 29,90</span>
                                <span className="text-5xl font-bold text-purple-600">R$ 9,90</span>
                                <span className="text-gray-600">/mês</span>
                            </div>
                            <p className="text-purple-600 font-semibold">🎁 3 dias grátis para experimentar!</p>
                        </div>

                        <ul className="space-y-4 mb-8">
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Tarefas ilimitadas</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Filhos ilimitados</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Sugestões de tarefas por IA</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Validação com IA avançada</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Loja de Gift Cards</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Integração com WhatsApp</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Relatórios financeiros</span>
                            </li>
                        </ul>

                        <Button
                            onClick={() => navigate("/register")}
                            className="w-full bg-gradient-to-r from-purple-500 to-indigo-500 hover:from-purple-600 hover:to-indigo-600 text-white py-6 text-lg font-semibold"
                            size="lg"
                        >
                            Começar Trial Grátis
                        </Button>

                        <p className="text-center text-gray-500 text-sm mt-4">
                            Sem cartão de crédito • Cancele quando quiser
                        </p>
                    </div>
                </div>

                <div className="text-center mt-12">
                    <p className="text-gray-600">
                        Após o trial, apenas R$ 9,90/mês • Cancele a qualquer momento
                    </p>
                </div>
            </div>
        </section>
    );
};
