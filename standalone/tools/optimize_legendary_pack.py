#!/usr/bin/env python3
"""Conservative surface meshing of opaque, solid-color native weapon cuboids.

Original .bbmodel files and PNG bytes are never edited. Coplanar faces merge only
when their complete material matches. Geometry with patterned/translucent faces
falls back unchanged. Sparse poses are held for multiple 20 Hz server ticks.
"""
from __future__ import annotations

from collections import defaultdict
from copy import deepcopy
from pathlib import Path
import argparse
import hashlib
import io
import json
import math
import zipfile

import numpy as np
from PIL import Image

IDS = ("bloodlust", "shadow_blade", "windweaver", "earthbreaker", "thunder_spear")
SIDES = {
    "west": (0, -1),
    "east": (0, 1),
    "down": (1, -1),
    "up": (1, 1),
    "north": (2, -1),
    "south": (2, 1),
}
ANCHOR = np.array([8.0, 8.0, 8.0])


def rotation(angles):
    x, y, z = np.radians(angles)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    return rz @ ry @ rx


def encoded(value):
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def material_is_opaque_flat(face, pixels):
    uv = face.get("uv", [0, 0, 16, 16])
    # Supplied palettes contain 4x4 flat swatches; sample the entire UV footprint.
    x0, y0, x1, y1 = uv
    region = pixels[
        max(0, math.floor(min(y0, y1))) : min(16, math.ceil(max(y0, y1))),
        max(0, math.floor(min(x0, x1))) : min(16, math.ceil(max(x0, x1))),
    ]
    return region.size > 0 and np.all(region[..., 3] == 255) and np.all(region == region[0, 0])


def merge_boxes(parts, pixels):
    result = deepcopy(parts)
    flat = {
        encoded(f): material_is_opaque_flat(f, pixels) for e in result for f in e["faces"].values()
    }
    previous = -1
    while previous != len(result):
        previous = len(result)
        for axis in range(3):
            buckets = defaultdict(list)
            untouched = []
            for part in result:
                if not all(flat[encoded(f)] for f in part["faces"].values()):
                    untouched.append(part)
                    continue
                others = [n for n in range(3) if n != axis]
                sides = [side for side, (ax, _) in SIDES.items() if ax != axis]
                key = tuple(round(part[edge][n], 6) for n in others for edge in ("from", "to")) + (
                    part.get("shade", True),
                    part.get("light_emission", 0),
                    tuple(encoded(part["faces"].get(side)) for side in sides),
                )
                buckets[key].append(part)
            result = untouched
            for bucket in buckets.values():
                bucket.sort(key=lambda e: e["from"][axis])
                current = deepcopy(bucket[0])
                for part in bucket[1:]:
                    if abs(part["from"][axis] - current["to"][axis]) > 1e-6:
                        result.append(current)
                        current = deepcopy(part)
                        continue
                    if part["to"][axis] > current["to"][axis]:
                        current["to"][axis] = part["to"][axis]
                        side = next(
                            side for side, (ax, sign) in SIDES.items() if ax == axis and sign > 0
                        )
                        face = part["faces"].get(side)
                        if face is None:
                            current["faces"].pop(side, None)
                        else:
                            current["faces"][side] = deepcopy(face)
                result.append(current)
    return result


def optimize_group(parts, pixels):
    if len(parts) < 2:
        return deepcopy(parts), {"hiddenFaces": 0, "mergedBoxes": 0}
    angles = [parts[0].get("rotation", {}).get(axis, 0) for axis in ("x", "y", "z")]
    basis = rotation(angles)
    boxes = []
    for part in parts:
        low, high = np.array(part["from"]), np.array(part["to"])
        center = (low + high) / 2
        origin = np.array(part.get("rotation", {}).get("origin", center))
        center = origin + basis @ (center - origin)
        local = basis.T @ (center - ANCHOR) + ANCHOR
        boxes.append((np.round(local - (high - low) / 2, 6), np.round(local + (high - low) / 2, 6)))
    axes = [
        np.unique(np.concatenate([np.array([lo[a], hi[a]]) for lo, hi in boxes])) for a in range(3)
    ]
    shape = tuple(len(axis) - 1 for axis in axes)
    if (
        math.prod(shape) > 8_000_000
        or min(shape) < 1
        or any(np.any(low >= high) for low, high in boxes)
    ):
        return deepcopy(parts), {"hiddenFaces": 0, "mergedBoxes": 0}
    occupied = np.zeros(shape, dtype=bool)
    ranges = []
    closed = []
    for part, (low, high) in zip(parts, boxes):
        lo = tuple(int(np.searchsorted(axes[a], low[a])) for a in range(3))
        hi = tuple(int(np.searchsorted(axes[a], high[a])) for a in range(3))
        ranges.append((lo, hi))
        solid = len(part["faces"]) == 6 and all(
            material_is_opaque_flat(f, pixels) for f in part["faces"].values()
        )
        closed.append(solid)
        if solid:
            occupied[tuple(slice(lo[a], hi[a]) for a in range(3))] = True
    culled = []
    hidden = 0
    for part, (lo, hi), (low, high), solid in zip(parts, ranges, boxes, closed):
        kept = {}
        for side, face in part["faces"].items():
            axis, sign = SIDES[side]
            others = [a for a in range(3) if a != axis]
            plane = lo[axis] if sign < 0 else hi[axis]
            near = []
            for cell in (plane - 1, plane):
                if cell < 0 or cell >= shape[axis]:
                    near.append(np.zeros(tuple(hi[a] - lo[a] for a in others), dtype=bool))
                else:
                    selection = [slice(lo[a], hi[a]) for a in range(3)]
                    selection[axis] = cell
                    near.append(occupied[tuple(selection)])
            exterior = ~near[1 if sign > 0 else 0]
            if not solid:
                exterior = ~(near[0] & near[1])
            if exterior.any():
                kept[side] = deepcopy(face)
            else:
                hidden += 1
        if kept:
            copy = deepcopy(part)
            copy.pop("rotation", None)
            copy["from"] = low.tolist()
            copy["to"] = high.tolist()
            copy["faces"] = kept
            culled.append(copy)
    merged = merge_boxes(culled, pixels)
    result = []
    for part in merged:
        low, high = np.array(part["from"]), np.array(part["to"])
        center = ANCHOR + basis @ ((low + high) / 2 - ANCHOR)
        half = (high - low) / 2
        part["from"] = np.round(center - half, 7).tolist()
        part["to"] = np.round(center + half, 7).tolist()
        if max(abs(v) for v in angles) > 1e-6:
            part["rotation"] = {
                "origin": np.round(center, 7).tolist(),
                **dict(zip(("x", "y", "z"), angles)),
            }
        result.append(part)
    assert len(result) <= len(parts)
    assert sum(len(e["faces"]) for e in result) <= sum(len(e["faces"]) for e in parts)
    return result, {"hiddenFaces": hidden, "mergedBoxes": len(culled) - len(merged)}


def optimize_elements(elements, pixels):
    # Minecraft classifies the entire sprite as translucent when any texel has
    # alpha. Preserve its complete quad order, including hidden-looking faces.
    if np.any(pixels[:, :, 3] != 255):
        faces = sum(len(e["faces"]) for e in elements)
        return deepcopy(elements), {
            "beforeElements": len(elements),
            "afterElements": len(elements),
            "beforeFaces": faces,
            "afterFaces": faces,
            "hiddenFaces": 0,
            "mergedBoxes": 0,
            "preservedTranslucentOrdering": True,
        }
    groups = defaultdict(list)
    for element in elements:
        angles = tuple(element.get("rotation", {}).get(a, 0) for a in ("x", "y", "z"))
        groups[angles].append(element)
    output = []
    hidden = 0
    merged = 0
    for parts in groups.values():
        optimized, proof = optimize_group(parts, pixels)
        output.extend(optimized)
        hidden += proof["hiddenFaces"]
        merged += proof["mergedBoxes"]
    return output, {
        "beforeElements": len(elements),
        "afterElements": len(output),
        "beforeFaces": sum(len(e["faces"]) for e in elements),
        "afterFaces": sum(len(e["faces"]) for e in output),
        "hiddenFaces": hidden,
        "mergedBoxes": merged,
    }


def element_corners(part):
    low, high = np.array(part["from"]), np.array(part["to"])
    center = (low + high) / 2
    r = rotation([part.get("rotation", {}).get(a, 0) for a in ("x", "y", "z")])
    origin = np.array(part.get("rotation", {}).get("origin", center))
    center = origin + r @ (center - origin)
    corners = np.array(
        [[x, y, z] for x in (low[0], high[0]) for y in (low[1], high[1]) for z in (low[2], high[2])]
    )
    return (corners - origin) @ r.T + origin, center, r, high - low


def dominant_transform(base, pose):
    if len(base) != len(pose) or any(
        a["faces"] != b["faces"]
        or a.get("shade", True) != b.get("shade", True)
        or a.get("light_emission", 0) != b.get("light_emission", 0)
        for a, b in zip(base, pose)
    ):
        return None
    before = [element_corners(e) for e in base]
    after = [element_corners(e) for e in pose]
    seeds = sorted(range(len(base)), key=lambda i: float(np.prod(before[i][3])), reverse=True)[:4]
    best = None
    for seed in seeds:
        _, ca, ra, sa = before[seed]
        _, cb, rb, sb = after[seed]
        if min(sa) < 1e-6:
            continue
        linear = rb @ np.diag(sb / sa) @ ra.T
        translation = cb - linear @ ca
        indices = [
            i
            for i, (a, b) in enumerate(zip(before, after))
            if np.max(np.abs(a[0] @ linear.T + translation - b[0])) < 2e-5
        ]
        if best is None or len(indices) > len(best[0]):
            best = (indices, linear, translation)
    return best if best and len(best[0]) >= len(base) * 0.65 else None


def sample_sequence(sequence, stride, loop):
    result = [sequence[(tick // stride) * stride] for tick in range(len(sequence))]
    if not loop:
        result[-1] = sequence[-1]
    return result


def rewrite_pack(archive, properties, output):
    report = {"weapons": {}, "serverTicksPerSecond": 20, "idlePoseFPS": 5, "attackPoseFPS": 10}
    lines = properties.splitlines()
    values = dict(line.split("=", 1) for line in lines if "=" in line and not line.startswith("#"))
    for key in IDS:
        prefix = "assets/bigcasares/models/item/"
        rest = json.loads(archive.read(prefix + key + ".json"))
        base = rest["elements"]
        pixels = np.array(
            Image.open(
                io.BytesIO(archive.read("assets/bigcasares/textures/item/user/" + key + ".png"))
            ).convert("RGBA")
        )
        parts, proof = optimize_elements(base, pixels)
        rest["elements"] = parts
        path = output / (prefix + key + ".json")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(encoded(rest) + "\n")
        definitions = json.loads(archive.read("assets/bigcasares/items/" + key + ".json"))
        entries = definitions["model"]["fallback"]["entries"]
        original_refs = {int(e["threshold"]): e["model"]["model"] for e in entries}
        poses = {
            index: (
                base
                if index == 0
                else json.loads(
                    archive.read("assets/bigcasares/models/" + model.split(":", 1)[1] + ".json")
                )["elements"]
            )
            for index, model in original_refs.items()
        }
        selected = {0}
        aliases = defaultdict(list)
        clip_stats = {}
        for name, value in list(values.items()):
            if not name.startswith(key + ".") or not name.endswith(".frames"):
                continue
            sequence = list(map(int, value.split(",")))
            clip_name = name[len(key) + 1 : -len(".frames")]
            loop = values[name[:-7] + ".loop"] == "true"
            sampled = sample_sequence(sequence, 4 if loop else 2, loop)
            selected.update(sampled)
            for original, new in zip(sequence, sampled):
                aliases[original].append(new)
            values[name] = ",".join(map(str, sampled))
            clip_stats[clip_name] = {
                "ticks": len(sequence),
                "originalUniquePoses": len(set(sequence)),
                "optimizedUniquePoses": len(set(sampled)),
            }
        from collections import Counter

        aliases = {
            old: Counter(candidates).most_common(1)[0][0] for old, candidates in aliases.items()
        }
        aliases.update({frame: frame for frame in selected})
        geometry = {}
        max_layers = 1
        shared_frames = 0
        verified = 0

        def mesh(elements):
            optimized, _ = optimize_elements(elements, pixels)
            payload = encoded(optimized)
            digest = hashlib.sha256(payload.encode()).hexdigest()[:16]
            name = f"item/optimized/{key}/{digest}"
            if name not in geometry:
                geometry[name] = optimized
                target = output / "assets/bigcasares/models" / (name + ".json")
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(
                    encoded({"parent": "bigcasares:item/" + key, "elements": optimized}) + "\n"
                )
            return {"type": "minecraft:model", "model": "bigcasares:" + name}

        refs = {0: {"type": "minecraft:model", "model": "bigcasares:item/" + key}}
        for frame in sorted(selected - {0}):
            pose = poses[frame]
            dominant = dominant_transform(base, pose)
            if dominant is None:
                refs[frame] = mesh(pose)
                continue
            indices, linear, translation = dominant
            matrix = np.eye(4)
            matrix[:3, :3] = linear
            matrix[:3, 3] = translation / 16
            main_indices = set(indices)
            if np.any(pixels[:, :, 3] != 255):
                runs = []
                for i in range(len(pose)):
                    is_main = i in main_indices
                    if not runs or runs[-1][0] != is_main:
                        runs.append((is_main, []))
                    runs[-1][1].append(i)
                if len(runs) > 4:
                    refs[frame] = mesh(pose)
                    continue
            else:
                runs = [(True, indices)]
                remaining = [i for i in range(len(pose)) if i not in main_indices]
                if remaining:
                    runs.append((False, remaining))
            shared_frames += 1
            verified += len(indices) * 8
            layers = []
            for is_main, run in runs:
                if is_main:
                    layer = (
                        refs[0].copy() if len(run) == len(base) else mesh([base[i] for i in run])
                    )
                    if np.max(np.abs(matrix - np.eye(4))) > 1e-7:
                        layer["transformation"] = np.round(matrix, 9).flatten().tolist()
                else:
                    layer = mesh([pose[i] for i in run])
                layers.append(layer)
            refs[frame] = (
                layers[0] if len(layers) == 1 else {"type": "minecraft:composite", "models": layers}
            )
            max_layers = max(max_layers, len(layers))
        definitions["model"]["fallback"]["entries"] = [
            {"threshold": index, "model": deepcopy(refs[aliases.get(index, 0)])}
            for index in sorted(original_refs)
        ]
        target = output / "assets/bigcasares/items" / (key + ".json")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(encoded(definitions) + "\n")
        report["weapons"][key] = {
            **proof,
            "originalAnimationPoses": len(original_refs) - 1,
            "selectedAnimationPoses": len(selected) - 1,
            "geometryMeshes": len(geometry) + 1,
            "sharedAnimationFrames": shared_frames,
            "verifiedAffineCorners": verified,
            "maximumRenderLayers": max_layers,
            "storedFacesBefore": sum(len(e["faces"]) for pose in poses.values() for e in pose),
            "storedFacesAfter": sum(len(e["faces"]) for e in parts)
            + sum(len(e["faces"]) for elements in geometry.values() for e in elements),
            "clips": clip_stats,
        }
        print(key, report["weapons"][key], flush=True)
    rendered = [
        "# Optimized supplied animation poses; holds preserve original 20 Hz clip durations."
    ] + [f"{name}={value}" for name, value in values.items()]
    return report, "\n".join(rendered) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--pack",
        type=Path,
        required=True,
        help="Original distribution ZIP containing pack and matching JAR",
    )
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument(
        "--catalog",
        type=Path,
        required=True,
        help="Destination for the matching server animation sequences",
    )
    args = parser.parse_args()
    archive = zipfile.ZipFile(args.pack)
    nested = [n for n in archive.namelist() if n.endswith("/legendary-weapons-java.zip")]
    jars = [n for n in archive.namelist() if n.endswith("/LegendaryWeapons-2.0.15.jar")]
    if len(nested) != 1 or len(jars) != 1:
        raise ValueError("Expected original 2.0.15 distribution")
    source = archive.read(nested[0])
    properties = (
        zipfile.ZipFile(io.BytesIO(archive.read(jars[0])))
        .read("legendary-animations.properties")
        .decode()
    )
    report, catalog = rewrite_pack(zipfile.ZipFile(io.BytesIO(source)), properties, args.output)
    report["sourcePackSHA256"] = hashlib.sha256(source).hexdigest()
    report["nativeTransformationFormat"] = (
        "Minecraft 26.2 minecraft:model transformation, row-major 4x4, translation in blocks"
    )
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2) + "\n")
    args.catalog.parent.mkdir(parents=True, exist_ok=True)
    args.catalog.write_text(catalog)


if __name__ == "__main__":
    main()
