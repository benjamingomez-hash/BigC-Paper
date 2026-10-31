import unittest
from copy import deepcopy
import numpy as np
from optimize_legendary_pack import (
    optimize_elements,
    sample_sequence,
    dominant_transform,
    element_corners,
)

SIDES = ("west", "east", "down", "up", "north", "south")


def cube(low, high, color=0):
    return {
        "from": list(low),
        "to": list(high),
        "faces": {s: {"uv": [color, 0, color + 1, 1], "texture": "#body"} for s in SIDES},
        "shade": True,
    }


class SurfaceMeshingTest(unittest.TestCase):
    def setUp(self):
        self.pixels = np.full((16, 16, 4), 255, dtype=np.uint8)

    def test_adjacent_flat_cubes_merge_without_changing_external_surfaces(self):
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        output, report = optimize_elements(source, self.pixels)
        self.assertEqual(1, len(output))
        self.assertEqual([0, 0, 0], output[0]["from"])
        self.assertEqual([2, 1, 1], output[0]["to"])
        self.assertEqual(6, len(output[0]["faces"]))
        self.assertEqual(2, report["hiddenFaces"])
        self.assertEqual(12, sum(len(x["faces"]) for x in source))

    def test_hidden_inner_cube_disappears_but_surface_colors_are_unchanged(self):
        self.pixels[:, 1, :3] = 50
        outer = cube((0, 0, 0), (4, 4, 4))
        inner = cube((1, 1, 1), (3, 3, 3), 1)
        output, _ = optimize_elements([outer, inner], self.pixels)
        self.assertEqual([outer], output)

    def test_translucent_geometry_is_never_used_as_an_occluder_or_merged(self):
        self.pixels[:, :, 3] = 150
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        output, _ = optimize_elements(source, self.pixels)
        self.assertEqual(2, len(output))
        self.assertEqual(12, sum(len(e["faces"]) for e in output))

    def test_unused_transparent_texel_preserves_full_sprite_quad_order(self):
        self.pixels[15, 15, 3] = 150
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        output, _ = optimize_elements(source, self.pixels)
        self.assertEqual(source, output)

    def test_patterned_textures_are_not_stretched_by_merging(self):
        self.pixels[0, 1, :3] = 0
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        for e in source:
            for f in e["faces"].values():
                f["uv"] = [0, 0, 2, 1]
        output, _ = optimize_elements(source, self.pixels)
        self.assertEqual(2, len(output))

    def test_shared_global_rotation_preserves_world_corners(self):
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        for e in source:
            e["rotation"] = {"origin": [0, 0, 0], "x": 0, "y": 0, "z": 45}
        output, _ = optimize_elements(source, self.pixels)
        self.assertEqual(1, len(output))
        expected = np.concatenate([element_corners(e)[0] for e in source])
        actual = element_corners(output[0])[0]
        self.assertTrue(np.allclose(expected.min(axis=0), actual.min(axis=0), atol=2e-6))
        self.assertTrue(np.allclose(expected.max(axis=0), actual.max(axis=0), atol=2e-6))

    def test_resampling_preserves_duration_and_nonlooping_last_pose(self):
        source = list(range(19))
        sampled = sample_sequence(source, 4, False)
        self.assertEqual(len(source), len(sampled))
        self.assertEqual(18, sampled[-1])
        self.assertEqual([0, 0, 0, 0, 4], sampled[:5])

    def test_shared_affine_transform_matches_all_original_vertices(self):
        source = [cube((0, 0, 0), (1, 1, 1)), cube((1, 0, 0), (2, 1, 1))]
        pose = deepcopy(source)
        for e in pose:
            e["from"] = [x + 3 for x in e["from"]]
            e["to"] = [x + 3 for x in e["to"]]
        indices, linear, translation = dominant_transform(source, pose)
        self.assertEqual([0, 1], indices)
        self.assertTrue(np.allclose(linear, np.eye(3)))
        self.assertTrue(np.allclose(translation, [3, 3, 3]))
        pose[0]["faces"].pop("up")
        self.assertIsNone(dominant_transform(source, pose))


if __name__ == "__main__":
    unittest.main()
