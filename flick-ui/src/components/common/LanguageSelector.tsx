import { useTranslation } from "react-i18next";

const LanguageSelector: React.FC = () => {
    const { i18n } = useTranslation();

    const changeLanguage = (lng: string) => {
        i18n.changeLanguage(lng);
    };

    return (
        <div className="flex items-center space-x-2">
            <button
                onClick={() => changeLanguage('pt')}
                className={`px-2 py-1 text-sm rounded-md transition-colors ${i18n.language.startsWith('pt') ? 'bg-brand-primary text-white' : 'bg-transparent text-text-secondary'}`}
            >
                PT-BR
            </button>
            <button
                onClick={() => changeLanguage('en')}
                className={`px-2 py-1 text-sm rounded-md transition-colors ${i18n.language.startsWith('pt') ? 'bg-brand-primary text-white' : 'bg-transparent text-text-secondary'}`}
            >
                EN
            </button>
        </div>

    );
}
export default LanguageSelector;