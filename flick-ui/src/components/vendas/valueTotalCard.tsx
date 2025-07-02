import { useTranslation } from "react-i18next";
import Card from "../ui/Card";
import { formatCurrency } from "../../utils/formatters";
import clsx from "clsx";

interface valueTotalCardProps {
    title?: string;
    description?: string;
    color?: 'green' | 'red' | 'blue' | 'yellow' | 'purple' | 'orange';
    value: number;
}

const colorClasses = {
    green: 'text-green-700 dark:text-green-400',
    red: 'text-red-700 dark:text-red-400',
    blue: 'text-blue-700 dark:text-blue-400',
    yellow: 'text-yellow-700 dark:text-yellow-400',
    purple: 'text-purple-700 dark:text-purple-400',
    orange: 'text-orange-700 dark:text-orange-400',
    default: 'text-brand-primary dark:text-brand-accent',
};

const ValueTotalCard: React.FC<valueTotalCardProps> = ({ value, title, description, color }) => {
    const { t } = useTranslation();
    const dynamicColorClass = color ? colorClasses[color] : colorClasses.default;
    return (
        <Card padding="md" className="bg-card-light dark:bg-card-dark">
            <div className="flex flex-col items-center justify-center text-center">
                <h4 className="text-sm font-medium text-text-secondary dark:text-gray-400 uppercase tracking-wider">
                    {title || t("vendas.grossTotal")}
                </h4>
                <p
                    className={clsx('text-3xl font-bold mt-1', dynamicColorClass)}
                >
                    {formatCurrency(value)}
                </p>
                <p className="text-xs text-text-secondary dark:text-gray-500 mt-1">
                    {description || t("vendas.grossTotalDescription")}
                </p>
            </div>
        </Card>
    )
}
export default ValueTotalCard;