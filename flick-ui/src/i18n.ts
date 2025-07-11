import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import enTranslation from './locales/en/translation.json';
import ptTranslation from './locales/pt/translation.json';

i18n.use(LanguageDetector).use(initReactI18next).init({
	debug: true,
	fallbackLng: 'en',
	interpolation: {
		escapeValue: false,
	},
	resources: {
		en: {
			translation: enTranslation,
		},
		pt: {
			translation: ptTranslation,
		},
	},
});

export default i18n;
