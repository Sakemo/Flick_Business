// tailwind.config.js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}", // Aponta para todos os arquivos dentro de src
  ],
  theme: {
    extend: {
      colors: {
        // Paleta principal
        'brand-primary': '#6D28D9', // Roxo vibrante
        'brand-accent': '#FDE047', // Amarelo limão
        'brand-muted': '#E0E7FF',  // Roxo pastel
        'bg-light': '#F8F9FA',     // Fundo claro
        'bg-dark': '#1A202C',      // Fundo escuro
        'card-light': '#FFFFFF',   // Card claro
        'card-dark': '#2D3748',    // Card escuro
        'text-primary': '#1F2937', // Texto principal
        'text-secondary': '#6B7280', // Texto secundário
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui'],
      },
      borderRadius: {
        'card': '1rem',     // 16px
        'btn': '0.5rem',    // 8px
        'lg': '1.5rem',     // 24px opcional para visuais mais "soft"
      },
      boxShadow: {
        'soft': '0 2px 12px rgba(0, 0, 0, 0.06)',
        'card': '0 4px 24px rgba(0, 0, 0, 0.08)',
      },
      spacing: {
        'section': '4rem',  // Para grandes espaços entre seções
        'card-padding': '1.5rem', // Padding interno de cards
      },
      typography: (theme) => ({
        DEFAULT: {
          css: {
            color: theme('colors.text-primary'),
            a: { color: theme('colors.brand-primary'), textDecoration: 'none' },
            strong: { color: theme('colors.text-primary') },
            h1: { fontSize: '2rem', fontWeight: '700' },
            h2: { fontSize: '1.5rem', fontWeight: '600' },
            h3: { fontSize: '1.25rem', fontWeight: '500' },
            p: { color: theme('colors.text-secondary') },
          },
        },
        dark: {
          css: {
            color: theme('colors.gray.100'),
            a: { color: theme('colors.brand-accent') },
            strong: { color: theme('colors.gray.100') },
            p: { color: theme('colors.gray.300') },
          },
        },
      }),
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
  darkMode: 'class',
}
