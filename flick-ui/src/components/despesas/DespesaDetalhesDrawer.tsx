import { useTranslation } from "react-i18next";
import { DespesaResponse } from "../../types/domain";
import Card from "../ui/Card";
import { LuPencil, LuX } from "react-icons/lu";
import Button from "../ui/Button";
import { formatCurrency, formatVendaDate } from "../../utils/formatters";
import Badge from "../ui/Badge";

interface DespesaDetalhesDrawerProps {
    despesa: DespesaResponse;
    onClose: () => void;
    onEdit: (despesa: DespesaResponse) => void;
}

const DetailRow: React.FC<{ label: string; value?: React.ReactNode }> = ({
    label, value
}) => (
    <div className="py-2 sm:grid sm:grid-cols-3 sm:gap-4">
        <dt className="text-sm font-medium text-text-secondary dark:text-gray-400">
            {label}
        </dt>
        <dd className="mt-1 text-sm text-text-primary dark:text-white sm:mt-0 sm:col-span-2">
            {value ?? 'N/A'}
        </dd>
    </div>
);

const DespesaDetalhesDrawer: React.FC<DespesaDetalhesDrawerProps> = ({
    despesa, onClose, onEdit
}) => {
    const { t } = useTranslation();

    return (
        <Card className="h-full flex flex-col" padding="none">
            <div className="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-semibold">
                    {t('common.details')}
                </h2>
                <div className="flex items-center space-x-2">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => onEdit(despesa)}
                        title={t('userActions.edit')}
                    >
                        <LuPencil className="h-5 w-5" />
                    </Button>
                    <Button variant="ghost" size="icon" onClick={onClose} title={t('userActions.close')}>
                        <LuX className="h-5 w-5" />
                    </Button>
                </div>
            </div>

            <div className="p-6 flex-grow overflow-y-auto space-y-4">
                <dl className="divide-y divide-gray-200 dark:divide-gray-700">
                    <DetailRow label={t('common.name')} value={despesa.nome} />
                    <DetailRow label={t('common.value')} value={formatCurrency(despesa.valor)} />
                    <DetailRow label={t('common.category')} value={
                        <Badge colorScheme="blue">
                            {t(`expenseCategories.${despesa.tipoDespesa}`)}
                        </Badge>
                    }
                    />
                    <DetailRow label={t('despesas.form.expenseDate')} value={formatVendaDate(despesa.dataDespesa, true)} />
                    {despesa.observacao && (
             <DetailRow label={t('vendas.form.observations')} value={
                <p className="whitespace-pre-wrap">{despesa.observacao}</p>
             }/>
            )}
            <DetailRow label={t('produtos.createdAt')} value={formatVendaDate(despesa.dataCriacao, true)} />
            <DetailRow label={t('produtos.lastUpdate')} value={formatVendaDate(despesa.dataAtualizacao, true)} />
            <DetailRow label={t('common.description')} value={despesa.observacao} />
            </dl>
            </div>
        </Card>
    )
};

export default DespesaDetalhesDrawer;