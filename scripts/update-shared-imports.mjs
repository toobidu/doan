import fs from 'node:fs';
import path from 'node:path';

const root = 'c:/Users/dungt/OneDrive/Desktop/Code/Project/Doan/doan-backend/src/main/webapp/src';
const exts = new Set(['.ts', '.tsx']);

const replacements = [
  [/from\s+['"](?:\.\.\/)+(?:components)\/PopupNotification['"]/g, "from '@shared/components/PopupNotification'"],
  [/from\s+['"](?:\.\.\/)+(?:hooks)\/use-popup['"]/g, "from '@shared/hooks/use-popup'"],
  [/from\s+['"](?:\.\.\/)+(?:components)\/Decoration['"]/g, "from '@shared/components/Decoration'"],
  [/from\s+['"](?:\.\.\/)+(?:components)\/SimpleBackground['"]/g, "from '@shared/components/SimpleBackground'"],
  [/from\s+['"](?:\.\.\/)+(?:contexts)\/theme-context['"]/g, "from '@shared/contexts/theme-context'"],
];

function walk(dir) {
  for (const name of fs.readdirSync(dir)) {
    const filePath = path.join(dir, name);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      walk(filePath);
      continue;
    }

    if (!exts.has(path.extname(filePath))) {
      continue;
    }

    const original = fs.readFileSync(filePath, 'utf8');
    let updated = original;

    for (const [regex, replacement] of replacements) {
      updated = updated.replace(regex, replacement);
    }

    if (updated !== original) {
      fs.writeFileSync(filePath, updated, 'utf8');
    }
  }
}

walk(root);
console.log('shared imports updated');
