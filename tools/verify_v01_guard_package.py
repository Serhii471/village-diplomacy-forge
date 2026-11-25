"""Validate the real v0.1 build/test evidence without importing game libraries."""
import hashlib
import json
import re
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'daaf840b61f2b72dd946e3e4559aea3def095e2c'


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True).strip()


def main():
    jar = ROOT / 'build/libs/villagediplomacy-0.1.0.jar'
    unit_files = list((ROOT / 'build/test-results/test').glob('TEST-*.xml'))
    assert unit_files, 'No JUnit XML results'
    suites = [ET.parse(p).getroot() for p in unit_files]
    units = {key: sum(int(s.attrib.get(key, 0)) for s in suites)
             for key in ('tests', 'failures', 'errors', 'skipped')}
    assert units['tests'] >= 89 and all(units[k] == 0 for k in ('failures', 'errors', 'skipped')), units
    log = (ROOT / 'build/v01-guard-gametest.log').read_text(encoding='utf-8', errors='replace')
    matches = re.findall(r'All (\d+) required tests passed', log)
    assert matches and int(matches[-1]) == 25, 'Guard GameTests did not pass all 25 cases'

    allowed_java = {
        'src/main/java/com/diplomacy/villagediplomacy/entity/VillageGuardEntity.java',
        'src/main/java/com/diplomacy/villagediplomacy/client/renderer/VillageGuardRenderer.java',
        'src/main/java/com/diplomacy/villagediplomacy/entity/ai/GuardHostilityEvents.java',
        'src/main/java/com/diplomacy/villagediplomacy/entity/ai/GuardTargetGoal.java',
    }
    paths = git('diff', '--name-only', BASE).splitlines()
    assert {p for p in paths if p.startswith('src/main/java/')} <= allowed_java, paths
    for name in allowed_java:
        source = (ROOT / name).read_text(encoding='utf-8-sig')
        assert not re.search(r'ResidentData|garrison|residentSnapshot|ResidentNetwork', source), name

    textures = []
    with zipfile.ZipFile(jar) as archive:
        entries = archive.namelist()
        forbidden = [p for p in entries if any(x in p.lower() for x in
                     ('gametests', 'guard_arena', 'skincheck', 'garrison', 'resident', '.log', '.mca'))]
        assert not forbidden, forbidden
        for variant in ('base', 'plains', 'mountain', 'forest'):
            resource = 'assets/villagediplomacy/textures/entity/guard/guard_' + variant + '.png'
            data = archive.read(resource)
            assert data == (ROOT / 'src/main/resources' / resource).read_bytes(), resource
            assert struct.unpack('>II', data[16:24]) == (64, 64), resource
            textures.append({'resource': resource, 'sha256': hashlib.sha256(data).hexdigest()})
        for name in ('VillageGuardEntity.class', 'GuardHostilityEvents.class', 'GuardTargetGoal.class',
                     'DiplomacyTableScreen.class', 'TradeHandoverScreen.class', 'RouteScreen.class', 'DiplomacyNetwork.class'):
            assert any(p.endswith('/' + name) for p in entries), name

        spawn_egg_model = 'assets/villagediplomacy/models/item/village_guard_spawn_egg.json'
        assert spawn_egg_model in entries, spawn_egg_model
        model = json.loads(archive.read(spawn_egg_model))
        assert model == {'parent': 'item/template_spawn_egg'}, model

    client_results = (
        ROOT / 'build/v01-release-client-check/result.json',
        ROOT / 'build/v01-guard-client-check/result.json',
    )
    client_result = next((path for path in client_results if path.exists()), None)
    client = (json.loads(client_result.read_text(encoding='utf-8')) if client_result
              else {'passed': False, 'reason': 'not run'})
    report = {
        'base': BASE, 'head': git('rev-parse', 'HEAD'), 'branch': git('branch', '--show-current'),
        'unitTests': units, 'requiredGameTests': 25, 'clients': client,
        'release': {'path': str(jar.relative_to(ROOT)), 'bytes': jar.stat().st_size,
                    'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
                    'testCodeAbsent': True, 'residentCodeAbsent': True},
        'textures': textures,
        'limitations': ['No end-to-end trade-contract completion was performed in the real client.',
                        'No combat-animation verification was performed in the real client.'],
    }
    destination = ROOT / 'build/v01-guard-verification.json'
    destination.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding='utf-8')
    print(json.dumps({key: report[key] for key in ('unitTests', 'requiredGameTests', 'clients', 'release')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
