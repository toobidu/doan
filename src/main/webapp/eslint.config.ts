import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import unusedImports from 'eslint-plugin-unused-imports'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx,js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    plugins: {
      'unused-imports': unusedImports,
    },
    languageOptions: {
      ecmaVersion: 2020,
      globals: {
        ...globals.browser,
        ...globals.node,
      },
      parserOptions: {
        ecmaVersion: 'latest',
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
    },
    rules: {
      'no-unused-vars': 'off',
      'unused-imports/no-unused-imports': 'error',
      'unused-imports/no-unused-vars': [
        'warn',
        {
          vars: 'all',
          varsIgnorePattern: '^_',
          args: 'after-used',
          argsIgnorePattern: '^_',
          ignoreRestSiblings: true,
        },
      ],
      'no-empty': ['warn', { allowEmptyCatch: true }],
      'no-case-declarations': 'error',
      'no-useless-catch': 'error',
      'no-dupe-else-if': 'error',
      'no-undef': 'off',
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      'react-hooks/exhaustive-deps': 'warn',
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: [
                '../components/PopupNotification',
                '../../components/PopupNotification',
                '../../../components/PopupNotification',
                '../../../../components/PopupNotification',
                '../components/Decoration',
                '../../components/Decoration',
                '../../../components/Decoration',
                '../../../../components/Decoration',
                '../components/SimpleBackground',
                '../../components/SimpleBackground',
                '../../../components/SimpleBackground',
                '../../../../components/SimpleBackground',
                '../hooks/use-popup',
                '../../hooks/use-popup',
                '../../../hooks/use-popup',
                '../../../../hooks/use-popup',
                '../contexts/theme-context',
                '../../contexts/theme-context',
                '../../../contexts/theme-context',
                '../../../../contexts/theme-context',
              ],
              message: 'Dung alias @shared/* thay cho import tuong doi de dam bao cau truc module nhat quan.',
            },
          ],
        },
      ],
    },
  },
])
