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
        'brand-secondary-accent': '#38BDF8', // Azul Ciano
        'brand-muted': '#EDE9FE',  // Roxo pastel
        'bg-light': '#F8F9FA',     // Fundo claro
        'bg-dark': '#1A202C',      // Fundo escuro
        'card-light': '#F9FAFB',   // Card claro
        'card-dark': '#2D3748',    // Card escuro
        'text-primary': '#1F2937', // Texto principal
        'text-secondary': '#6B7280', // Texto secundário
        'border-light': '#E5E7EB', // GRAY 300
        'border-dark': '#374151', // GRAY 700
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
        'hover': '0 6px 16px rgba(0, 0, 0, 0.07',
      },
      spacing: {
        'section': '4rem',  // Para grandes espaços entre seções
        'card-padding': '1.5rem', // Padding interno de cards
      },
      typography: (theme) => ({
        DEFAULT: {
          css: {
            '--tw-prose-body': theme('colors.text-secondary'), // Texto de parágrafo
            '--tw-prose-headings': theme('colors.text-primary'), // Títulos
            '--tw-prose-lead': theme('colors.text-secondary'),
            '--tw-prose-links': theme('colors.brand-primary'), // Cor de links
            '--tw-prose-bold': theme('colors.text-primary'),
            // ... outros overrides de cores prose ...
            h1: { fontSize: theme('fontSize.2xl'), fontWeight: '600', marginBottom: theme('spacing.4') }, // 24px (se 2xl = 24px)
            h2: { fontSize: theme('fontSize.xl'), fontWeight: '600', marginBottom: theme('spacing.3') },   // 20px
            h3: { fontSize: theme('fontSize.lg'), fontWeight: '500', marginBottom: theme('spacing.2') },   // 18px
            p: { lineHeight: '1.6' }, // Espaçamento de linha
          },
        },
        dark: {
          css: {
            '--tw-prose-body': theme('colors.gray.300'),
            '--tw-prose-headings': theme('colors.white'),
            '--tw-prose-links': theme('colors.brand-accent'),
            '--tw-prose-bold': theme('colors.white'),
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
