#!/usr/bin/env python3
"""Capture the actual editable masters in an already running Blockbench web app.

User-supplied projects stay untouched. Axis corrections and uniform scales apply
to the review composition only. Each master retains its own embedded textures.
"""
import argparse
import json
from pathlib import Path

from playwright.sync_api import sync_playwright


ROOT = Path(__file__).resolve().parents[1] / 'models/reference-study'
IDS = ['bloodlust', 'shadow_blade', 'windweaver', 'earthbreaker', 'thunder_spear']


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--cdp', default='http://127.0.0.1:9224')
    parser.add_argument('--view', choices=['gallery', 'shadow'], default='gallery')
    args = parser.parse_args()
    payload = {
        'projects': {key: json.loads((ROOT / f'{key}.bbmodel').read_text()) for key in IDS},
        'overrides': json.loads((ROOT / 'model-overrides.json').read_text()),
        'view': args.view,
    }
    stem = 'review-gallery' if args.view == 'gallery' else 'review-shadow-user-model'
    image = 'review-blockbench.png' if args.view == 'gallery' else stem + '.png'
    with sync_playwright() as playwright:
        browser = playwright.chromium.connect_over_cdp(args.cdp)
        page = browser.contexts[0].pages[0]
        width, height = (1800, 1300) if args.view == 'gallery' else (1600, 1100)
        page.set_viewport_size({'width': width, 'height': height})
        result = page.evaluate('''async ({projects, overrides, view}) => {
            if (Dialog.open) Dialog.open.hide();
            const previous = document.getElementById('model-review-stage');
            if (previous) previous.remove();
            Project.saved = true;
            newProject(Formats.free);
            Project.name = view === 'gallery' ? 'Cinco armas · modelos del usuario' : 'Shadow Blade · modelo del usuario';
            Project.texture_width = Project.texture_height = 2048;
            const labels = [], counts = {};
            function add(id, position, yaw, extraScale = 1) {
                const raw = structuredClone(projects[id]), override = overrides[id];
                const center = override?.preview.sourceCenter || [8, 8, 8];
                const scale = (override?.preview.scale || 1) * extraScale;
                const transform = vector => vector.map((v, axis) => (v - center[axis]) * scale + position[axis]);
                for (const element of raw.elements) {
                    if (element.type === 'cube') {
                        // Project merge expects all six entries. An omitted
                        // face in an uploaded project is an invisible face.
                        for (const side of ['north', 'east', 'south', 'west', 'up', 'down']) {
                            element.faces[side] ||= {texture: null, uv: [0, 0, 0, 0]};
                        }
                    }
                    if (element.origin) element.origin = transform(element.origin);
                    if (element.from) element.from = transform(element.from);
                    if (element.to) element.to = transform(element.to);
                    if (element.vertices) for (const key of Object.keys(element.vertices)) {
                        element.vertices[key] = element.vertices[key].map(v => v * scale);
                    }
                }
                function transformGroups(nodes) {
                    for (const node of nodes) if (typeof node === 'object') {
                        if (node.origin) node.origin = transform(node.origin);
                        transformGroups(node.children || []);
                    }
                }
                transformGroups(raw.outliner || []);
                // Animations remain in the master; the still composition uses its rest pose.
                raw.animations = [];
                // Older projects store groups only in the outliner. Remap all
                // IDs explicitly so a second view cannot steal the first view's
                // groups when Blockbench merges the same uploaded project twice.
                const ids = new Map();
                function collectIds(value) {
                    if (!value || typeof value !== 'object') return;
                    if (typeof value.uuid === 'string' && !ids.has(value.uuid)) ids.set(value.uuid, guid());
                    for (const child of Object.values(value)) collectIds(child);
                }
                function remapIds(value) {
                    if (typeof value === 'string') return ids.get(value) || value;
                    if (Array.isArray(value)) return value.map(remapIds);
                    if (value && typeof value === 'object') for (const key of Object.keys(value)) value[key] = remapIds(value[key]);
                    return value;
                }
                collectIds(raw);
                remapIds(raw);
                const before = new Set(Outliner.root);
                Codecs.project.merge(raw);
                const roots = Outliner.root.filter(node => !before.has(node));
                const outer = new Group({name: id + ' · review angle', origin: position, rotation: [0, yaw, 0]}).init();
                const axis = new Group({name: 'review axis', origin: position, rotation: override?.preview.axisRotation || [0, 0, 0]}).addTo(outer).init();
                for (const node of roots) node.addTo(axis);
                counts[id] = (counts[id] || 0) + raw.elements.length;
            }
            const titles = ['BLOODLUST', 'SHADOW BLADE', 'WINDWEAVER', 'EARTHBREAKER', 'THUNDER SPEAR'];
            if (view === 'gallery') {
                Object.keys(projects).forEach((id, index) => {
                    const x = index < 3 ? (index - 1) * 58 : (index - 3.5) * 72;
                    const y = index < 3 ? 32 : -32;
                    add(id, [x, y, 8], -24);
                    labels.push({x, y: y - 28, title: titles[index]});
                });
            } else {
                add('shadow_blade', [-16, 0, 8], 0, 1.45);
                add('shadow_blade', [16, 0, 8], -38, 1.45);
                labels.push({x: -16, y: -27, title: 'VISTA FRONTAL'}, {x: 16, y: -27, title: 'TRES CUARTOS'});
            }
            for (const texture of Texture.all) await new Promise((resolve, reject) => {
                if (texture.img.complete && texture.img.naturalWidth) resolve();
                else {
                    texture.img.addEventListener('load', resolve, {once: true});
                    texture.img.addEventListener('error', () => reject(new Error('Texture failed: ' + texture.name)), {once: true});
                }
            });
            await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            unselectAll();
            for (const key of ['grids', 'base_grid', 'large_grid', 'full_grid', 'highlight_cubes']) if (settings[key]) settings[key].set(false);
            settings.shading.set(true);
            Canvas.updateAll();
            const width = view === 'gallery' ? 1800 : 1600, height = view === 'gallery' ? 1300 : 1100;
            const stage = document.createElement('div');
            stage.id = 'model-review-stage';
            stage.style.cssText = `position:fixed;inset:0;width:${width}px;height:${height}px;background:#f5f3ee;z-index:999999;overflow:hidden;`;
            document.body.appendChild(stage);
            const preview = Preview.selected;
            stage.appendChild(preview.node);
            preview.node.style.cssText = `position:absolute;inset:0;width:${width}px;height:${height}px;background:#f5f3ee;`;
            const style = document.createElement('style');
            style.textContent = '#model-review-stage .preview > :not(canvas){display:none!important} #model-review-stage .orbit_gizmo{display:none!important}';
            document.head.appendChild(style);
            preview.resize();
            preview.setProjectionMode(true);
            preview.camera.position.set(0, view === 'gallery' ? 27 : 0, 240);
            preview.controls.target.set(0, 0, 8);
            preview.camera.zoom = (preview.height / 40) / (view === 'gallery' ? 138 : 68);
            preview.camera.updateProjectionMatrix();
            preview.controls.update();
            preview.camera.updateMatrixWorld(true);
            preview.renderer.setClearColor(0xf5f3ee, 1);
            const title = document.createElement('div');
            title.textContent = view === 'gallery' ? 'ARMAS LEGENDARIAS · TUS MODELOS' : 'SHADOW BLADE · TU MODELO';
            title.style.cssText = 'position:absolute;top:24px;left:0;right:0;text-align:center;color:#29272b;font:700 29px Arial,sans-serif;letter-spacing:3px;pointer-events:none;';
            stage.appendChild(title);
            for (const label of labels) {
                const q = new THREE.Vector3(label.x, label.y, 8).project(preview.camera), node = document.createElement('div');
                node.textContent = label.title;
                node.style.cssText = `position:absolute;left:${(q.x+1)*preview.width/2}px;top:${(1-q.y)*preview.height/2}px;transform:translate(-50%,-50%);color:#29272b;font:700 22px Arial,sans-serif;letter-spacing:1px;pointer-events:none;white-space:nowrap;`;
                stage.appendChild(node);
            }
            preview.render();
            return {version: Blockbench.version, counts, project: Codecs.project.compile({raw:true,bitmaps:true,absolute_paths:false}), textures: Texture.all.map(t => ({name:t.name,width:t.width,height:t.height}))};
        }''', payload)
        (ROOT / (stem + '.bbmodel')).write_text(json.dumps(result.pop('project'), ensure_ascii=False, indent=2) + '\n')
        page.wait_for_timeout(500)
        page.locator('#model-review-stage').screenshot(path=str(ROOT / image))
        print(json.dumps(result, ensure_ascii=False))


if __name__ == '__main__':
    main()
