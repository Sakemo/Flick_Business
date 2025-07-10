// Arquivo: src/components/vendas/TotalPorPagamentoCard.tsx

import { useTranslation } from "react-i18next";
import { FormaPagamento } from "../../types/domain";
import { TotalPorPagamento } from "../../services/vendaService";
import { formatCurrency } from "../../utils/formatters";
import Card from "../ui/Card";
import Select from "../ui/Select";
import { useMemo, useState } from "react";

interface TotalPorPagamentoCardProps {
  totais: TotalPorPagamento[];
}

const TotalPorPagamentoCard: React.FC<TotalPorPagamentoCardProps> = ({ totais }) => {
  const { t } = useTranslation();
  const [formaSelecionada, setFormaSelecionada] = useState<FormaPagamento | ''>('');

  const valorExibido = useMemo(() => {
    if (formaSelecionada === '') return 0;
    const totalEncontrado = totais.find(t => t.formaPagamento === formaSelecionada);
    return totalEncontrado?.total ?? 0;
  }, [formaSelecionada, totais]);

  return (
    <Card padding="md" className="bg-card-light dark:bg-card-dark flex-1">
      <div className="flex flex-col text-center">
        <div className="mb-2">
          <Select
            value={formaSelecionada}
            onChange={(e) => setFormaSelecionada(e.target.value as FormaPagamento | '')}
            className="text-sm"
          >
            <option value="">{t('vendas.selectPaymentMethod', 'Selecione um pagamento')}</option>
            {Object.values(FormaPagamento).map(fp => (
              <option key={fp} value={fp}>
                {t(`vendas.paymentMethods.${fp.toLowerCase()}`, fp.replace('_', ' '))}
              </option>
            ))}
          </Select>
        </div>
        <p className="text-3xl font-bold text-yellow-500 dark:text-yellow-400 mt-1">
          {formatCurrency(valorExibido)}
        </p>
        <p className="text-xs text-text-secondary dark:text-gray-500 mt-1">
          {t('vendas.totalForSelectedMethod', 'Total para a forma selecionada')}
        </p>
      </div>
    </Card>
  );
};

export default TotalPorPagamentoCard;