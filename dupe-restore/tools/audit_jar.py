"""Inspect production class references and packaging; not an antivirus verdict."""
import argparse
import hashlib
import json
import struct
import zipfile
from pathlib import Path


def class_references(data):
    if data[:4] != b'\xca\xfe\xba\xbe':
        raise ValueError('Not a Java class file')
    count = struct.unpack_from('>H', data, 8)[0]
    offset = 10
    utf8 = {}
    classes = []
    index = 1
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            size = struct.unpack_from('>H', data, offset)[0]
            offset += 2
            utf8[index] = data[offset:offset + size].decode('utf-8', errors='replace')
            offset += size
        elif tag == 7:
            classes.append(struct.unpack_from('>H', data, offset)[0])
            offset += 2
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag in (8, 16, 19, 20):
            offset += 2
        elif tag == 15:
            offset += 3
        else:
            raise ValueError(f'Unsupported constant-pool tag {tag}')
        index += 1
    return {utf8[index] for index in classes}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('jar', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    references = set()
    unexpected = []
    class_count = 0
    with zipfile.ZipFile(args.jar) as archive:
        if archive.testzip() is not None:
            raise ValueError('Invalid archive CRC')
        for name in archive.namelist():
            if name.endswith('/'):
                continue
            if name.endswith('.class'):
                class_count += 1
                if not name.startswith('dev/linqfy/bigCasares/duperestore/') or '/probe/' in name:
                    unexpected.append(name)
                references.update(class_references(archive.read(name)))
            elif name not in {'META-INF/MANIFEST.MF', 'config.yml', 'plugin.yml'}:
                unexpected.append(name)
    forbidden_exact = {
        'java/lang/Runtime', 'java/lang/ProcessBuilder', 'java/lang/ClassLoader',
        'sun/misc/Unsafe', 'jdk/internal/misc/Unsafe',
    }
    forbidden_prefixes = (
        'java/lang/reflect/', 'java/net/', 'javax/script/',
        'net/minecraft/', 'org/bukkit/craftbukkit/',
        'io/papermc/paper/configuration/GlobalConfiguration',
    )
    forbidden = sorted(ref for ref in references
                       if ref in forbidden_exact or any(prefix in ref for prefix in forbidden_prefixes))
    report = {
        'sha256': hashlib.sha256(args.jar.read_bytes()).hexdigest(),
        'classCount': class_count,
        'unexpectedEntries': sorted(unexpected),
        'restrictedReferences': forbidden,
        'classReferences': sorted(references),
        'passed': not unexpected and not forbidden and class_count > 0,
        'scope': 'Checks direct production class references and included files; does not prove absence of malware or replace Defender.',
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    if not report['passed']:
        raise SystemExit('Production archive inspection failed; see report.')
    print(f'Production archive inspection passed: {class_count} classes; no restricted references or unexpected files.')


if __name__ == '__main__':
    main()
