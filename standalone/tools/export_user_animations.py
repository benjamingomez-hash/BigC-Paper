#!/usr/bin/env python3
"""Bake supplied Blockbench scenes at 20 Hz into native Minecraft 26.2 cuboids.

Requires the real Blockbench web app on CDP 9224. No image pixels are edited:
embedded texture bytes are copied unchanged. Uploaded projects remain untouched.
"""
from pathlib import Path
import base64, hashlib, json, math
import numpy as np
from playwright.sync_api import sync_playwright

ROOT=Path(__file__).resolve().parents[1]
SOURCES=ROOT/'models/reference-study'
ASSETS=ROOT/'pack/assets/bigcasares'
IDS=['bloodlust','shadow_blade','windweaver','earthbreaker','thunder_spear']
FPS=20

def rotation(values):
    x,y,z=np.radians(values)
    rx=np.array([[1,0,0],[0,math.cos(x),-math.sin(x)],[0,math.sin(x),math.cos(x)]])
    ry=np.array([[math.cos(y),0,math.sin(y)],[0,1,0],[-math.sin(y),0,math.cos(y)]])
    rz=np.array([[math.cos(z),-math.sin(z),0],[math.sin(z),math.cos(z),0],[0,0,1]])
    return rz@ry@rx

def euler(matrix):
    y=math.atan2(-matrix[2,0],math.hypot(matrix[0,0],matrix[1,0]))
    if math.hypot(matrix[0,0],matrix[1,0])>1e-8:
        x=math.atan2(matrix[2,1],matrix[2,2]);z=math.atan2(matrix[1,0],matrix[0,0])
    else:
        x=0;z=math.atan2(-matrix[0,1],matrix[1,1])
    result=np.degrees([x,y,z])
    assert np.max(np.abs(rotation(result)-matrix))<1e-5, (result,matrix)
    return result

def rounded(values):return [round(float(v),7) for v in values]
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':'))+'\n')

def main():
    projects={key:json.loads((SOURCES/(key+'.bbmodel')).read_text()) for key in IDS}
    mapping=json.loads((SOURCES/'model-overrides.json').read_text())
    with sync_playwright() as playwright:
        browser=playwright.chromium.connect_over_cdp('http://127.0.0.1:9224')
        page=browser.contexts[0].pages[0]
        sampled=page.evaluate('''async projects=>{
            const result={};
            for(const [id,raw] of Object.entries(projects)){
                if(Dialog.open)Dialog.open.hide();Project.saved=true;
                newProject(Formats.free);Codecs.project.parse(raw);
                for(const t of Texture.all)await new Promise(resolve=>{if(t.img.complete&&t.img.naturalWidth)resolve();else t.img.addEventListener('load',resolve,{once:true});});
                await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
                Canvas.updateAll();Modes.options.animate.select();
                for(const a of Animation.all)a.playing=false;
                Animator.showDefaultPose(true);
                const cubes=Cube.all.map(c=>{
                    c.mesh.geometry.computeBoundingBox();const box=c.mesh.geometry.boundingBox;
                    return {uuid:c.uuid,name:c.name,min:box.min.toArray(),max:box.max.toArray()};
                });
                function snapshot(){return Cube.all.map(c=>{
                    for(let n=c.mesh;n;n=n.parent)if(!n.visible)return null;
                    c.mesh.updateWorldMatrix(true,false);
                    return c.mesh.matrixWorld.toArray();
                });}
                const entry={cubes,rest:snapshot(),clips:[]};
                for(const a of Animation.all){
                    for(const other of Animation.all)other.playing=false;
                    a.select();a.playing=true;
                    const frames=[];
                    for(let frame=0;frame<Math.ceil(a.length*20);frame++){
                        Timeline.setTime(frame/20);Animator.preview();frames.push(snapshot());
                    }
                    entry.clips.push({name:a.name,loop:a.loop==='loop',frames});
                }
                for(const a of Animation.all)a.playing=false;
                Animator.showDefaultPose(true);Modes.options.edit.select();Project.saved=true;
                result[id]=entry;
            }
            return result;
        }''',projects)
    (ROOT/'build/model-scene-samples.json').write_text(json.dumps(sampled,separators=(',',':')))
    proof={'fps':FPS,'blockbench':'5.2.2','weapons':{}}
    properties=['# Generated from supplied Blockbench animations. Frame 0 is the rest pose.','fps=20']
    for key,entry in sampled.items():
        raw=projects[key];elements={e['uuid']:e for e in raw['elements']}
        r=rotation(mapping[key]['preview']['axisRotation']);center=np.array(mapping[key]['preview']['sourceCenter'])
        poses=[entry['rest']]+[frame for clip in entry['clips'] for frame in clip['frames']]
        # One fixed scale for all frames. Animation poses never change item size.
        extent=1
        for pose in poses:
            for cube,flat in zip(entry['cubes'],pose):
                if flat is None:continue
                m=np.array(flat).reshape((4,4),order='F');size=np.array(cube['max'])-cube['min']
                half=np.array([np.linalg.norm(m[:3,i]) for i in range(3)])*size/2
                c=r@(m[:3,:3]@((np.array(cube['max'])+cube['min'])/2)+m[:3,3]-center)
                extent=max(extent,float(np.max(np.abs(c)+half)))
        scale=min(.45,23/extent)
        matrices_verified=0
        def bake(pose):
            nonlocal matrices_verified
            result=[]
            for cube,flat in zip(entry['cubes'],pose):
                if flat is None:continue
                source=elements[cube['uuid']]
                m=np.array(flat).reshape((4,4),order='F')
                axes=r@m[:3,:3]
                lengths=np.linalg.norm(axes,axis=0)
                size=np.array(cube['max'])-cube['min']
                if min(lengths*size*scale)<1e-6:continue
                orthogonal=axes/lengths
                assert np.max(np.abs(orthogonal.T@orthogonal-np.eye(3)))<1e-5,(key,cube['name'],'sheared animation cuboid')
                assert np.linalg.det(orthogonal)>0,(key,cube['name'],'mirrored animation')
                angles=euler(orthogonal)
                midpoint=(np.array(cube['max'])+cube['min'])/2
                c=(r@(m[:3,:3]@midpoint+m[:3,3]-center))*scale+8
                half=size*lengths*scale/2
                faces={}
                for side,face in source['faces'].items():
                    if face.get('texture') is None:continue
                    uv=face.get('uv',[0,0,16,16]);data={'uv':uv,'texture':'#body'}
                    if face.get('rotation'):data['rotation']=face['rotation']
                    faces[side]=data
                if not faces:continue
                part={'from':rounded(c-half),'to':rounded(c+half),'faces':faces,'shade':True}
                if source.get('light_emission'):part['light_emission']=source['light_emission']
                if max(abs(angles))>1e-6:part['rotation']={'origin':rounded(c),'x':round(float(angles[0]),7),'y':round(float(angles[1]),7),'z':round(float(angles[2]),7)}
                assert min(c-half)>=-16-1e-6 and max(c+half)<=32+1e-6
                # Verify all eight exported corners against the sampled BB matrix.
                for sx in [-1,1]:
                    for sy in [-1,1]:
                        for sz in [-1,1]:
                            signs=np.array([sx,sy,sz]);p=midpoint+signs*size/2
                            expected=(r@(m[:3,:3]@p+m[:3,3]-center))*scale+8
                            actual=c+rotation(angles)@(signs*half)
                            assert np.max(np.abs(expected-actual))<1e-5,(key,cube['name'])
                matrices_verified+=1;result.append(part)
            return result
        texture=raw['textures'][0]['source'];assert texture.startswith('data:image/png;base64,')
        texture_bytes=base64.b64decode(texture.split(',',1)[1]);texture_path=ASSETS/'textures/item/user'/f'{key}.png'
        texture_path.parent.mkdir(parents=True,exist_ok=True);texture_path.write_bytes(texture_bytes)
        rest=bake(entry['rest'])
        # GUI fills the same 12 units while preserving the original aspect ratio.
        rest_bounds=np.array([[p['from'],p['to']] for p in rest]);span=rest_bounds[:,1,:].max(axis=0)-rest_bounds[:,0,:].min(axis=0)
        gui_scale=12/float(max(span));hand_scale=.45/scale*.8
        display={
          'gui':{'rotation':[0,-24,0],'translation':[0,0,0],'scale':[gui_scale]*3},
          'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1.1,2.5,1.1],'scale':[hand_scale]*3},
          'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[1.1,2.5,1.1],'scale':[hand_scale]*3},
          'thirdperson_righthand':{'rotation':[0,-90,55],'translation':[0,2,0],'scale':[hand_scale]*3},
          'thirdperson_lefthand':{'rotation':[0,90,-55],'translation':[0,2,0],'scale':[hand_scale]*3},
          'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.6]*3},
          'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1]*3}}
        base={'textures':{'body':f'bigcasares:item/user/{key}','particle':f'bigcasares:item/user/{key}'},'gui_light':'front','display':display,'elements':rest}
        write(ASSETS/'models/item'/f'{key}.json',base)
        pose_ids={json.dumps(rest,separators=(',',':')):0};entries=[];clips={}
        for clip in entry['clips']:
            sequence=[]
            for pose in clip['frames']:
                parts=bake(pose);encoded=json.dumps(parts,separators=(',',':'))
                if encoded not in pose_ids:
                    frame_id=len(pose_ids);pose_ids[encoded]=frame_id
                    model=f'item/animated/{key}/{frame_id}'
                    write(ASSETS/'models'/(model+'.json'),{'parent':f'bigcasares:item/{key}','elements':parts})
                    entries.append({'threshold':frame_id,'model':{'type':'minecraft:model','model':'bigcasares:'+model}})
                sequence.append(pose_ids[encoded])
            properties.extend([f'{key}.{clip["name"]}.frames='+','.join(map(str,sequence)),f'{key}.{clip["name"]}.loop='+str(clip['loop']).lower()])
            clips[clip['name']]={'ticks':len(sequence),'loop':clip['loop']}
        rest_model={'type':'minecraft:model','model':f'bigcasares:item/{key}'}
        animated={'type':'minecraft:range_dispatch','property':'minecraft:custom_model_data','index':0,'entries':[{'threshold':0,'model':rest_model}]+entries,'fallback':rest_model}
        write(ASSETS/'items'/f'{key}.json',{'hand_animation_on_swap':False,'model':{'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':'gui','model':rest_model}],'fallback':animated}})
        proof['weapons'][key]={'originalSha256':mapping[key]['sha256'],'textureSha256':hashlib.sha256(texture_bytes).hexdigest(),'sourceCubes':len(entry['cubes']),'restCubes':len(rest),'nativeScale':scale,'uniquePoses':len(pose_ids),'verifiedCuboidMatrices':matrices_verified,'clips':clips}
        print(key,proof['weapons'][key])
    (ROOT/'src/main/resources/legendary-animations.properties').write_text('\n'.join(properties)+'\n')
    write(ROOT/'docs/user-models-export-2.0.15.json',proof)
    print('Exported supplied models and all eleven clips.')

if __name__=='__main__':main()
