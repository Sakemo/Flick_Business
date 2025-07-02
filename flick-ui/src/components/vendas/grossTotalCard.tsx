import { useTranslation } from "react-i18next";
import Card from "../ui/Card";
import { formatCurrency } from "../../utils/formatters";

interface GrossTotalCardProps {
    value: number;
}

const GrossTotalCard: React.FC<GrossTotalCardProps> = ({ value }) => {
    const { t } = useTranslation();
    return (
        <div className="mb-6">
            <Card padding="md" className="bg-card-light dark:bg-card-dark">
                <div className="flex flex-col items-center justify-center text-center">
                    <h4 className="text-sm font-medium text-text-secondary dark:text-gray-400 uppercase tracking-wider">
                        {t("vendas.grossTotal")}
                    </h4>
                    <p className="text-3xl font-bold text-brand-primary dark:text-brand-accent mt-1">
                        {formatCurrency(value)}
                    </p>
                    <p className="text-xs text-text-secondary dark:text-gray-500 mt-1">
                        {t("vendas.grossTotalDescription")}
                    </p>
                </div>
            </Card>
        </div>
    )
}
export default GrossTotalCard;