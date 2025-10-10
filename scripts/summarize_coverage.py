#!/usr/bin/env python3
import os
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
APP = ROOT / 'app'

UNIT_XML = APP / 'build' / 'reports' / 'jacoco' / 'jacocoUnitTestReport' / 'jacoco-unit.xml'
ANDROID_XML = APP / 'build' / 'reports' / 'jacoco' / 'jacocoAndroidTestReport' / 'jacoco-androidTest.xml'
OUT_MD = ROOT / 'reports' / 'COVERAGE.md'


def parse_counters(xml_path: Path):
    if not xml_path.exists():
        return None
    tree = ET.parse(str(xml_path))
    root = tree.getroot()
    totals = {}
    for counter in root.findall('.//counter'):
        ctype = counter.attrib.get('type')
        missed = int(counter.attrib.get('missed', '0'))
        covered = int(counter.attrib.get('covered', '0'))
        totals.setdefault(ctype, {'missed': 0, 'covered': 0})
        totals[ctype]['missed'] += missed
        totals[ctype]['covered'] += covered
    def pct(kind):
        m = totals.get(kind, {'missed': 0, 'covered': 0})
        total = m['missed'] + m['covered']
        return 0.0 if total == 0 else (m['covered'] * 100.0 / total)
    return {
        'LINE': pct('LINE'),
        'BRANCH': pct('BRANCH'),
        'INSTRUCTION': pct('INSTRUCTION'),
        'COMPLEXITY': pct('COMPLEXITY')
    }


def main():
    unit = parse_counters(UNIT_XML)
    android = parse_counters(ANDROID_XML)
    OUT_MD.parent.mkdir(parents=True, exist_ok=True)
    with OUT_MD.open('w', encoding='utf-8') as f:
        f.write('### Coverage Summary (Jacoco)\n\n')
        if unit:
            f.write(f"- Unit: lines={unit['LINE']:.1f}% branches={unit['BRANCH']:.1f}% instructions={unit['INSTRUCTION']:.1f}%\n")
            f.write('  - HTML: reports/jacoco/unit/index.html\n')
        else:
            f.write('- Unit: no XML found\n')
        if android:
            f.write(f"- AndroidTest: lines={android['LINE']:.1f}% branches={android['BRANCH']:.1f}% instructions={android['INSTRUCTION']:.1f}%\n")
            f.write('  - HTML: reports/jacoco/androidTest/index.html\n')
        else:
            f.write('- AndroidTest: no XML found\n')

if __name__ == '__main__':
    main()


