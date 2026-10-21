#!/usr/bin/env python3
"""Generate the six current, simplified models; never modify texture pixels.

Keep the historical atlas and the two retired weapon models intact. Minecraft
extracts the new image_gen palette directly with native power-of-two regions.
"""
import json
import struct

from generate_models import ASSETS, MATERIALS, NAMES, Weapon, atlas

ACTIVE = ('bloodlust', 'frost_scythe', 'shadow_blade', 'windweaver',
          'earthbreaker', 'thunder_spear')


def weapon(name):
    return Weapon(name, 'legendary_simple')


def handle(w, bottom=-12, top=2):
    w.box('grip', (7, bottom, 7), (9, top, 9), 'grip')
    w.box('pommel', (6.5, bottom - 1, 6.5), (9.5, bottom + 1, 9.5), 'shade')


def shaft(w):
    w.box('shaft', (7.2, -15, 7.2), (8.8, 25, 8.8), 'shade')
    w.box('grip', (6.8, -13, 6.8), (9.2, 0, 9.2), 'grip')
    w.box('endcap', (6.7, -16, 6.7), (9.3, -14, 9.3), 'metal')


def blade(w, name, x1, y1, x2, y2, edge='right'):
    w.box(name, (x1, y1, 7), (x2, y2, 9), 'metal')
    if edge == 'right':
        w.box(name + '_edge', (x2 - .7, y1, 6.8), (x2, y2, 9.2), 'edge')
    elif edge == 'left':
        w.box(name + '_edge', (x1, y1, 6.8), (x1 + .7, y2, 9.2), 'edge')
    else:
        w.box(name + '_edge', (x1, y1, 6.8), (x2, y1 + .7, 9.2), 'edge')


def bloodlust():
    w = weapon('bloodlust'); handle(w)
    w.box('guard', (2, 1, 6.5), (14, 3, 9.5), 'shade')
    for i, bounds in enumerate(((5, 3, 11, 23), (6, 23, 10, 28), (7, 28, 9, 31))):
        blade(w, 'blade_' + str(i), *bounds)
    w.box('ruby', (7, 1, 6), (9, 3, 10), 'gem', 3)
    w.save()


def frost():
    w = weapon('frost_scythe'); shaft(w)
    for i, bounds in enumerate(((1, 25, 10, 29), (-6, 23, 1, 29),
                                (-11, 18, -6, 26), (-13, 12, -10, 21))):
        blade(w, 'crescent_' + str(i), *bounds, edge='bottom')
    w.box('ice_socket', (6, 23, 6), (10, 27, 10), 'gem', 3)
    w.save()


def shadow():
    w = weapon('shadow_blade'); handle(w, -11)
    w.box('guard', (3, 1, 6.5), (13, 3, 9.5), 'shade')
    for i, bounds in enumerate(((5, 3, 11, 16), (6, 16, 12, 23),
                                (8, 23, 13, 28), (11, 28, 14, 31))):
        blade(w, 'curved_blade_' + str(i), *bounds)
    w.box('violet_core', (7, 1, 6), (9, 3, 10), 'gem', 3)
    w.save()


def wind():
    w = weapon('windweaver'); shaft(w)
    w.box('mace_head', (3, 17, 5), (13, 27, 11), 'metal')
    w.box('left_flange', (1, 19, 6), (3, 25, 10), 'shade')
    w.box('right_flange', (13, 19, 6), (15, 25, 10), 'shade')
    w.box('head_cap', (4, 27, 5.5), (12, 29, 10.5), 'highlight')
    w.box('collar', (5, 15, 6), (11, 17, 10), 'edge')
    w.box('wind_core', (6.5, 20, 4.5), (9.5, 24, 11.5), 'gem', 3)
    w.save()


def earth():
    w = weapon('earthbreaker'); shaft(w)
    blade(w, 'axe_head', -5, 18, 9, 28, edge='left')
    blade(w, 'upper_shoulder', -3, 28, 7, 30, edge='left')
    blade(w, 'lower_shoulder', -3, 16, 7, 18, edge='left')
    w.box('back_poll', (9, 20, 6.5), (13, 26, 9.5), 'shade')
    w.box('socket', (6, 20, 6), (10, 26, 10), 'gold')
    w.box('emerald', (7, 22, 5.5), (9, 24, 10.5), 'gem', 3)
    w.save()


def thunder():
    w = weapon('thunder_spear'); shaft(w)
    for i, bounds in enumerate(((5, 20, 11, 25), (6, 25, 10, 29), (7, 29, 9, 32))):
        blade(w, 'point_' + str(i), *bounds)
    w.box('guard', (3, 18, 6.5), (13, 20, 9.5), 'gold')
    w.box('storm_core', (7, 19, 6), (9, 22, 10), 'gem', 3)
    w.save()


def simple_atlas():
    path = ASSETS / 'bigcasares/textures/legendary/simple_material_atlas.png'
    with path.open('rb') as image:
        header = image.read(24)
    assert header[:8] == b'\x89PNG\r\n\x1a\n'
    width, height = struct.unpack('>II', header[16:24])
    # A centered 64px crop avoids boundaries even when the original image size
    # is not a power of two. No image resampling or pixel rewriting is needed.
    assert min(width, height) >= 512
    regions = []
    for name in ACTIVE:
        row = NAMES.index(name)
        for column, material in enumerate(MATERIALS):
            regions.append({'sprite': f'bigcasares:item/legendary_simple/{name}/{material}',
                            'x': round((column + .5) * width / 8 - 32),
                            'y': round((row + .5) * height / 8 - 32),
                            'width': 64, 'height': 64})
    atlas()
    output = ASSETS / 'minecraft/atlases/items.json'
    data = json.loads(output.read_text())
    data['sources'].append({'type': 'minecraft:unstitch',
                           'resource': 'bigcasares:legendary/simple_material_atlas',
                           'divisor_x': width, 'divisor_y': height, 'regions': regions})
    output.write_text(json.dumps(data, indent=2) + '\n')


if __name__ == '__main__':
    simple_atlas()
    for generate in (bloodlust, frost, shadow, wind, earth, thunder):
        generate()
