from rcon import command
from pathlib import Path
import json
commands=['difficulty peaceful','time set noon','weather clear','forceload add -16 -16 32 32','fill -10 63 -10 20 63 20 minecraft:stone']
scene=[]
for z,blocks in [(0,['shingle_steep','shingle_steep_lower']),(3,['dark_brick','light_brick','dark_brick_stair','light_brick_stair']),(6,['dynamic_timberframe','dynamic_timberframe'])]:
 for i,block in enumerate(blocks):
  x=i*2;mat=['minecraft:oak_leaves','minecraft:bricks','minecraft:diamond_block','minecraft:birch_leaves'][i]
  data={'minecraft:block/oak_planks':mat,'minecraft:block/clay':mat}
  nbt={'textureData':data}
  if block=='dynamic_timberframe':
   nbt={'textureData':{'minecraft:block/dark_oak_planks':mat,'minecraft:block/oak_planks':'minecraft:stone_bricks'},'primaryBlock':mat,'secondaryBlock':'minecraft:stone_bricks'}
  snbt=json.dumps(nbt,separators=(',',':'))
  commands.append(f'setblock {x} 64 {z} domum_ornamentum:{block}{snbt}')
  scene.append({'position':[x,64,z],'block':block,'nbt':nbt})
commands+=['save-all flush']
with Path('server/scene-commands.log').open('w') as f:
 for cmd in commands:
  result=command(cmd);print(cmd,result,flush=True);f.write(cmd+'\n'+str(result)+'\n');f.flush()
Path('server/scene.json').write_text(json.dumps(scene,indent=2))
