#!/usr/bin/env python3
"""Editable reference study: real bevels, tapered blades and faceted gems.

This writes geometry and a paint specification, never image pixels. The review
is separate from the production resource pack until the user approves it.
"""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'models/reference-study'
COLORS = {
    'black': '#101114', 'obsidian': '#19191e', 'charcoal': '#282a2e',
    'graphite': '#3c3d43', 'gunmetal': '#50535c', 'steel': '#737a89',
    'silver': '#a3adbe', 'lightsteel': '#d0d7e2', 'ivory': '#e5e2bd', 'white': '#f0eee0',
    'blood_dark': '#440c13', 'blood': '#86101c', 'blood_light': '#b52738',
    'ruby': '#d62737', 'ruby_light': '#f36165',
    'bronze': '#66502e', 'brass': '#967038', 'gold': '#c39534', 'gold_light': '#e7bd59',
    'wood_dark': '#46321f', 'wood': '#735531', 'tan': '#b19a65', 'leather': '#292929',
    'cyan_dark': '#00617b', 'cyan': '#00b8d9', 'ice': '#24d7e8', 'ice_light': '#a5f5f0',
    'purple_dark': '#301a47', 'purple': '#7531b4', 'amethyst': '#b14bfd', 'violet': '#edcfff',
    'shadow': '#202329',
}
MODELS = {}


def area(points):
    return sum(ax * by - bx * ay for (ax, ay), (bx, by) in zip(points, points[1:] + points[:1])) / 2


def triangulate(points):
    """Ear clipping keeps the intended voids in hooked and curved silhouettes."""
    remaining = list(range(len(points))); result = []
    def cross(a, b, c):
        return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
    def inside(p, a, b, c):
        return min(cross(a, b, p), cross(b, c, p), cross(c, a, p)) >= -1e-8
    while len(remaining) > 3:
        for i, current in enumerate(remaining):
            previous, following = remaining[i - 1], remaining[(i + 1) % len(remaining)]
            a, b, c = points[previous], points[current], points[following]
            if cross(a, b, c) <= 1e-8:
                continue
            if any(inside(points[j], a, b, c) for j in remaining if j not in (previous, current, following)):
                continue
            result.append([previous, current, following]);remaining.pop(i);break
        else:
            raise ValueError(('Invalid polygon', points, remaining))
    return result + [remaining]


def inset_polygon(points, amount):
    result = []
    for i, (x, y) in enumerate(points):
        px, py = points[i - 1];nx, ny = points[(i + 1) % len(points)]
        ax, ay, bx, by = x - px, y - py, nx - x, ny - y
        la, lb = math.hypot(ax, ay), math.hypot(bx, by)
        n1, n2 = (-ay / la, ax / la), (-by / lb, bx / lb)
        divisor = 1 + n1[0] * n2[0] + n1[1] * n2[1]
        if abs(divisor) < .001:
            ox, oy = n1[0] * amount, n1[1] * amount
        else:
            ox, oy = (n1[0] + n2[0]) / divisor * amount, (n1[1] + n2[1]) / divisor * amount
        # Very acute tips keep their silhouette instead of extending an inset
        # several blocks beyond the original model.
        distance = math.hypot(ox, oy)
        if distance > amount * 3.5:
            ox *= amount * 3.5 / distance;oy *= amount * 3.5 / distance
        result.append((x + ox, y + oy))
    return result


def octagon(x1, y1, x2, y2, cut):
    return [(x1 + cut, y1), (x2 - cut, y1), (x2, y1 + cut), (x2, y2 - cut),
            (x2 - cut, y2), (x1 + cut, y2), (x1, y2 - cut), (x1, y1 + cut)]


class Weapon:
    def __init__(self, name):
        self.name = name;self.meshes = []

    def mesh(self, name, vertices, faces):
        assert all(math.isfinite(v) for p in vertices for v in p)
        assert all(f['material'] in COLORS for f in faces)
        # Only the convex cylinder caps and crystal tables have more than four
        # vertices; keeping source faces triangular/quadrilateral is portable.
        faces = [piece for face in faces for piece in (
            [face] if len(face['vertices']) <= 4 else [
                {'vertices': [face['vertices'][0], face['vertices'][i], face['vertices'][i + 1]],
                 'material': face['material']} for i in range(1, len(face['vertices']) - 1)])]
        self.meshes.append({'name': name, 'vertices': vertices, 'faces': faces})

    def bevel(self, name, points, material, edge, bevel_light=None, bevel_dark=None,
              depth=3, bevel=.45, bevel_depth=.5, z=8):
        if area(points) < 0:points = list(reversed(points))
        inner = inset_polygon(points, bevel)
        count = len(points)
        vertices = [[x, y, zz] for ring, zz in (
            (inner, z - depth / 2), (points, z - depth / 2 + bevel_depth),
            (points, z + depth / 2 - bevel_depth), (inner, z + depth / 2)) for x, y in ring]
        faces = []
        for tri in triangulate(inner):
            faces.append({'vertices': list(reversed(tri)), 'material': material})
            faces.append({'vertices': [3 * count + i for i in tri], 'material': material})
        center = sum(x for x, _ in points) / count
        for i in range(count):
            j = (i + 1) % count
            bevel_material = (bevel_light or material) if (points[i][0] + points[j][0]) / 2 < center else (bevel_dark or edge)
            for layer, mat in ((0, bevel_material), (1, edge), (2, bevel_material)):
                faces.append({'vertices': [layer * count + i, layer * count + j,
                                           (layer + 1) * count + j, (layer + 1) * count + i], 'material': mat})
        self.mesh(name, vertices, faces)

    def cylinder(self, name, bottom, top, radius, material, side=None, x=8, z=8, sides=8):
        vertices = [[x + radius * math.cos(math.pi / 8 + i * 2 * math.pi / sides), y,
                     z + radius * math.sin(math.pi / 8 + i * 2 * math.pi / sides)]
                    for y in (bottom, top) for i in range(sides)]
        faces = [{'vertices': list(range(sides)), 'material': material},
                 {'vertices': list(reversed(range(sides, 2 * sides))), 'material': material}]
        for i in range(sides):
            j = (i + 1) % sides
            faces.append({'vertices': [i, sides + i, sides + j, j],
                          'material': side if side and i in (0, 1, 4, 5) else material})
        # Polygon caps are triangulated in the exporter / Blockbench project.
        self.mesh(name, vertices, faces)

    def gem(self, name, x, y, width, height, material='ruby', light='ruby_light', dark='blood',
            z=10.2, rectangular=False):
        if rectangular:
            base = octagon(x - width / 2, y - height / 2, x + width / 2, y + height / 2, min(width, height) * .13)
        else:
            base = [(x, y - height / 2), (x + width / 2, y), (x, y + height / 2), (x - width / 2, y)]
        for sign in (-1, 1):
            front = 8 + sign * (z - 8)
            ring = [(x + (a - x) * .62, y + (b - y) * .66) for a, b in base]
            count = len(base)
            vertices = [[a, b, front] for a, b in base] + [[a, b, front + sign * .75] for a, b in ring]
            faces = [{'vertices': list(range(count, 2 * count)), 'material': material}]
            for i in range(count):
                j = (i + 1) % count
                faces.append({'vertices': [i, j, count + j, count + i],
                              'material': light if i >= count // 2 else dark})
            if sign < 0:
                for face in faces:face['vertices'].reverse()
            self.mesh(name + ('/front' if sign > 0 else '/back'), vertices, faces)

    def plate_pair(self, name, points, material, edge=None, z=10, depth=.5, bevel=.18):
        for sign in (-1, 1):
            self.bevel(name + ('/front' if sign > 0 else '/back'), points,
                       material, edge or material, material, edge or material,
                       depth=depth, bevel=bevel, bevel_depth=min(depth * .35, .2), z=8 + sign * (z - 8))

    def beam(self, name, start, end, width, depth, material, edge, bevel=.18):
        """A solid beveled strut following a line, with actual side surfaces."""
        dx, dy = end[0] - start[0], end[1] - start[1]
        length = math.hypot(dx, dy)
        first = len(self.meshes)
        self.bevel(name, [(-width / 2, 0), (width / 2, 0),
                          (width / 2, length), (-width / 2, length)],
                   material, edge, material, edge, depth=depth,
                   bevel=min(bevel, width / 4, length / 4), bevel_depth=min(.35, depth / 4))
        for mesh in self.meshes[first:]:
            for vertex in mesh['vertices']:
                x, y = vertex[:2]
                vertex[0] = start[0] + x * dy / length + y * dx / length
                vertex[1] = start[1] - x * dx / length + y * dy / length

    def rivet_pair(self, name, x, y, radius, material='silver', z=11):
        points = [(x + radius * math.cos(i * math.pi / 4),
                   y + radius * math.sin(i * math.pi / 4)) for i in range(8)]
        self.plate_pair(name, points, material, 'graphite' if material == 'silver' else 'brass',
                        z=z, depth=.65, bevel=.08)

    def lens_blade(self, name, stations, left, right, edge, edge_right=None):
        """Closed diamond-section blade; the broad surfaces meet at a ridge."""
        profile = [(-1, 0), (-.72, -.7), (0, -1), (.72, -.7),
                   (1, 0), (.72, .7), (0, 1), (-.72, .7)]
        vertices = [[8 + x * width, y, 8 + z * thickness]
                    for y, width, thickness in stations for x, z in profile]
        faces = []
        for row in range(len(stations) - 1):
            for i in range(8):
                j = (i + 1) % 8
                center_x = (profile[i][0] + profile[j][0]) / 2
                mat = ((edge_right or edge) if center_x > .8 else edge) if abs(center_x) > .8 else (left if center_x < 0 else right)
                faces.append({'vertices': [row * 8 + i, (row + 1) * 8 + i,
                                           (row + 1) * 8 + j, row * 8 + j], 'material': mat})
        faces.extend([{'vertices': list(range(8)), 'material': edge},
                      {'vertices': list(reversed(range((len(stations) - 1) * 8, len(stations) * 8))),
                       'material': edge}])
        self.mesh(name, vertices, faces)

    def knife_blade(self, name, stations, tip):
        """Single-edge shadow knife: a thick spine and a thin black grind.

        The edge is genuinely thinner than the body, on both sides, and the
        final ring closes into one point instead of a blunt extruded end cap.
        """
        vertices = []
        for y, cutting_x, spine_x, thickness in stations:
            width = spine_x - cutting_x
            grind = min(1.65, width * .28)
            fine = min(.18, width * .04)
            lip = min(.025, thickness * .02)
            shoulder = min(.30, thickness * .2)
            profile = [
                (cutting_x, -lip), (cutting_x + fine, -shoulder),
                (cutting_x + grind, -thickness), (cutting_x + width * .62, -thickness),
                (spine_x, -thickness * .78), (spine_x, thickness * .78),
                (cutting_x + width * .62, thickness), (cutting_x + grind, thickness),
                (cutting_x + fine, shoulder), (cutting_x, lip)]
            vertices.extend([[x, y, 8 + z] for x, z in profile])
        bands = 10
        materials = ['black', 'shadow', 'obsidian', 'charcoal', 'black',
                     'charcoal', 'obsidian', 'shadow', 'black', 'black']
        faces = []
        for row in range(len(stations) - 1):
            for i in range(bands):
                j = (i + 1) % bands
                faces.append({'vertices': [row * bands + i, (row + 1) * bands + i,
                                           (row + 1) * bands + j, row * bands + j],
                              'material': materials[i]})
        faces.append({'vertices': list(range(bands)), 'material': 'black'})
        tip_index = len(vertices)
        vertices.append(list(tip))
        last = (len(stations) - 1) * bands
        for i in range(bands):
            faces.append({'vertices': [last + i, tip_index, last + (i + 1) % bands],
                          'material': materials[i]})
        self.mesh(name, vertices, faces)

    def lathe(self, name, rings, material, side=None, sides=12, phase=0):
        """Solid faceted body around the straight handle's Y axis."""
        vertices = [[8 + radius * math.cos(phase + i * 2 * math.pi / sides), y,
                     8 + radius * math.sin(phase + i * 2 * math.pi / sides)]
                    for y, radius in rings for i in range(sides)]
        faces = [{'vertices': list(range(sides)), 'material': material},
                 {'vertices': list(reversed(range((len(rings) - 1) * sides, len(rings) * sides))),
                  'material': material}]
        for row in range(len(rings) - 1):
            for i in range(sides):
                j = (i + 1) % sides
                faces.append({'vertices': [row * sides + i, (row + 1) * sides + i,
                                           (row + 1) * sides + j, row * sides + j],
                              'material': side if side and i % 3 == 0 else material})
        self.mesh(name, vertices, faces)

    def wind_fin(self, name, phase):
        """One blunt, closed steel flange swept around the vortex head."""
        rings = [(16.2, 2.8, 1, .9), (17.3, 4, 1.3, 1.1),
                 (19.2, 5.6, 1.8, 1.25), (22, 6.6, 2.05, 1.2),
                 (24.8, 6.9, 2.1, 1.15), (27.1, 5.8, 1.85, 1.1),
                 (29.2, 4.1, 1.45, 1.05), (30.6, 2.8, 1.15, .85), (31.2, 2.3, .8, .7)]
        profile = [(-.65, -1), (.65, -1), (1, -.6), (1, .6),
                   (.65, 1), (-.65, 1), (-1, .6), (-1, -.6)]
        vertices = []
        for y, radius, half_width, half_depth in rings:
            angle = phase + math.radians(70 * (y - 16.2) / 15)
            c, s = math.cos(angle), math.sin(angle)
            for radial, tangent in profile:
                r = radius + radial * half_depth
                t = tangent * half_width
                vertices.append([8 + r * c - t * s, y, 8 + r * s + t * c])
        materials = ['gunmetal', 'silver', 'lightsteel', 'ice_light',
                     'silver', 'steel', 'gunmetal', 'steel']
        faces = [{'vertices': list(range(8)), 'material': 'gunmetal'},
                 {'vertices': list(reversed(range((len(rings) - 1) * 8, len(rings) * 8))),
                  'material': 'silver'}]
        for row in range(len(rings) - 1):
            for i in range(8):
                j = (i + 1) % 8
                a, b, c, d = row * 8 + i, (row + 1) * 8 + i, (row + 1) * 8 + j, row * 8 + j
                # Twisting flanges have intentionally triangulated planar facets.
                faces.extend([{'vertices': [a, b, c], 'material': materials[i]},
                              {'vertices': [a, c, d], 'material': materials[i]}])
        self.mesh(name, vertices, faces)

    def wind_wrap(self):
        vertices = [];steps = 64
        profile = [(-.075, -.16), (.075, -.16), (.075, .16), (-.075, .16)]
        for i in range(steps + 1):
            t = i / steps;angle = t * 4 * math.pi
            for dr, dy in profile:
                radius = 1.31 + dr
                vertices.append([8 + radius * math.cos(angle), -.5 + 14 * t + dy,
                                 8 + radius * math.sin(angle)])
        faces = [{'vertices': [3, 2, 1, 0], 'material': 'ice'},
                 {'vertices': list(range(steps * 4, steps * 4 + 4)), 'material': 'ice'}]
        for row in range(steps):
            for i in range(4):
                j = (i + 1) % 4
                faces.append({'vertices': [row * 4 + i, row * 4 + j,
                                           (row + 1) * 4 + j, (row + 1) * 4 + i],
                              'material': 'ice_light' if i == 1 else 'ice'})
        self.mesh('Mango / hilo de viento helicoidal', vertices, faces)

    def crescent(self, name, side):
        """Curved axe wedge: heavy root, raised spine and a thin cutting edge."""
        count, bands = 13, 5
        vertices = []
        for sign in (1, -1):
            for i in range(count):
                t = i / (count - 1)
                angle = math.radians(122 + 116 * t)
                root = 5.5 + 2.9 * abs(2 * t - 1) ** 2.4
                radii = [root, root + .55, 9.4, 11.4, 12]
                thickness = [1.5, 2.8, 2.05, .95, .10]
                tip_taper = .65 + .35 * math.sin(math.pi * t)
                for radius, half_depth in zip(radii, thickness):
                    x = 8 + radius * math.cos(angle)
                    vertices.append([16 - x if side else x, 21.5 + radius * math.sin(angle),
                                     8 + sign * half_depth * tip_taper])
        faces = []
        materials = ['bronze', 'gold', 'ivory', 'gold_light']
        half = count * bands
        for back in (False, True):
            offset = half if back else 0
            for i in range(count - 1):
                for j in range(bands - 1):
                    face = [offset + i * bands + j, offset + i * bands + j + 1,
                            offset + (i + 1) * bands + j + 1, offset + (i + 1) * bands + j]
                    if back ^ side:face.reverse()
                    faces.append({'vertices': face, 'material': materials[j]})
        for i in range(count - 1):
            for j in (0, bands - 1):
                face = [i * bands + j, (i + 1) * bands + j,
                        half + (i + 1) * bands + j, half + i * bands + j]
                if (j == bands - 1) ^ side:face.reverse()
                faces.append({'vertices': face, 'material': 'bronze' if j == 0 else 'gold_light'})
        for i in (0, count - 1):
            for j in range(bands - 1):
                face = [i * bands + j, half + i * bands + j,
                        half + i * bands + j + 1, i * bands + j + 1]
                if (i > 0) ^ side:face.reverse()
                faces.append({'vertices': face, 'material': 'brass'})
        self.mesh(name, vertices, faces)

    def finish(self):
        MODELS[self.name] = {'id': self.name, 'meshes': self.meshes}


def bloodlust():
    w = Weapon('bloodlust')
    w.cylinder('Empuñadura / madera oscura', -12.5, -1, 1.35, 'wood', 'wood_dark')
    for i in range(5):
        y = -11.6 + i * 2.25
        w.cylinder(f'Empuñadura / venda beige {i}', y, y + 1.1, 1.45, 'tan', 'brass')
    w.bevel('Pomo / bronce tallado', octagon(5.5, -16, 10.5, -11.8, .65),
            'brass', 'bronze', 'gold', 'wood_dark', depth=5, bevel=.4, bevel_depth=.5)
    w.gem('Pomo / rubí', 8, -14, 1.7, 2, z=10.55)
    guard = [(-1.5, -.1), (1, -2.1), (5, -2.3), (8, -3), (11, -2.3), (15, -2.1),
             (17.5, -.1), (18, 2.2), (16.2, 3), (15.1, .7), (11.5, 0), (4.5, 0), (.9, .7), (-.2, 3), (-2, 2.2)]
    guard = [(8 + (x - 8) * .73, y) for x, y in guard]
    w.bevel('Guarda / curva de bronce y oro', guard, 'brass', 'bronze', 'gold_light', 'bronze',
            depth=4.3, bevel=.45, bevel_depth=.6)
    w.lens_blade('Hoja / acero carmesí con sección romboidal',
                 [(-.4, 3.7, 1.65), (22.4, 3.7, 2.2), (25.5, 4.7, 2.2), (32, .035, .04)],
                 'blood_light', 'blood', 'blood_dark')
    ridge = [(7.55, .5), (8.45, .5), (8.45, 23), (8, 26.5), (7.55, 23)]
    w.plate_pair('Hoja / nervio de rubí', ridge, 'ruby', 'blood', z=10, depth=.5, bevel=.16)
    w.gem('Hoja / cristal facetado en la punta', 8, 25.8, 3.8, 6.4, z=10.15)
    w.bevel('Guarda / montura de rubí', [(8, -3.2), (10.6, -.6), (8, 2), (5.4, -.6)],
            'gold', 'bronze', 'gold_light', 'brass', depth=5, bevel=.35, bevel_depth=.35)
    w.gem('Guarda / rubí central', 8, -.6, 2.5, 3, z=10.55)
    w.finish()


def shadow():
    w = Weapon('shadow_blade')
    w.bevel('Empuñadura / mango corto de cuchillo', octagon(6.2, -9.4, 9.8, 1.1, .3),
            'charcoal', 'black', 'graphite', 'black', depth=3.3, bevel=.22, bevel_depth=.25)
    for i, y in enumerate((-8, -5.9, -3.8, -1.7)):
        w.bevel(f'Empuñadura / vuelta de cuero {i}', octagon(6.05, y, 9.95, y + .9, .18),
                'obsidian', 'black', 'charcoal', 'black', depth=3.65, bevel=.12, bevel_depth=.15)
    w.bevel('Pomo / remate pequeño de acero', octagon(6.35, -11.5, 9.65, -9.1, .3),
            'gunmetal', 'graphite', 'steel', 'charcoal', depth=3.6, bevel=.25, bevel_depth=.35)
    guard = [(2.7, .2), (13.3, .2), (13.3, 2.5), (11.5, 3),
             (11.2, 2), (4.8, 2), (4.5, 3), (2.7, 2.5)]
    w.bevel('Guarda / cruceta de acero de la nueva foto', guard, 'steel', 'gunmetal', 'silver', 'graphite',
            depth=4.4, bevel=.3, bevel_depth=.45)
    w.knife_blade('Hoja / cuchillo de filo liso negro de sombra',
                  [(2, 4, 11.6, 1.9), (4, 3.8, 11.8, 2.05),
                   (12.5, 3.8, 11.6, 2), (15.8, 5, 10.9, 1.4),
                   (17.7, 7.2, 10, .75)], (9.2, 19, 8))
    w.rivet_pair('Guarda / remache de hierro', 8, 1.1, .4, z=10.4)
    w.finish()


def frost():
    w = Weapon('frost_scythe')
    w.beam('Asta / tubo de acero recto', (8, -12), (8, 21.5), 2.6, 2.8, 'silver', 'gunmetal', .18)
    w.plate_pair('Asta / reflejo longitudinal de acero', [(7, -9), (7.7, -9), (7.7, 17), (7, 17)],
                 'lightsteel', 'steel', z=9.5, depth=.18, bevel=.06)
    for i, y in enumerate((-6.5, -3.3, -.1, 17.5)):
        w.bevel(f'Asta / abrazadera negra {i}', octagon(6.45, y, 9.55, y + 1.8, .15),
                'black', 'charcoal', 'graphite', 'black', depth=3.4, bevel=.12, bevel_depth=.2)
        w.beam(f'Asta / banda de hielo {i}', (8, y + .3), (8, y + 1.2), 3.15, 3.5,
               'cyan', 'cyan_dark', .1)
    w.bevel('Pomo / montura negra de la nueva referencia', octagon(5.7, -12.6, 10.3, -8.7, .3),
            'black', 'obsidian', 'graphite', 'black', depth=4.8, bevel=.25, bevel_depth=.4)
    w.gem('Pomo / cristal cian cuadrado', 8, -10.6, 1.9, 1.9,
          'ice', 'ice_light', 'cyan', z=10.5, rectangular=True)
    w.bevel('Pomo / punta de acero', [(6, -12), (9.7, -12), (9.1, -16), (7.9, -14.4), (6.1, -14.6)],
            'lightsteel', 'steel', 'white', 'gunmetal', depth=3.3, bevel=.17, bevel_depth=.35)
    w.beam('Pomo / aleta lateral cian', (10.1, -11.6), (10.5, -14.4), 1.35, 3.7,
           'cyan', 'cyan_dark', .12)
    joint = [(4.8, 19), (10.9, 19), (12.3, 22.2), (10.6, 26), (5.5, 26), (3.9, 22.5)]
    w.bevel('Cabeza / articulación negra maciza', joint, 'black', 'obsidian', 'charcoal', 'black',
            depth=7, bevel=.4, bevel_depth=.7)
    hook = [(5.8, 27.4), (-10.7, 27.4), (-11.3, 22.4),
            (-9.2, 22.4), (-8.8, 24.8), (-6.6, 24.9), (-6.2, 23.2),
            (-4.5, 25.2), (-2.5, 24.3), (-1.4, 22.1), (.1, 22.7), (1.8, 20), (3.8, 20.3), (2.3, 24)]
    w.bevel('Hoja / garra plateada de lomo horizontal', hook,
            'silver', 'gunmetal', 'lightsteel', 'steel', depth=5.7, bevel=.4, bevel_depth=.8)
    spine = [(-9.8, 26.85), (4.7, 26.85), (4.7, 25.5), (-9.6, 25.5)]
    w.plate_pair('Hoja / refuerzo horizontal en relieve', spine, 'steel', 'gunmetal',
                 z=11.1, depth=.75, bevel=.2)
    w.beam('Ala / larguero superior negro', (10.6, 25.2), (23.6, 25.2), 2, 5.4,
           'black', 'charcoal', .18)
    w.beam('Ala / diagonal negra de cierre', (23.6, 25.2), (17.5, 18.3), 1.8, 5.4,
           'black', 'charcoal', .18)
    w.beam('Ala / borde inferior negro', (17.5, 18.3), (10.5, 19.2), 1.9, 5.4,
           'black', 'charcoal', .18)
    w.beam('Ala / filo superior blanco', (11.5, 26.3), (18.9, 26.3), .75, 5.7,
           'white', 'ice_light', .1)
    w.beam('Ala / extensión cian superior', (18.9, 26.3), (22.5, 26.3), .8, 5.7,
           'ice', 'cyan', .1)
    wing = [(11.2, 19.6), (18, 19.4), (22.9, 23.4), (25.8, 23.3),
            (23.4, 20.6), (20.6, 17.7), (15.6, 16.9), (10.9, 17.6)]
    w.bevel('Ala / aleta de hielo ancha y gruesa', wing, 'cyan', 'cyan_dark', 'ice', 'cyan',
            depth=4.5, bevel=.25, bevel_depth=.7)
    w.gem('Cabeza / núcleo de hielo cuadrado', 8, 22.7, 3, 3.2,
          'ice', 'ice_light', 'cyan', z=11.7, rectangular=True)
    w.rivet_pair('Cabeza / remache del larguero', 12, 24.8, .38, z=10.9)
    w.finish()


def wind():
    w = Weapon('windweaver')
    w.cylinder('Mango / asta de acero recta', -13, 16.4, 1.15, 'gunmetal', 'charcoal')
    w.cylinder('Mango / empuñadura negra', -12.5, -2.5, 1.35, 'black', 'charcoal')
    for i, y in enumerate((-11.2, -8.7, -6.2, -3.7)):
        w.cylinder(f'Mango / vuelta de cuero {i}', y, y + .65, 1.43, 'graphite', 'black')
    w.wind_wrap()
    w.lathe('Cuello / unión de acero con el núcleo',
            [(12.8, 1.35), (14.6, 1.8), (16, 2.9), (17, 3.2)],
            'silver', 'steel', sides=12, phase=math.pi / 12)
    w.bevel('Pomo / remate de acero oscuro', octagon(5.9, -16, 10.1, -12.5, .6),
            'gunmetal', 'charcoal', 'silver', 'steel', depth=4.2, bevel=.45, bevel_depth=.5)
    w.gem('Pomo / cristal de aire', 8, -14.3, 1.4, 1.8,
          'ice_light', 'lightsteel', 'ice', z=10.15)

    # A compact mace head follows one upward swirling motion. Its six steel
    # flanges are solid, blunt and joined to the lower hub and upper crown.
    w.lathe('Cabeza / cuerpo interior de acero',
            [(16.1, 2.3), (17.5, 3.8), (20, 4.35), (26.2, 4.2),
             (29.5, 2.8), (30.7, 2)], 'gunmetal', 'charcoal', sides=12)
    w.lathe('Núcleo / cristal facetado de viento',
            [(19.6, 4.4), (21, 4.72), (24.3, 4.72), (26.6, 4.3), (28, 3.6)],
            'ice_light', 'ice', sides=12)
    for i in range(6):
        w.wind_fin(f'Aletas / espiral de acero {i}', math.radians(15 + i * 60))
    w.lathe('Cabeza / corona superior biselada',
            [(30.1, 2.8), (30.7, 3.2), (31.2, 2.65), (31.7, 1.5)],
            'silver', 'lightsteel', sides=12, phase=math.pi / 12)
    w.lathe('Cabeza / anillo inferior del vórtice',
            [(15.6, 2.6), (16.2, 3.35), (17, 3.25)],
            'steel', 'silver', sides=12)
    w.finish()


def earth():
    w = Weapon('earthbreaker')
    w.cylinder('Asta / madera sólida', -13.5, 28.3, 1.45, 'wood', 'wood_dark')
    w.cylinder('Asta / agarre de cuero grueso', -12.5, -2.8, 1.6, 'leather', 'black')
    for i, y in enumerate((-11.3, -8.7, -6.1, -3.5)):
        w.cylinder(f'Asta / venda de cuero {i}', y, y + .6, 1.65, 'charcoal', 'graphite')
    w.bevel('Pomo / remate de oro macizo', octagon(5.3, -16, 10.7, -12.2, .55),
            'gold', 'brass', 'gold_light', 'bronze', depth=5.4, bevel=.45, bevel_depth=.65)
    w.cylinder('Cabeza / collar inferior de marfil', 13.1, 14.8, 2, 'ivory', 'gold_light')
    w.bevel('Cabeza / cubo central de bronce', octagon(4.2, 15.1, 11.8, 28.7, .8),
            'brass', 'bronze', 'gold', 'bronze', depth=8.4, bevel=.65, bevel_depth=.8)
    w.beam('Cabeza / nervio central elevado', (8, 16.7), (8, 27.6), 2.05, 9.7,
           'gold_light', 'gold', .2)
    for side in (False, True):
        w.crescent(f'Hojas / filo curvo en cuña {side}', side)
        for i, y in enumerate((18, 24.6)):
            start = (11.2, y) if side else (4.8, y)
            end = (14.5, y + .4) if side else (1.5, y + .4)
            w.beam(f'Refuerzos / raíz maciza de hoja {side} {i}', start, end,
                   2.4, 5.2, 'gold', 'bronze', .25)
        for i, y in enumerate((18.3, 25.8)):
            w.rivet_pair(f'Refuerzos / bulón de bronce {side} {i}', 10.5 if side else 5.5,
                         y, .48, 'gold_light', z=12.5)
    w.bevel('Cabeza / remate superior dorado', [(8, 28), (10.2, 30), (8, 32), (5.8, 30)],
            'gold', 'brass', 'gold_light', 'gold', depth=4, bevel=.28, bevel_depth=.5)
    w.finish()


def thunder():
    w = Weapon('thunder_spear')
    w.cylinder('Asta / madera marrón recta', -13.3, 17.5, 1.05, 'wood', 'wood_dark')
    w.cylinder('Asta / agarre negro', -12.5, -1, 1.2, 'leather', 'black')
    for i, y in enumerate((-13.2, -.5, 7.1, 15)):
        w.cylinder(f'Asta / abrazadera dorada {i}', y, y + 1.05, 1.52, 'gold', 'brass')
    w.bevel('Pomo / punta dorada', [(6.6, -13.8), (9.4, -13.8), (9, -15), (8, -16), (7, -15)],
            'gold', 'brass', 'gold_light', 'brass', depth=2.8, bevel=.18, bevel_depth=.3)
    w.lens_blade('Punta / cristal con nervio y sección romboidal',
                 [(17.9, 1.5, .7), (20, 3.4, 1.9), (24.7, 3.4, 2.1), (32, .035, .04)],
                 'ice', 'cyan', 'ivory', edge_right='gold_light')
    socket = [(5.8, 15.5), (10.2, 15.5), (11, 17), (10.2, 18.2), (5.8, 18.2), (5, 17)]
    w.bevel('Cabeza / casquillo de oro', socket, 'gold', 'brass', 'gold_light', 'bronze',
            depth=4, bevel=.3, bevel_depth=.45)
    w.gem('Cabeza / gema cian', 8, 17.2, 2, 2.4,
          'cyan', 'ice_light', 'cyan_dark', z=10.2)
    w.finish()


if __name__ == '__main__':
    ROOT.mkdir(parents=True, exist_ok=True)
    for generate in (bloodlust, shadow, frost, wind, earth, thunder):generate()
    study = {'colors': COLORS, 'models': MODELS}
    overrides = ROOT / 'model-overrides.json'
    if overrides.exists():
        # Supplied projects are authoritative; generated meshes for these IDs
        # are historical references and must not overwrite the uploaded model.
        study['sourceOverrides'] = json.loads(overrides.read_text())
    (ROOT / 'geometry.json').write_text(json.dumps(study, indent=2) + '\n')
    for name, model in MODELS.items():
        external = study.get('sourceOverrides', {}).get(name)
        if external:
            print(name, 'uses uploaded project', external['project'], '(generated meshes are archived references)')
        else:
            print(name, len(model['meshes']), 'meshes;', sum(len(m['faces']) for m in model['meshes']), 'faces')
