#!/usr/bin/env python3
"""Generate only the eight legendary JSON models and native atlas regions.

The generated material_atlas.png is an original image_gen asset. This script
never edits pixels: Minecraft's unstitch source extracts its 128px materials.
"""
import itertools
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'pack/assets'
NAMES = ('bloodlust', 'frost_scythe', 'shadow_blade', 'hyperion',
         'windweaver', 'earthbreaker', 'void_katana', 'thunder_spear')
MATERIALS = ('outline', 'shade', 'metal', 'edge', 'highlight', 'gem', 'grip', 'gold')


class Weapon:
    def __init__(self, name):
        self.name = name
        self.elements = []

    def box(self, name, start, end, material='metal', light=0):
        assert all(-16 <= a < b <= 32 for a, b in zip(start, end)), (name, start, end)
        self.elements.append({
            'name': name, 'from': list(start), 'to': list(end),
            'shade': not bool(light), 'light_emission': light,
            'faces': {face: {'uv': [0, 0, 16, 16], 'texture': '#' + material}
                      for face in ('north', 'south', 'east', 'west', 'up', 'down')},
        })

    def front(self, name, x1, y1, x2, y2, material='metal', light=0, depth=0):
        self.box(name + '_front', (x1, y1, 6.45 - depth), (x2, y2, 6.7 - depth), material, light)
        self.box(name + '_back', (x1, y1, 9.3 + depth), (x2, y2, 9.55 + depth), material, light)

    def blade(self, name, x1, y1, x2, y2, edge='right'):
        self.box(name + '_silhouette', (x1, y1, 6.7), (x2, y2, 9.3), 'outline')
        if x2 - x1 > 1:
            self.front(name + '_steel', x1 + .35, y1 + .1, x2 - .35, y2 - .1, 'metal')
            if edge == 'right':
                self.front(name + '_edge', x2 - .9, y1 + .15, x2 - .2, y2 - .15, 'edge', 3, .3)
                self.front(name + '_glint', x2 - .4, y1 + .25, x2 - .15, y2 - .25, 'highlight', 5, .6)
            elif edge == 'left':
                self.front(name + '_edge', x1 + .2, y1 + .15, x1 + 1, y2 - .15, 'edge', 3, .3)
                self.front(name + '_glint', x1 + .15, y1 + .25, x1 + .45, y2 - .25, 'highlight', 5, .6)
            else:
                self.front(name + '_edge', x1 + .2, y1 + .15, x2 - .2, y1 + .85, 'edge', 3, .3)
                self.front(name + '_glint', x1 + .25, y1 + .12, x2 - .25, y1 + .4, 'highlight', 5, .6)

    def gem(self, name, x, y, width=3, height=4):
        self.box(name + '_bezel', (x - width / 2 - .4, y - height / 2 - .4, 5.9),
                 (x + width / 2 + .4, y + height / 2 + .4, 10.1), 'gold')
        self.front(name + '_crystal', x - width / 2, y - height / 3, x + width / 2, y + height / 3, 'gem', 13, 1)
        self.front(name + '_tips', x - width / 4, y - height / 2, x + width / 4, y + height / 2, 'gem', 13, 1.15)
        self.front(name + '_facet', x - width / 4, y, x - width / 8, y + height / 3, 'highlight', 14, 1.45)

    def handle(self, bottom=-13, top=2, width=2.5):
        self.box('leather_grip', (8 - width / 2, bottom, 8 - width / 2), (8 + width / 2, top, 8 + width / 2), 'grip')
        for i, y in enumerate(range(math.ceil(bottom + 1), math.floor(top), 3)):
            self.box('grip_wrap_' + str(i), (8 - width / 2 - .2, y, 8 - width / 2 - .2),
                     (8 + width / 2 + .2, y + .5, 8 + width / 2 + .2), 'shade')
        self.box('pommel', (6, bottom - 1.5, 6), (10, bottom + 1, 10), 'outline')
        self.front('pommel_trim', 6.4, bottom - 1.1, 9.6, bottom + .5, 'gold')

    def shaft(self):
        self.box('long_haft', (7.05, -14, 7.05), (8.95, 25, 8.95), 'outline')
        self.box('haft_inlay', (7.5, -12, 6.9), (8.5, 22, 7.05), 'metal')
        self.box('lower_grip', (6.7, -13, 6.7), (9.3, 1, 9.3), 'grip')
        self.box('haft_endcap', (6.5, -16, 6.5), (9.5, -13, 9.5), 'gold')
        for i, y in enumerate((-10, -6, -2, 4, 9, 14, 18)):
            self.box('haft_ring_' + str(i), (6.8, y, 6.8), (9.2, y + .65, 9.2), 'gold')

    def save(self):
        # Compute a safe diagonal icon size from the real cuboid bounds.
        rx, ry, rz = map(math.radians, (12, -22, -38))
        points = []
        for e in self.elements:
            for x, y, z in itertools.product(*zip(e['from'], e['to'])):
                x, y, z = x - 8, y - 8, z - 8
                x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
                x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
                y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
                points.append((x, y, z))
        low = [min(p[i] for p in points) for i in range(3)]
        high = [max(p[i] for p in points) for i in range(3)]
        scale = round(13.5 / max(high[i] - low[i] for i in (0, 1)), 4)
        gui_shift = [round(-(low[i] + high[i]) / 2 * scale, 3) for i in range(3)]
        display = {
            'gui': {'rotation': [12, -22, -38], 'translation': gui_shift, 'scale': [scale] * 3},
            'firstperson_righthand': {'rotation': [0, -85, 22], 'translation': [1, 1.5, -1.5], 'scale': [.46] * 3},
            'firstperson_lefthand': {'rotation': [0, 85, -22], 'translation': [1, 1.5, -1.5], 'scale': [.46] * 3},
            'thirdperson_righthand': {'rotation': [0, -90, 0], 'translation': [0, 2, 0], 'scale': [.42] * 3},
            'thirdperson_lefthand': {'rotation': [0, 90, 0], 'translation': [0, 2, 0], 'scale': [.42] * 3},
            'ground': {'translation': [0, 5, 0], 'scale': [.3] * 3},
            'fixed': {'scale': [.48] * 3},
        }
        model = {
            'gui_light': 'front', 'ambientocclusion': False,
            'textures': {**{m: f'bigcasares:item/legendary/{self.name}/{m}' for m in MATERIALS}, 'particle': '#metal'},
            'elements': self.elements, 'display': display,
        }
        path = ASSETS / 'bigcasares/models/item' / (self.name + '.json')
        path.write_text(json.dumps(model, ensure_ascii=False, indent=2) + '\n')
        print(self.name + ': ' + str(len(self.elements)) + ' cuboids')


def bloodlust():
    w = Weapon('bloodlust'); w.handle()
    w.box('spiked_guard', (-1, 1, 6), (17, 4, 10), 'outline')
    w.front('guard_gold', 1, 1.4, 15, 2.5, 'gold')
    for name, x1, y1, x2, y2 in [('left_horn', -3, 4, 1, 7), ('right_horn', 15, 4, 19, 7),
                                  ('left_barb', 1, 7, 4, 11), ('right_barb', 12, 7, 15, 11)]:
        w.box(name, (x1, y1, 7), (x2, y2, 9), 'shade')
        w.front(name + '_red', x1 + .3, y1 + .5, x2 - .3, y2 - .5, 'edge', 5)
    for i, band in enumerate([(3, 4, 13, 10), (3.5, 10, 12.5, 18), (4.5, 18, 11.5, 24), (5.5, 24, 10.5, 28), (7, 28, 9, 31)]):
        w.blade('crimson_blade_' + str(i), *band)
    for i, y in enumerate((8, 13, 18, 23, 26)):
        w.front('blood_rune_' + str(i), 7.5, y, 8.5, y + 1.1, 'gem', 13, .8)
    w.gem('guard_ruby', 8, 2, 3, 3.6); w.save()


def frost():
    w = Weapon('frost_scythe'); w.shaft()
    for i, band in enumerate([(6, 24, 11, 29), (0, 25, 6, 31), (-6, 23, 0, 30),
                              (-11, 19, -6, 27), (-14, 14, -10, 23), (-15, 10, -12, 17)]):
        w.blade('crescent_' + str(i), *band, edge='bottom')
    for i, (x, y) in enumerate(((-5, 22), (-9, 18), (0, 24))):
        w.box('ice_tooth_' + str(i), (x, y - 2, 7.2), (x + .8, y, 8.8), 'highlight', 6)
    w.gem('frost_heart', 8, 26, 3.2, 4); w.save()


def shadow():
    w = Weapon('shadow_blade'); w.handle(-11, 2, 2.4)
    w.box('hook_guard', (0, 0, 6.4), (15, 3.5, 9.6), 'outline')
    w.box('raised_hook', (14, 3, 7), (18, 6, 9), 'metal')
    w.front('guard_glow', 1, .6, 14, 1.2, 'edge', 7)
    for i, band in enumerate([(4, 3.5, 11, 10), (5, 10, 12, 16), (7, 16, 13, 21), (10, 21, 14, 25), (12, 25, 15, 28)]):
        w.blade('curved_shadow_' + str(i), *band)
    for i, (x, y) in enumerate(((2, 8), (3, 13), (5, 18))):
        w.box('spine_barb_' + str(i), (x, y, 7.2), (x + 3, y + 2, 8.8), 'shade')
    for i, (x, y) in enumerate(((7, 7), (8, 13), (10, 19))):
        w.front('shadow_sigil_' + str(i), x, y, x + .6, y + 1.2, 'gem', 12, .9)
    w.gem('shadow_core', 8, 2, 2.8, 3); w.save()


def hyperion():
    w = Weapon('hyperion'); w.shaft()
    w.box('head_support', (5, 17, 6), (11, 20, 10), 'gold')
    for name, start, end in [
        ('left_cage', (1.5, 21, 6.5), (4.5, 28, 9.5)),
        ('right_cage', (11.5, 21, 6.5), (14.5, 28, 9.5)),
        ('cage_floor', (3, 19, 6.5), (13, 22, 9.5)),
        ('crown_left', (3, 28, 7), (6, 31, 9)),
        ('crown_right', (10, 28, 7), (13, 31, 9)),
    ]:
        w.box(name, start, end, 'outline')
    w.front('left_gilding', 2.1, 22, 3.2, 27, 'gold')
    w.front('right_gilding', 12.8, 22, 13.9, 27, 'gold')
    w.box('crystal_body', (5, 22, 6), (11, 28, 10), 'gem', 14)
    w.box('crystal_crown', (6.5, 28, 6.5), (9.5, 32, 9.5), 'gem', 14)
    w.front('crystal_cut', 6, 23, 6.8, 27, 'highlight', 14, 1)
    w.front('arcane_rune', 7.6, 5, 8.4, 14, 'gem', 12, -.2); w.save()


def wind():
    w = Weapon('windweaver'); w.shaft()
    for name, start, end in [
        ('ring_left', (2, 16, 6), (5, 26, 10)), ('ring_right', (11, 16, 6), (14, 26, 10)),
        ('ring_top', (4, 25, 6), (12, 28, 10)), ('ring_bottom', (4, 14, 6), (12, 17, 10)),
    ]: w.box(name, start, end, 'outline')
    w.front('left_silver', 2.5, 17, 4.5, 25, 'highlight')
    w.front('right_silver', 11.5, 17, 13.5, 25, 'highlight')
    w.front('upper_silver', 4.5, 25.5, 11.5, 27, 'metal')
    for side in (-1, 1):
        for i in range(3):
            x = 8 + side * (8 + i * 2.3)
            lo, hi = sorted((x, x + side * 2.4))
            w.box('wing_' + str(side) + '_' + str(i), (lo, 20 + i, 7), (hi, 25 + i * 1.5, 9), 'metal')
            w.front('wing_glint_' + str(side) + '_' + str(i), lo + .2, 23 + i, hi - .2, 24 + i, 'highlight', 5)
    w.gem('wind_eye', 8, 21, 3, 4.5); w.save()


def earth():
    w = Weapon('earthbreaker'); w.shaft()
    for i, band in enumerate([(-2, 12, 4, 16), (-6, 16, 7, 20), (-8, 20, 8, 25), (-6, 25, 7, 29), (-2, 29, 5, 31)]):
        w.blade('jade_axe_' + str(i), *band, edge='left')
    for i, (x1, y1, x2, y2) in enumerate(((10, 20, 17, 25), (16, 21, 20, 24), (11, 25, 15, 28))):
        w.box('axe_spike_' + str(i), (x1, y1, 7), (x2, y2, 9), 'shade')
        w.front('spike_rim_' + str(i), x1 + .3, y1 + .4, x2 - .3, y1 + 1, 'gold')
    w.gem('emerald_socket', 8, 23, 4, 5); w.save()


def katana():
    w = Weapon('void_katana'); w.handle(-14, 1, 2.25)
    w.box('tsuba', (3, 1, 5.8), (13, 3, 10.2), 'outline')
    w.front('tsuba_rim', 3.5, 1.5, 12.5, 2.4, 'gold')
    for i, band in enumerate(((6, 3, 10, 14), (6.5, 14, 10.5, 24), (7.2, 24, 11.2, 29), (9, 29, 11.3, 32))):
        w.blade('void_edge_' + str(i), *band)
    for i, y in enumerate((-11, -7, -3)):
        w.front('violet_wrap_' + str(i), 7, y, 9, y + .65, 'edge', 6, -.1)
    for i, (x, y) in enumerate(((7.5, 7), (8, 16), (8.7, 25))):
        w.front('void_mark_' + str(i), x, y, x + .5, y + 1.4, 'gem', 13, .8)
    w.save()


def thunder():
    w = Weapon('thunder_spear'); w.shaft()
    for i, band in enumerate(((5, 20, 11, 24), (5.8, 24, 10.2, 28), (7, 28, 9, 31), (7.5, 31, 8.5, 32))):
        w.blade('single_spear_point_' + str(i), *band)
    for i, (x1, y1, x2, y2) in enumerate(((1, 18, 6, 20), (-1, 20, 3, 22), (10, 18, 15, 20), (13, 16, 17, 18))):
        w.box('lightning_guard_' + str(i), (x1, y1, 6.7), (x2, y2, 9.3), 'outline')
        w.front('electric_inlay_' + str(i), x1 + .2, y1 + .4, x2 - .2, y2 - .4, 'edge', 12)
    for i, y in enumerate((3, 7, 11, 15)):
        w.front('haft_spark_' + str(i), 7.7, y, 8.3, y + 1.3, 'gem', 12, -.3)
    w.gem('storm_core', 8, 21, 3.5, 4); w.save()


def atlas():
    # The 1254px source stays byte-identical to image_gen's output. Native
    # unstitch creates power-of-two sprites without stitching the full sheet.
    regions = []
    for row, weapon in enumerate(NAMES):
        for column, material in enumerate(MATERIALS):
            regions.append({'sprite': f'bigcasares:item/legendary/{weapon}/{material}',
                            'x': round((column + .5) * 1254 / 8 - 64),
                            'y': round((row + .5) * 1254 / 8 - 64), 'width': 128, 'height': 128})
    source = {'sources': [{'type': 'minecraft:unstitch', 'resource': 'bigcasares:legendary/material_atlas',
                          'divisor_x': 1254, 'divisor_y': 1254, 'regions': regions}]}
    path = ASSETS / 'minecraft/atlases/items.json'; path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(source, indent=2) + '\n')


if __name__ == '__main__':
    atlas()
    for generate in (bloodlust, frost, shadow, hyperion, wind, earth, katana, thunder):
        generate()
