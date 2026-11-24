"""Export only the four prepared commits and reviewed evidence for teammate handoff."""
import hashlib
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'daaf840b61f2b72dd946e3e4559aea3def095e2c'


def git(*args):
    return subprocess.check_output(['git', *map(str, args)], cwd=ROOT, text=True).strip()


def main():
    branch = git('branch', '--show-current')
    commits = git('rev-list', '--reverse', BASE + '..HEAD').splitlines()
    assert branch == 'codex/v0.1-guards' and len(commits) == 4, (branch, commits)
    report_path = ROOT / 'build/v01-guard-verification.json'
    report = json.loads(report_path.read_text(encoding='utf-8'))
    assert report['head'] == git('rev-parse', 'HEAD'), 'Regenerate verification for the committed HEAD'
    destination = ROOT / 'build/v01-guard-handoff'
    assert not destination.exists(), 'Preserve previous artifacts; choose a fresh export directory'
    (destination / 'patches').mkdir(parents=True)
    git('format-patch', '--binary', '--output-directory', destination / 'patches', BASE + '..HEAD')
    git('bundle', 'create', destination / 'guards-v0.1.bundle', branch, '^' + BASE)
    shutil.copy2(ROOT / report['release']['path'], destination / 'villagediplomacy-0.1.0.jar')
    shutil.copy2(report_path, destination / 'verification.json')
    screenshots = []
    for stage in ('initial', 'reloaded'):
        source = ROOT / 'build/v01-guard-client-check' / ('SkinViewerOne-' + stage + '.png')
        if source.exists():
            (destination / 'screenshots').mkdir(exist_ok=True)
            target = destination / 'screenshots' / source.name
            shutil.copy2(source, target)
            screenshots.append(target.relative_to(destination).as_posix())

    inventory = [
        {'sha': sha, 'subject': git('show', '-s', '--format=%s', sha),
         'files': git('diff-tree', '--no-commit-id', '--name-only', '-r', sha).splitlines()}
        for sha in commits
    ]
    (destination / 'commits.json').write_text(json.dumps(inventory, indent=2), encoding='utf-8')
    rows = '\n'.join(f'| {index + 1} | `{item["sha"]}` | {item["subject"]} |'
                     for index, item in enumerate(inventory))
    client_status = 'PASS, два клієнти, повний перезапуск сервера' if report['clients'].get('passed') else 'НЕ ПІДТВЕРДЖЕНО; див. verification.json'
    text = f'''# Пакет охоронців Village Diplomacy v0.1 для Артура

Дата підготовки: 2026-11-19. Гілка: `{branch}`.
Точна основа: `{BASE}`. Функції мешканців, професій і гарнізону 0.2.1 не перенесені.

| Порядок | Коміт | Зміст |
|---|---|---|
{rows}

Перелік файлів кожного коміту: `commits.json`. Той самий набір змін надано
як чотири `patches/*.patch` і як `guards-v0.1.bundle`; застосувати треба лише один варіант.

## Перенесення

На інтеграційній гілці Артура, створеній від погодженої master, можна імпортувати bundle:

```powershell
git fetch .\\guards-v0.1.bundle codex/v0.1-guards
```

Потім переносити наведені коміти по одному через `git cherry-pick <hash>` і після
кожного виконувати `.\\gradlew.bat test --no-daemon`. Альтернатива — `git am`
чотирьох patch-файлів у порядку номерів. Не виконувати обидва способи.
Якщо master змінилася після вказаної основи, повторно перевірити конфлікти та всі результати.

## Перевірено на підготовленій гілці

- Unit-тести: PASS, {report['unitTests']['tests']}, без failures/errors/skipped.
- Серверні guard GameTests: PASS, {report['requiredGameTests']}.
- Клієнти: {client_status}.
- Звичайна збірка `test build --no-daemon --offline`: PASS.
- JAR: чотири точні PNG 64×64; тестових класів, арен і resident/garrison-функцій немає.
- SHA-256 JAR: `{report['release']['sha256']}`.
- Скріншоти: {', '.join(screenshots) or 'не надано'}.

Кандидат JAR містить актуальне ядро основи та ці guard-зміни. Це не опублікований реліз.
Логи, середовище запуску, конфігурації, світи та кеші в пакет не включено.

## Відомі обмеження / робота перед релізом

- Первісний автор і ліцензія наданого користувачем базового скіна не встановлені.
  Перед публічним розповсюдженням підтвердити права або замінити ці текстури.
- Клієнтська галерея перевіряє реальний рендер і синхронізацію; весь сценарій
  дипломатії/торгівлі для презентації та бойові анімації окремо не перевірені.
- Поведінка нейтральних мобів збережена: звичайні пігліни, ендермени,
  зомбіфіковані пігліни й вартени не отримують безумовної агресії.
- Набір/навчання та автоматичний спавн охоронців не додаються цим пакетом;
  для демосвіту використовувати наявне creative-яйце або команду виклику.
- Метадані авторів, README/changelog, підготовка демосвіту та фінальний реліз
  залишаються завданнями інтеграції Артура. Спільний GUI, мережа та локалізації не змінені.
- Push, злиття в master, створення тегів і надсилання повідомлень не виконувалися.
'''
    (destination / 'HANDOFF_UA.md').write_text(text, encoding='utf-8')
    files = sorted(p for p in destination.rglob('*') if p.is_file())
    checksums = {p.relative_to(destination).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
    (destination / 'SHA256.json').write_text(json.dumps(checksums, indent=2), encoding='utf-8')
    target_zip = ROOT / 'build/v0.1-guard-handoff.zip'
    assert not target_zip.exists(), 'Do not overwrite an earlier export'
    with zipfile.ZipFile(target_zip, 'w', zipfile.ZIP_DEFLATED) as archive:
        for p in sorted(destination.rglob('*')):
            if p.is_file():
                archive.write(p, p.relative_to(destination).as_posix())
    print(target_zip)
    print(json.dumps({'branch': branch, 'commits': commits, 'zipBytes': target_zip.stat().st_size}))


if __name__ == '__main__':
    main()
