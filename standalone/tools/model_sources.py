"""Load the five original projects; keep compatibility adjustments in memory."""
import copy
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODEL_IDS = ('bloodlust', 'shadow_blade', 'windweaver', 'earthbreaker', 'thunder_spear')
SIDES = ('north', 'east', 'south', 'west', 'up', 'down')


def normalize_invisible_faces(project):
    normalized = copy.deepcopy(project)
    for element in normalized.get('elements', []):
        if element.get('type') != 'cube':
            continue
        faces = element.setdefault('faces', {})
        for side in SIDES:
            if side not in faces:
                faces[side] = {'uv': [0, 0, 0, 0], 'texture': None}
    return normalized


def load_projects(root=ROOT):
    folder = Path(root) / 'models/reference-study'
    originals = (folder / 'user-sources').resolve()
    mapping = json.loads((folder / 'model-overrides.json').read_text(encoding='utf-8'))
    projects = {}
    for key in MODEL_IDS:
        entry = mapping[key]
        source = (folder / entry['original']).resolve()
        if not source.is_relative_to(originals):
            raise ValueError(f'{key}: model source must stay inside user-sources')
        data = source.read_bytes()
        if hashlib.sha256(data).hexdigest() != entry['sha256']:
            raise ValueError(f'{key}: original SHA-256 does not match model-overrides.json')
        project = json.loads(data)
        if entry.get('normalization'):
            project = normalize_invisible_faces(project)
        projects[key] = project
    return projects


if __name__ == '__main__':
    output = ROOT / 'build/model-sources'
    output.mkdir(parents=True, exist_ok=True)
    for key, project in load_projects().items():
        (output / (key + '.bbmodel')).write_text(
            json.dumps(project, ensure_ascii=False, separators=(',', ':')) + '\n', encoding='utf-8')
    print(output)
