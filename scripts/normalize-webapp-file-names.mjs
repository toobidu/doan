import fs from 'node:fs';
import path from 'node:path';

const projectRoot = process.cwd();
const webappRoot = path.join(projectRoot, 'src', 'main', 'webapp');
const sourceRoot = path.join(webappRoot, 'src');

const TARGET_EXTENSIONS = new Set(['.ts', '.tsx', '.css']);

function toPosix(value) {
  return value.replace(/\\/g, '/');
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

function collectFiles(dir, list = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    const abs = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      collectFiles(abs, list);
      continue;
    }

    const ext = path.extname(entry.name);
    if (TARGET_EXTENSIONS.has(ext)) {
      list.push(abs);
    }
  }

  return list;
}

function isComponentPath(relPath) {
  const p = toPosix(relPath);

  if (/^src\/components\//.test(p)) {
    return true;
  }

  if (/^src\/features\/[^/]+\/components\//.test(p)) {
    return true;
  }

  if (p === 'src/App.tsx' || p === 'src/App.ts' || p === 'src/App.css') {
    return true;
  }

  return false;
}

if (!fs.existsSync(sourceRoot)) {
  throw new Error(`Khong tim thay source root: ${sourceRoot}`);
}

const files = collectFiles(sourceRoot);
const renameMap = new Map();

for (const oldAbs of files) {
  const rel = path.relative(webappRoot, oldAbs);
  const ext = path.extname(oldAbs);
  const dir = path.dirname(rel);
  const base = path.basename(rel, ext);

  if (base === 'index' || base === 'main') {
    continue;
  }

  const componentFile = isComponentPath(rel);
  const newBase = componentFile ? toPascalCase(base) : toKebabCase(base);

  if (newBase === base) {
    continue;
  }

  const newRel = path.join(dir, `${newBase}${ext}`);
  const newAbs = path.join(webappRoot, newRel);

  renameMap.set(path.resolve(oldAbs), path.resolve(newAbs));
}

const allTextFiles = collectFiles(sourceRoot);

function resolveTargetFromSpecifier(fromAbs, specifier) {
  const fromDir = path.dirname(fromAbs);
  const rawTarget = path.resolve(fromDir, specifier);

  const possible = [
    rawTarget,
    `${rawTarget}.ts`,
    `${rawTarget}.tsx`,
    `${rawTarget}.css`,
    path.join(rawTarget, 'index.ts'),
    path.join(rawTarget, 'index.tsx'),
    path.join(rawTarget, 'index.css')
  ];

  for (const p of possible) {
    const resolved = path.resolve(p);
    if (renameMap.has(resolved)) {
      return renameMap.get(resolved);
    }

    if (fs.existsSync(resolved) && fs.statSync(resolved).isFile()) {
      return resolved;
    }
  }

  return null;
}

function cleanSpecifier(value) {
  let next = toPosix(value)
    .replace(/\.(ts|tsx)$/i, '')
    .replace(/\/index$/i, '');

  if (!next.startsWith('.')) {
    next = `./${next}`;
  }

  return next;
}

function rewriteImports(content, fileAbs) {
  const patterns = [
    /(from\s+['"])([^'"\n]+)(['"])/g,
    /(import\s*\(\s*['"])([^'"\n]+)(['"]\s*\))/g,
    /(import\s+['"])([^'"\n]+)(['"])/g,
    /(require\(\s*['"])([^'"\n]+)(['"]\s*\))/g
  ];

  let updated = content;

  for (const regex of patterns) {
    updated = updated.replace(regex, (match, p1, specifier, p3) => {
      if (!specifier.startsWith('.')) {
        return match;
      }

      const resolvedTarget = resolveTargetFromSpecifier(fileAbs, specifier);
      if (!resolvedTarget) {
        return match;
      }

      const relativePath = path.relative(path.dirname(fileAbs), resolvedTarget);
      const finalSpecifier = cleanSpecifier(relativePath);

      return `${p1}${finalSpecifier}${p3}`;
    });
  }

  return updated;
}

for (const fileAbs of allTextFiles) {
  const original = fs.readFileSync(fileAbs, 'utf8');
  const rewritten = rewriteImports(original, fileAbs);
  if (rewritten !== original) {
    fs.writeFileSync(fileAbs, rewritten, 'utf8');
  }
}

if (renameMap.size > 0) {
  const sortedRenames = Array.from(renameMap.entries()).sort((a, b) => b[0].length - a[0].length);

  for (const [oldAbs, newAbs] of sortedRenames) {
    if (!fs.existsSync(oldAbs)) {
      continue;
    }

    fs.mkdirSync(path.dirname(newAbs), { recursive: true });
    fs.renameSync(oldAbs, newAbs);
  }
}

const freshFiles = collectFiles(sourceRoot);
for (const fileAbs of freshFiles) {
  const original = fs.readFileSync(fileAbs, 'utf8');
  const rewritten = rewriteImports(original, fileAbs);
  if (rewritten !== original) {
    fs.writeFileSync(fileAbs, rewritten, 'utf8');
  }
}

console.log(`Da doi ten ${renameMap.size} file theo naming convention.`);
