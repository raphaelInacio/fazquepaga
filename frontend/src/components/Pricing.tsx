import { Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export const Pricing = () => {
    const navigate = useNavigate();

    return (
        <section id="pricing" className="py-20 bg-gradient-to-b from-white to-gray-50">
            <div className="container mx-auto px-4">
                <div className="text-center mb-16">
                    <h2 className="text-4xl font-bold mb-4">
                        Escolha o Plano Ideal para Sua Família
                    </h2>
                    <p className="text-xl text-gray-600">
                        Comece grátis e faça upgrade quando precisar de mais recursos
                    </p>
                </div>

                <div className="grid md:grid-cols-2 gap-8 max-w-5xl mx-auto">
                    {/* Free Plan */}
                    <div className="bg-white rounded-2xl shadow-lg p-8 border-2 border-gray-200 hover:border-gray-300 transition-all">
                        <div className="mb-6">
                            <h3 className="text-2xl font-bold mb-2">Básico</h3>
                            <div className="flex items-baseline gap-2 mb-4">
                                <span className="text-5xl font-bold">Grátis</span>
                            </div>
                            <p className="text-gray-600">Para começar a organizar as tarefas</p>
                        </div>

                        <ul className="space-y-4 mb-8">
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-500 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-700">Até 2 filhos</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-500 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-700">Tarefas limitadas</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-500 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-700">Validação básica de tarefas</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-500 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-700">Painel dos pais</span>
                            </li>
                        </ul>

                        <Button
                            onClick={() => navigate("/register")}
                            className="w-full bg-gradient-to-r from-orange-400 to-orange-500 hover:from-orange-500 hover:to-orange-600 text-white py-6 text-lg font-semibold"
                            size="lg"
                        >
                            Começar Grátis
                        </Button>
                    </div>

                    {/* Pro Plan */}
                    <div className="bg-gradient-to-br from-teal-50 to-cyan-50 rounded-2xl shadow-xl p-8 border-2 border-teal-400 relative hover:shadow-2xl transition-all">
                        <div className="absolute -top-4 right-8">
                            <span className="bg-gradient-to-r from-teal-500 to-cyan-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
                                MAIS POPULAR
                            </span>
                        </div>

                        <div className="mb-6">
                            <h3 className="text-2xl font-bold mb-2">Pro</h3>
                            <div className="flex items-baseline gap-2 mb-1">
                                <span className="text-5xl font-bold">R$ 29,90</span>
                                <span className="text-gray-600">/mês</span>
                            </div>
                            <p className="text-gray-700 font-medium">Para famílias que querem automação total</p>
                        </div>

                        <ul className="space-y-4 mb-8">
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Filhos ilimitados</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Validação com IA avançada</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Integração total com WhatsApp</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Relatórios de desempenho</span>
                            </li>
                            <li className="flex items-start gap-3">
                                <Check className="w-5 h-5 text-teal-600 flex-shrink-0 mt-0.5" />
                                <span className="text-gray-800 font-medium">Sugestões de tarefas por IA</span>
                            </li>
                        </ul>

                        <Button
                            onClick={() => {
                                // TODO: Navigate to checkout/upgrade page
                                alert("Funcionalidade de upgrade em desenvolvimento!");
                            }}
                            className="w-full bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white py-6 text-lg font-semibold"
                            size="lg"
                        >
                            Assinar Pro
                        </Button>
                    </div>
                </div>

                <div className="text-center mt-12">
                    <p className="text-gray-600">
                        Todos os planos incluem suporte por email • Cancele a qualquer momento
                    </p>
                </div>
            </div>
        </section>
    );
};
