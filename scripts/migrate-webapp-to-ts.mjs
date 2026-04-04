import fs from 'node:fs';
import path from 'node:path';

const projectRoot = process.cwd();
const webappRoot = path.join(projectRoot, 'src', 'main', 'webapp');
const sourceRoot = path.join(webappRoot, 'src');

const JS_EXTS = new Set(['.js', '.jsx']);
const TS_EXTS = ['.ts', '.tsx'];

function toPosix(p) {
  return p.replace(/\\/g, '/');
}

function toKebabCase(input) {
  return input
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/[_\s]+/g, '-')
    .replace(/-+/g, '-')
    .toLowerCase();
}

function toPascalCase(input) {
  return input
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/[_\-\s]+/g, ' ')
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join('');
}

function isComponentFile(relPath, content) {
  const p = toPosix(relPath);

  if (p === 'src/App.jsx' || p === 'src/App.js') {
    return true;
  }

  if (/^src\/(components|pages)\//.test(p)) {
    return true;
  }

  if (/^src\/features\/[^/]+\/(components|pages)\//.test(p)) {
    return true;
  }

  if (/^src\/contexts\/data\/[A-Z]/.test(path.basename(p))) {
    return true;
  }

  if (/export\s+default\s+function\s+[A-Z]/.test(content)) {
    return true;
  }

  return false;
}

function collectJsFiles(dir, list = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    const abs = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      collectJsFiles(abs, list);
      continue;
    }

    const ext = path.extname(entry.name);
    if (JS_EXTS.has(ext)) {
      list.push(abs);
    }
  }

  return list;
}

if (!fs.existsSync(sourceRoot)) {
  throw new Error(`Khong tim thay thu muc nguon: ${sourceRoot}`);
}

const oldFiles = collectJsFiles(sourceRoot);
const mapping = new Map();
const oldContent = new Map();

for (const oldAbs of oldFiles) {
  const rel = path.relative(webappRoot, oldAbs);
  const ext = path.extname(oldAbs);
  const dir = path.dirname(rel);
  const originalBase = path.basename(rel, ext);
  const content = fs.readFileSync(oldAbs, 'utf8');

  const componentFile = isComponentFile(rel, content);

  let newBase = originalBase;
  if (!['index', 'main'].includes(originalBase)) {
    newBase = componentFile ? toPascalCase(originalBase) : toKebabCase(originalBase);
  }

  if (toPosix(rel) === 'src/App.jsx' || toPosix(rel) === 'src/App.js') {
    newBase = 'App';
  }

  const newExt = ext === '.jsx' ? '.tsx' : '.ts';
  const newRel = path.join(dir, `${newBase}${newExt}`);
  const newAbs = path.join(webappRoot, newRel);

  mapping.set(path.resolve(oldAbs), path.resolve(newAbs));
  oldContent.set(path.resolve(oldAbs), content);
}

function resolveOldTarget(fromOldAbs, specifier) {
  const fromDir = path.dirname(fromOldAbs);
  const base = path.resolve(fromDir, specifier);

  const directCandidates = [
    base,
    ...Array.from(JS_EXTS).map((ext) => `${base}${ext}`),
    ...TS_EXTS.map((ext) => `${base}${ext}`),
  ];

  for (const candidate of directCandidates) {
    const resolved = path.resolve(candidate);
    if (mapping.has(resolved)) {
      return { resolved, viaIndex: false };
    }
  }

  const indexCandidates = [
    ...Array.from(JS_EXTS).map((ext) => path.join(base, `index${ext}`)),
    ...TS_EXTS.map((ext) => path.join(base, `index${ext}`)),
  ];

  for (const candidate of indexCandidates) {
    const resolved = path.resolve(candidate);
    if (mapping.has(resolved)) {
      return { resolved, viaIndex: true };
    }
  }

  return null;
}

function cleanImportPath(relPath, viaIndex, originalSpecifier) {
  let normalized = toPosix(relPath);
  normalized = normalized.replace(/\.(ts|tsx|js|jsx)$/i, '');

  if (viaIndex && !originalSpecifier.endsWith('/index') && originalSpecifier !== './index' && originalSpecifier !== '../index') {
    normalized = normalized.replace(/\/index$/i, '');
  }

  if (!normalized.startsWith('.')) {
    normalized = `./${normalized}`;
  }

  return normalized;
}

function rewriteImports(content, fromOldAbs, toNewAbs) {
  const patterns = [
    {
      regex: /(from\s+['"])([^'"\n]+)(['"])/g,
      groupIndex: 2,
    },
    {
      regex: /(import\s*\(\s*['"])([^'"\n]+)(['"]\s*\))/g,
      groupIndex: 2,
    },
    {
      regex: /(import\s+['"])([^'"\n]+)(['"])/g,
      groupIndex: 2,
    },
    {
      regex: /(require\(\s*['"])([^'"\n]+)(['"]\s*\))/g,
      groupIndex: 2,
    },
  ];

  let updated = content;

  for (const { regex, groupIndex } of patterns) {
    updated = updated.replace(regex, (...args) => {
      const match = args[0];
      const specifier = args[groupIndex];

      if (!specifier.startsWith('.')) {
        return match;
      }

      const target = resolveOldTarget(fromOldAbs, specifier);
      if (!target) {
        return match;
      }

      const newTargetAbs = mapping.get(target.resolved);
      const newFromDir = path.dirname(toNewAbs);
      const newRelative = path.relative(newFromDir, newTargetAbs);
      const finalSpecifier = cleanImportPath(newRelative, target.viaIndex, specifier);

      return match.replace(specifier, finalSpecifier);
    });
  }

  updated = updated.replace(/(from\s+['"][^'"]+)\.jsx(['"])/g, '$1$2');
  updated = updated.replace(/(from\s+['"][^'"]+)\.js(['"])/g, '$1$2');
  updated = updated.replace(/(import\s*\(\s*['"][^'"]+)\.jsx(['"]\s*\))/g, '$1$2');
  updated = updated.replace(/(import\s*\(\s*['"][^'"]+)\.js(['"]\s*\))/g, '$1$2');
  updated = updated.replace(/(import\s+['"][^'"]+)\.jsx(['"])/g, '$1$2');
  updated = updated.replace(/(import\s+['"][^'"]+)\.js(['"])/g, '$1$2');

  return updated;
}

for (const [oldAbs, newAbs] of mapping.entries()) {
  const content = oldContent.get(oldAbs);
  const rewritten = rewriteImports(content, oldAbs, newAbs);
  fs.mkdirSync(path.dirname(newAbs), { recursive: true });
  fs.writeFileSync(newAbs, rewritten, 'utf8');
}

for (const oldAbs of mapping.keys()) {
  if (path.resolve(oldAbs) !== path.resolve(mapping.get(oldAbs))) {
    fs.unlinkSync(oldAbs);
  }
}

console.log(`Da chuyen ${mapping.size} file JS/JSX sang TS/TSX va cap nhat import.`);
