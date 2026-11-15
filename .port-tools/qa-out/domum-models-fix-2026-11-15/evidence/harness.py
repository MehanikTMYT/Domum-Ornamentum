import pathlib,json,os,subprocess,hashlib,shutil,sys
ROOT=pathlib.Path(__file__).resolve().parent
x=json.loads((ROOT/'launch.json').read_text())
mode=sys.argv[1]; baseline='baseline' in sys.argv
runtime=ROOT/('baseline-client' if baseline else ('server' if mode=='server' else 'client'))
runtime.mkdir(exist_ok=True); (runtime/'mods').mkdir(exist_ok=True)
jar=ROOT/'baseline/domum-ornamentum-1.0.0-local-main.jar' if baseline else pathlib.Path('/root/home/forks/Domum-Ornamentum/build/libs/domum-ornamentum-1.0.0-local-main.jar')
paths=[jar]
for tree in ['BlockUI','MineColonies','Structurize']:
 paths.extend(p for p in pathlib.Path('/root/home/forks',tree,'build/libs').glob('*.jar') if not any(t in p.name for t in ['sources','javadoc','test']))
manifest=[]
for p in paths:
 dest=runtime/'mods'/p.name; shutil.copy2(p,dest); manifest.append({'source':str(p),'runtime':str(dest),'sha256':hashlib.sha256(dest.read_bytes()).hexdigest()})
cp=[p for p in x['classpath'] if not p.startswith('/root/home/forks/Domum-Ornamentum/build/classes') and not p.startswith('/root/home/forks/Domum-Ornamentum/build/resources')]
for p in cp:
 if 'emi-neoforge' in p: manifest.append({'source':p,'runtime':'classpath','sha256':hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()})
(runtime/'runtime-classpath.json').write_text(json.dumps(cp,indent=2))
(runtime/'runtime-manifest.json').write_text(json.dumps(manifest,indent=2))
env=os.environ.copy();env.pop('MOD_CLASSES',None);env.pop('MOD_FOLDERS',None);env['DISPLAY']=':131';env['LIBGL_ALWAYS_SOFTWARE']='1'
args=x['args']+['--username','DomumQA','--width','1280','--height','720'] if mode=='client' else ['--nogui']
if mode=='client':
 (runtime/'options.txt').write_text('narrator:0\nonboardAccessibility:false\nrenderDistance:5\nsimulationDistance:5\nmaxFps:30\nguiScale:2\nsoundCategory_master:0.0\n')
 if '--connect' in sys.argv: args+=['--quickPlayMultiplayer','127.0.0.1:29361']
 elif (runtime/'saves/domum-models/level.dat').exists():args+=['--quickPlaySingleplayer','domum-models']
else:
 (runtime/'eula.txt').write_text('eula=true\n')
 (runtime/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=29361\npause-when-empty-seconds=-1\nenable-rcon=true\nrcon.port=29362\nrcon.password=domum-models-local\nonline-mode=false\ngamemode=creative\ndifficulty=peaceful\nlevel-name=domum-models\nlevel-type=minecraft:flat\nspawn-protection=0\nview-distance=5\nsimulation-distance=5\nmax-tick-time=-1\n')
cmd=['/usr/lib/jvm/java-25-openjdk-amd64/bin/java','-Xmx1536m','-XX:ActiveProcessorCount=2',*x['jvmArgs'],'-cp',':'.join(cp),'net.neoforged.fml.startup.'+('Client' if mode=='client' else 'Server'),*args]
log=runtime/(sys.argv[2] if len(sys.argv)>2 and sys.argv[2]!='baseline' else 'console.log'); f=log.open('w'); p=subprocess.Popen(cmd,cwd=runtime,env=env,stdout=f,stderr=subprocess.STDOUT,start_new_session=True); (runtime/'pid').write_text(str(p.pid)); print(p.pid,runtime,log)
