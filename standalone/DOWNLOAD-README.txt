ARMAS LEGENDARIAS 2.0.10 - PAPER 26.2 / JAVA 25

Compilado desde las fuentes de 2.0.3 con todos los cambios pedidos juntos.
Plugin independiente; no necesita BigCasares ni BetterModel.

CAMBIOS SOBRE 2.0.3
- Lanza: campo fijo de radio 7 por 10 s que paraliza al lanzador y rivales;
  permite mirar y atacar. Solo el impacto directo recibe cinco pulsos
  electricos. Regresa sin duplicarse. Lunge III nativo.
- Earthbreaker: conserva el crater de radio 8 y profundidad 4/3/2/1.
  Un unico golpe inicial quita 4 puntos de salud (dos corazones), aun con
  armadura, encantamientos, Resistencia, escudo y absorcion. Respeta
  invulnerabilidad, equipos, regiones y cancelaciones. Caidas: dano vanilla aparte.
- Bloodlust: Fuerza III durante 10 segundos y cura medio corazon por golpe
  directo valido con la espada mientras la habilidad esta activa.
  No multiplica la curacion con barridos; no cura con proyectiles, golpes
  cancelados o sin dano real. Respeta salud maxima y cancelaciones de curacion.
- Sombras: teletransporte tras el rival, hasta 6 de dano magico (3 corazones
  antes de reducciones), Debilidad II 4 s y Oscuridad 3 s. Invisibilidad y
  Velocidad III 5 s al lanzador. La configuracion antigua de 14 se limita a 6.

SE CONSERVA
- Frost: congelacion y Lentitud IV para entidades vivas permitidas.
- Hyperion: cinco bolas de fuego de ghast.
- Maza: impulso vertical para preparar un smash y proteccion de la primera caida.
- Katana, ocho modelos, recetas, evento, datos y cooldowns de 40 s.

CONTENIDO
- LegendaryWeapons-2.0.10.jar: plugin con los ocho modelos incluidos.
- legendary-weapons-java.zip: pack para instalacion manual opcional.
- Este archivo.

INSTALACION
1. Detene Paper 26.2 y conserva una copia coherente del mundo, jugadores y
   plugins/LegendaryWeapons. Java 25 es obligatorio.
2. Guarda el JAR anterior fuera de plugins y copia LegendaryWeapons-2.0.10.jar
   a plugins. Conserva el diario y los datos de jugadores al actualizar 2.0.x.
3. Inicia Paper con Java 25 y conecta Minecraft Java 26.2. Acepta el pack.
   Por defecto se sirve en http://127.0.0.1:8124/legendary-pack.zip para esa PC.
4. Consulta /weapons info <arma> y fabrica en una mesa vanilla.

AVISO DE WINDOWS
Defender detecto Trojan:Script/Ulthar.A!ml en versiones anteriores. La causa
del aviso local sigue sin confirmarse. Reconstruir desde 2.0.3 no garantiza
que desaparezca ni demuestra un falso positivo. Si vuelve a bloquear el
archivo, dejalo en cuarentena; no desactives Defender ni agregues exclusiones.
