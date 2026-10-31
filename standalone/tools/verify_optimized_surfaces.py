#!/usr/bin/env python3
"""Independently ray-check visible materials of original and optimized rest meshes."""
import io
import json
from pathlib import Path
import zipfile
import numpy as np
from PIL import Image
from optimize_legendary_pack import IDS, SIDES, rotation

ROOT = Path(__file__).resolve().parents[1]


def quads(elements, pixels):
    points = []
    u = []
    v = []
    normals = []
    materials = []
    for part in elements:
        low = np.array(part["from"])
        high = np.array(part["to"])
        r = rotation([part.get("rotation", {}).get(a, 0) for a in ("x", "y", "z")])
        origin = np.array(part.get("rotation", {}).get("origin", (low + high) / 2))
        for side, face in part["faces"].items():
            axis, sign = SIDES[side]
            others = [a for a in range(3) if a != axis]
            point = low.copy()
            point[axis] = (low if sign < 0 else high)[axis]
            point = origin + r @ (point - origin)
            first = np.zeros(3)
            second = np.zeros(3)
            first[others[0]] = high[others[0]] - low[others[0]]
            second[others[1]] = high[others[1]] - low[others[1]]
            n = np.zeros(3)
            n[axis] = sign
            uv = face["uv"]
            region = pixels[
                int(min(uv[1], uv[3])) : int(max(uv[1], uv[3])),
                int(min(uv[0], uv[2])) : int(max(uv[0], uv[2])),
            ]
            flat = bool(np.all(region == region[0, 0]))
            material = (
                ("flat", tuple(region[0, 0]))
                if flat
                else ("pattern", tuple(uv), face.get("rotation", 0))
            )
            points.append(point)
            u.append(r @ first)
            v.append(r @ second)
            normals.append(r @ n)
            materials.append((material, part.get("shade", True), part.get("light_emission", 0)))
    return tuple(np.array(x) for x in (points, u, v, normals)), materials


def hit(data, materials, origin, direction):
    points, u, v, normals = data
    den = normals @ direction
    t = np.divide(
        np.sum(normals * (points - origin), axis=1),
        den,
        out=np.full(len(points), np.inf),
        where=np.abs(den) > 1e-9,
    )
    q = origin + t[:, None] * direction - points
    with np.errstate(invalid="ignore"):
        a = np.sum(q * u, axis=1) / np.sum(u * u, axis=1)
        b = np.sum(q * v, axis=1) / np.sum(v * v, axis=1)
    indices = np.flatnonzero((t > 0) & (a > 1e-5) & (a < 1 - 1e-5) & (b > 1e-5) & (b < 1 - 1e-5))
    if not len(indices):
        return None
    indices = indices[np.argsort(t[indices])]
    nearest = indices[0]
    tied = [i for i in indices if abs(t[i] - t[nearest]) < 2e-5]
    if len({materials[i] for i in tied}) > 1:
        return "ambiguous"
    return float(t[nearest]), materials[nearest]


def main():
    archive = zipfile.ZipFile(ROOT / "models/archive/legendary-pack-2.0.15.zip")
    random = np.random.default_rng(20316)
    report = {}
    for key in IDS:
        source = json.loads(archive.read(f"assets/bigcasares/models/item/{key}.json"))["elements"]
        target = json.loads((ROOT / f"pack/assets/bigcasares/models/item/{key}.json").read_text())[
            "elements"
        ]
        pixels = np.array(
            Image.open(
                io.BytesIO(archive.read(f"assets/bigcasares/textures/item/user/{key}.png"))
            ).convert("RGBA")
        )
        old, old_m = quads(source, pixels)
        new, new_m = quads(target, pixels)
        passed = skipped = 0
        for index in range(1500):
            center = np.array([8.0, 8.0, 8.0])
            vector = random.normal(size=3)
            origin = center + vector / np.linalg.norm(vector) * 32
            face = random.integers(len(old[0]))
            aim = (
                old[0][face]
                + old[1][face] * random.uniform(0.1, 0.9)
                + old[2][face] * random.uniform(0.1, 0.9)
            )
            direction = aim - origin
            direction /= np.linalg.norm(direction)
            a = hit(old, old_m, origin, direction)
            b = hit(new, new_m, origin, direction)
            if a == "ambiguous" or b == "ambiguous":
                skipped += 1
                continue
            if a is None or b is None:
                assert a == b, (key, index, a, b)
                passed += 1
                continue
            assert abs(a[0] - b[0]) < 1e-4 and a[1] == b[1], (key, index, a, b)
            passed += 1
        report[key] = {"verifiedRays": passed, "skippedCoplanarAmbiguities": skipped}
        print(key, report[key])
    (ROOT / "docs/model-surface-check-2.0.16.json").write_text(json.dumps(report, indent=2) + "\n")


if __name__ == "__main__":
    main()
