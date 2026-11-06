import copy
import json
from pathlib import Path
import tempfile
import unittest

from model_sources import MODEL_IDS, ROOT, SIDES, load_projects, normalize_invisible_faces


class ModelSourcesTest(unittest.TestCase):
    def test_current_models_keep_all_geometry_textures_and_eleven_animations(self):
        folder = ROOT / 'models/reference-study'
        mapping = json.loads((folder / 'model-overrides.json').read_text())
        projects = load_projects()
        self.assertEqual(set(MODEL_IDS), set(projects))
        clips = 0
        for key, project in projects.items():
            original = json.loads((folder / mapping[key]['original']).read_bytes())
            self.assertEqual(original['textures'], project['textures'])
            self.assertEqual(original.get('animations', []), project.get('animations', []))
            clips += len(project.get('animations', []))
            self.assertEqual(len(original['elements']), len(project['elements']))
            added = 0
            for before, after in zip(original['elements'], project['elements']):
                self.assertEqual({k: v for k, v in before.items() if k != 'faces'},
                                 {k: v for k, v in after.items() if k != 'faces'})
                for side, face in before.get('faces', {}).items():
                    self.assertEqual(face, after['faces'][side])
                for side in after.get('faces', {}).keys() - before.get('faces', {}).keys():
                    added += 1
                    self.assertIsNone(after['faces'][side]['texture'])
            self.assertEqual(50 if key == 'shadow_blade' else 0, added)
        self.assertEqual(11, clips)

    def test_normalization_does_not_mutate_input_or_visible_face_order(self):
        project = {'elements': [{'type': 'cube', 'from': [0, 1, 2], 'to': [3, 4, 5],
                                'faces': {'south': {'texture': 0, 'uv': [1, 2, 3, 4]},
                                          'north': {'texture': None}}}],
                   'animations': [{'name': 'idle', 'length': 2}]}
        before = copy.deepcopy(project)
        normalized = normalize_invisible_faces(project)
        self.assertEqual(before, project)
        self.assertEqual(set(SIDES), set(normalized['elements'][0]['faces']))
        visible = lambda faces: [side for side, face in faces.items() if face.get('texture') is not None]
        self.assertEqual(visible(project['elements'][0]['faces']), visible(normalized['elements'][0]['faces']))
        self.assertEqual(project['animations'], normalized['animations'])

    def test_mesh_faces_are_not_replaced_with_cube_faces(self):
        project = {'elements': [{'type': 'mesh', 'vertices': {'a': [1, 2, 3]},
                                'faces': {'triangle': {'vertices': ['a', 'b', 'c']}}}]}
        self.assertEqual(project, normalize_invisible_faces(project))

    def test_refuses_a_source_outside_the_originals_directory(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            folder = root / 'models/reference-study'
            folder.mkdir(parents=True)
            (folder / 'model-overrides.json').write_text(json.dumps({
                'bloodlust': {'original': '../outside.bbmodel', 'sha256': '0' * 64}}))
            with self.assertRaisesRegex(ValueError, 'user-sources'):
                load_projects(root)

    def test_rejects_changed_original_before_importing_it(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            folder = root / 'models/reference-study'
            (folder / 'user-sources').mkdir(parents=True)
            data = b'{"elements":[]}'
            source = folder / 'user-sources/changed.bbmodel'
            source.write_bytes(data)
            (folder / 'model-overrides.json').write_text(json.dumps({
                'bloodlust': {'original': 'user-sources/changed.bbmodel', 'sha256': '0' * 64}}))
            with self.assertRaisesRegex(ValueError, 'SHA-256'):
                load_projects(root)
            self.assertEqual(data, source.read_bytes())


if __name__ == '__main__':
    unittest.main()
