#!/usr/bin/env python3
"""Build the six approved voxel designs using the existing image_gen materials.

Writes model/atlas JSON only. Neither palette PNG nor retired model is changed.
All long weapons and the mace share a straight shaft centered at x=z=8.
"""
from generate_models import Weapon
from generate_simple_models import simple_atlas


def sprite(weapon, material, palette='legendary_simple'):
    return f'bigcasares:item/{palette}/{weapon}/{material}'


PALETTE = {
    'black': sprite('shadow_blade', 'grip'),
    'charcoal': sprite('windweaver', 'outline'),
    'darksteel': sprite('windweaver', 'shade'),
    'steel': sprite('windweaver', 'metal'),
    'silver': sprite('windweaver', 'edge'),
    'white': sprite('windweaver', 'highlight'),
    'cyan': sprite('frost_scythe', 'metal'),
    'ice': sprite('frost_scythe', 'edge'),
    'blue': sprite('thunder_spear', 'shade'),
    'purple': sprite('shadow_blade', 'gem'),
    'violet': sprite('shadow_blade', 'edge'),
    'darkpurple': sprite('shadow_blade', 'shade'),
    'wood': sprite('earthbreaker', 'grip'),
    'gold': sprite('earthbreaker', 'gold'),
    'brass': sprite('bloodlust', 'gold'),
    'palegold': sprite('frost_scythe', 'gold'),
    'ivory': sprite('windweaver', 'gold'),
    'red': sprite('bloodlust', 'metal'),
    'ruby': sprite('bloodlust', 'gem'),
    'crimson': sprite('bloodlust', 'shade'),
    'rededge': sprite('bloodlust', 'edge'),
    'burgundy': sprite('bloodlust', 'grip'),
}


class ApprovedWeapon(Weapon):
    def __init__(self, name, metal):
        super().__init__(name, 'legendary_simple', gui_rotation=(0, -10, 0))
        self.textures.update(PALETTE)
        self.textures['metal'] = PALETTE[metal]

    def part(self, name, start, end, material, top=None, side=None, light=0):
        self.box(name, start, end, material, light)
        faces = self.elements[-1]['faces']
        if top:
            faces['up']['texture'] = '#' + top
        if side:
            for face in ('east', 'west', 'down'):
                faces[face]['texture'] = '#' + side

    def panel(self, name, x1, y1, x2, y2, material, z=6.7, light=0):
        # Paired faces keep the same detail visible from either hand.
        self.part(name + '_front', (x1, y1, z - .2), (x2, y2, z), material, light=light)
        self.part(name + '_back', (x1, y1, 16 - z), (x2, y2, 16.2 - z), material, light=light)

    def ring(self, name, y, material, width=3.2):
        half = width / 2
        self.part(name, (8 - half, y, 8 - half), (8 + half, y + .8, 8 + half), material)

    def gem(self, name, x, y, material, width=2.5, height=3, z=5.8):
        self.panel(name, x - width / 2, y - height / 3,
                   x + width / 2, y + height / 3, material, z, 5)
        self.panel(name + '_tips', x - width / 4, y - height / 2,
                   x + width / 4, y + height / 2, material, z - .15, 5)


def bloodlust():
    w = ApprovedWeapon('bloodlust', 'red')
    w.part('wrapped_handle', (6.6, -12, 6.6), (9.4, 2, 9.4), 'wood', 'gold')
    for i, y in enumerate((-10, -7, -4, -1)):
        w.ring('tan_wrap_' + str(i), y, 'palegold', 2.9)
    w.part('bronze_pommel', (5.8, -16, 5.8), (10.2, -12, 10.2), 'brass', 'palegold', 'wood')
    w.gem('pommel_ruby', 8, -14, 'ruby', 1.6, 1.8, 5.55)
    w.part('guard', (1, 1, 6), (15, 3.5, 10), 'brass', 'palegold', 'wood')
    for side in (0, 1):
        x = 0 if side == 0 else 13
        w.part('guard_end_' + str(side), (x, 2.5, 6), (x + 3, 5, 10), 'gold', 'palegold', 'brass')
        x = 0 if side == 0 else 15
        w.part('guard_hook_' + str(side), (x, 5, 6.5), (x + 1, 7, 9.5), 'gold', 'palegold', 'brass')
    bands = ((4, 3.5, 12, 24), (3, 24, 13, 27), (4, 27, 12, 29),
             (5.5, 29, 10.5, 30.5), (7, 30.5, 9, 32))
    for i, (x1, y1, x2, y2) in enumerate(bands):
        w.part('crimson_outline_' + str(i), (x1, y1, 6.6), (x2, y2, 9.4), 'burgundy')
        w.panel('crimson_face_' + str(i), x1 + .65, y1, x2 - .65, y2, 'red', 6.55)
    w.panel('blade_dark_facet', 4.7, 4, 6.4, 24, 'crimson', 6.25)
    w.panel('blade_ridge', 7.65, 5, 8.35, 27, 'rededge', 6.15)
    w.part('ruby_mount', (5.6, .6, 5.2), (10.4, 5, 10.8), 'gold', 'palegold', 'brass')
    w.gem('guard_ruby', 8, 2.8, 'ruby', 3, 3.4, 5)
    w.save()


def shadow():
    w = ApprovedWeapon('shadow_blade', 'black')
    w.part('handle', (6.7, -12, 6.7), (9.3, 2, 9.3), 'black')
    for i, y in enumerate((-10, -7, -4, -1)):
        w.ring('gray_wrap_' + str(i), y, 'charcoal', 2.9)
    w.part('pommel', (5.8, -16, 5.8), (10.2, -12, 10.2), 'darksteel', 'steel', 'charcoal')
    w.gem('pommel_violet', 8, -14, 'purple', 1.6, 1.8, 5.55)
    w.part('blade', (4.5, 3, 6.8), (11, 28, 9.2), 'black', 'darksteel', 'charcoal')
    w.part('spine', (10.4, 4, 6.5), (12, 28, 9.5), 'darksteel', 'steel', 'charcoal')
    w.part('left_edge', (4, 4, 6.6), (5.2, 26, 9.4), 'darksteel', 'steel', 'charcoal')
    for i, y in enumerate((6, 12, 18, 24)):
        w.part('spine_step_' + str(i), (11.5, y, 6.7), (13, y + 2, 9.3), 'steel', 'silver', 'darksteel')
    w.part('tip_shoulder', (5.5, 28, 6.6), (12, 30, 9.4), 'darksteel', 'steel', 'charcoal')
    w.part('tip', (7.5, 30, 6.8), (12, 31.5, 9.2), 'steel', 'silver', 'darksteel')
    w.part('guard', (1, .5, 5.8), (15, 3, 10.2), 'steel', 'silver', 'darksteel')
    for x in (1, 13):
        w.part('guard_hook_' + str(x), (x, 3, 6), (x + 2, 5, 10), 'darksteel', 'steel', 'charcoal')
    w.part('core_mount', (5.7, -.5, 5.1), (10.3, 4.5, 10.9), 'darksteel', 'steel', 'charcoal')
    w.gem('violet_core', 8, 2, 'purple', 2.5, 3.4, 4.95)
    w.save()


def frost():
    w = ApprovedWeapon('frost_scythe', 'steel')
    w.part('steel_shaft', (6.8, -14, 6.8), (9.2, 23, 9.2), 'steel', 'silver', 'darksteel')
    for i, y in enumerate((-10, -6, -2, 12, 17)):
        w.ring('dark_band_' + str(i), y, 'black', 2.8)
        w.ring('cyan_band_' + str(i), y + .8, 'cyan', 2.8)
    w.part('pommel', (5.8, -16, 5.8), (10.2, -13, 10.2), 'black', 'darksteel')
    w.panel('pommel_inset', 7, -15.5, 9, -13.5, 'cyan', 5.6)
    w.part('black_neck', (5, 18, 6), (11, 26, 10), 'black', 'darksteel')
    for i, (x1, y1, x2, y2) in enumerate(((-2, 24, 8, 28), (-7, 21, 1, 27),
                                                        (-11, 17, -5, 24), (-14, 13, -9, 20))):
        w.part('hook_blade_' + str(i), (x1, y1, 6.5), (x2, y2, 9.5), 'steel', 'darksteel', 'darksteel')
        w.part('silver_cut_' + str(i), (x1, y1, 6.3), (x2, y1 + 1.2, 9.7), 'silver', 'white', 'steel')
    w.part('hook_tip', (-15, 10, 6.7), (-12, 14.2, 9.3), 'silver', 'white', 'steel')
    for i, (x1, y1, x2, y2) in enumerate(((10, 24, 15, 27), (15, 26, 19, 29), (19, 28, 22, 31))):
        w.part('counterspike_' + str(i), (x1, y1, 6.6), (x2, y2, 9.4), 'darksteel', 'silver', 'black')
        w.part('cyan_counteredge_' + str(i), (x1, y2 - .9, 6.3), (x2, y2, 9.7), 'cyan', 'ice', 'blue')
    w.part('joint_frame', (5.8, 20, 5.7), (10.2, 24, 10.3), 'darksteel', 'steel', 'black')
    w.gem('cyan_joint', 8, 22, 'ice', 2.5, 2.8, 5.5)
    w.save()


def wind():
    w = ApprovedWeapon('windweaver', 'steel')
    w.part('straight_shaft', (6.8, -13, 6.8), (9.2, 17, 9.2), 'black', 'charcoal')
    for i, y in enumerate((1, 4.5, 8, 11.5)):
        w.ring('purple_wrap_' + str(i), y, 'violet', 2.8)
    w.part('pommel', (5.7, -16, 5.7), (10.3, -12, 10.3), 'darksteel', 'steel', 'charcoal')
    w.gem('pommel_amethyst', 8, -14, 'purple', 2, 2.4, 5.4)
    w.part('mace_core', (2, 16, 3), (14, 29, 13), 'charcoal', 'darksteel', 'black')
    w.part('head_shoulders', (0, 18, 4), (16, 27, 12), 'black', 'darksteel', 'darkpurple')
    w.part('head_crown', (4, 29, 5), (12, 31, 11), 'charcoal', 'steel', 'black')
    w.part('left_brace', (2.5, 18, 2), (5, 27, 14), 'steel', 'silver', 'darksteel')
    w.part('right_brace', (11, 18, 2), (13.5, 27, 14), 'steel', 'silver', 'darksteel')
    w.part('bottom_brace', (4.5, 16.5, 2), (11.5, 19, 14), 'steel', 'silver', 'darksteel')
    w.part('top_brace', (4.5, 26, 2), (11.5, 28.5, 14), 'steel', 'silver', 'darksteel')
    w.part('upper_strut', (7, 28, 2.5), (9, 32, 13.5), 'silver', 'white', 'steel')
    w.part('lower_strut', (7, 13, 3), (9, 17, 13), 'silver', 'white', 'steel')
    for x in (0, 14):
        w.part('side_stud_' + str(x), (x, 21.5, 3), (x + 2, 24, 13), 'steel', 'silver', 'darksteel')
    w.part('crystal_socket', (5, 19, 1.7), (11, 26, 14.3), 'darkpurple', 'violet', 'charcoal')
    w.part('amethyst_crystal', (6, 20, 1.3), (10, 25, 14.7), 'purple', 'violet', 'darkpurple', 6)
    w.panel('crystal_facet', 6.3, 23.5, 9.7, 24.5, 'violet', 1.2, 6)
    w.save()


def earth():
    w = ApprovedWeapon('earthbreaker', 'gold')
    w.part('wooden_shaft', (6.7, -14, 6.7), (9.3, 21, 9.3), 'wood', 'brass')
    w.part('black_grip', (6.4, -12, 6.4), (9.6, -3, 9.6), 'black', 'charcoal')
    w.part('gold_pommel', (5.5, -16, 5.5), (10.5, -12, 10.5), 'gold', 'palegold', 'brass')
    w.ring('lower_collar', 15, 'palegold', 4)
    w.part('axe_joint', (3, 18, 5), (13, 28, 11), 'brass', 'gold', 'wood')
    w.part('front_joint', (5, 18, 4.5), (11, 28, 11.5), 'gold', 'palegold', 'brass')
    w.panel('central_inlay', 7.3, 19, 8.7, 27, 'palegold', 4.3)
    w.part('top_stem', (7, 28, 7), (9, 30, 9), 'brass', 'gold')
    w.part('top_diamond', (6, 29, 6), (10, 31, 10), 'gold', 'palegold', 'brass')
    w.part('top_point', (7, 31, 7), (9, 32, 9), 'palegold', 'ivory', 'gold')
    for side in (0, 1):
        x1, x2, origin = (-4, 1, -1.5) if side == 0 else (15, 20, 17.5)
        for upper in (False, True):
            first = len(w.elements)
            y1, y2 = (21, 31) if upper else (11, 21)
            suffix = f'{side}_{int(upper)}'
            w.part('crescent_frame_' + suffix, (x1, y1, 6), (x2, y2, 10), 'wood', 'brass')
            w.panel('pale_cutting_edge_' + suffix, x1 + .6, y1 + .2,
                    x2 - .6, y2 - .2, 'white', 5.8)
            a, b = (x1 + .6, x1 + 2) if side else (x2 - 2, x2 - .6)
            w.panel('gold_inner_' + suffix, a, y1 + .2, b, y2 - .2, 'gold', 5.55)
            angle = -22.5 if upper else 22.5
            if side:
                angle = -angle
            for element in w.elements[first:]:
                element['rotation'] = {'origin': [origin, 21, 8], 'axis': 'z', 'angle': angle}
        a, b = (12, 16) if side else (0, 4)
        w.part('blade_connector_' + str(side), (a, 20, 6.3), (b, 26, 9.7), 'gold', 'palegold', 'brass')
    w.save()


def thunder():
    w = ApprovedWeapon('thunder_spear', 'cyan')
    w.part('straight_wooden_shaft', (6.9, -14, 6.9), (9.1, 17, 9.1), 'wood', 'gold')
    w.part('black_grip', (6.6, -12, 6.6), (9.4, 0, 9.4), 'black', 'charcoal')
    for i, y in enumerate((-13, -.5, 7, 14.5)):
        w.ring('gold_ferrule_' + str(i), y, 'gold', 3.5)
    w.part('gold_butt', (6.8, -15, 6.8), (9.2, -13, 9.2), 'gold', 'palegold', 'brass')
    w.part('bottom_tip', (7.5, -16, 7.5), (8.5, -15, 8.5), 'gold')
    # Each band is connected to its neighbours: one continuous central blade,
    # without a fork or any separated side prongs.
    bands = ((6, 16, 10, 19), (4.5, 19, 11.5, 23), (4.5, 23, 11.5, 25),
             (5.5, 25, 10.5, 28), (6.5, 28, 9.5, 30), (7.5, 30, 8.5, 32))
    for i, (x1, y1, x2, y2) in enumerate(bands):
        w.part('single_point_edge_' + str(i), (x1, y1, 6.7), (x2, y2, 9.3), 'white', 'white', 'silver')
        inset = .65 if x2 - x1 > 2 else .25
        w.panel('cyan_blade_' + str(i), x1 + inset, y1, x2 - inset, y2, 'ice', 6.5)
    w.panel('central_cyan_ridge', 7.7, 18, 8.3, 30, 'cyan', 6.2)
    w.part('gold_socket', (5.6, 15, 6), (10.4, 17.5, 10), 'gold', 'palegold', 'brass')
    w.gem('socket_crystal', 8, 16.3, 'ice', 2, 2.4, 5.8)
    w.save()


if __name__ == '__main__':
    simple_atlas()
    for generate in (bloodlust, shadow, frost, wind, earth, thunder):
        generate()
