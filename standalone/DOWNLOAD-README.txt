ARMAS LEGENDARIAS 2.0.6 - PAPER 26.2 / JAVA 25

Reconstruido desde las fuentes de 2.0.3, con la paralisis de Thunder Spear
implementada de nuevo. Plugin independiente; no necesita BigCasares ni BetterModel.

CAMBIOS 2.0.6
- Earthbreaker conserva el crater y aplica una vez 4 puntos de salud
  (dos corazones) a enemigos dentro de radio 8 y altura +/-4.
  Ignora armadura, encantamientos, Resistencia, escudos y absorcion.
  Respeta protecciones, equipos, invulnerabilidad y cancelaciones del servidor.
  Las caidas posteriores siguen causando su dano normal aparte.
- Shadow Blade teletransporta detras del rival y golpea con 14 de dano magico
  antes de reducciones. Aplica Debilidad II 4 s y Oscuridad 3 s al recibir dano.
  Otorga Invisibilidad y Velocidad III durante 5 s al lanzador.
  Sin rival conserva el desplazamiento seguro corto y esos beneficios.
  La clave nueva shadow-blade.ambush-damage usa 14 por defecto aun al actualizar;
  sustituye la clave strike-damage anterior. Alcance y cooldown siguen iguales.

CONTENIDO
- LegendaryWeapons-2.0.6.jar: el plugin; incluye los ocho modelos.
- legendary-weapons-java.zip: paquete de recursos para instalacion manual opcional.
- Este archivo.

ESTADO DE LA ALERTA
Windows Defender detecto Trojan:Script/Ulthar.A!ml en la distribucion 2.0.4.
El caso sigue sin diagnostico confirmado. Esta reconstruccion no demuestra
que aquella deteccion fuera un falso positivo ni garantiza que Defender permita
este archivo en otro equipo. Consulta los informes de 2.0.6 en la rama.
Si Defender lo bloquea, dejalo en cuarentena: no agregues exclusiones ni
desactives la proteccion para instalarlo.

INSTALACION EN PAPER
1. Detene Paper 26.2. Conserva una copia coherente del mundo, jugadores y
   plugins/LegendaryWeapons. Java 25 es obligatorio.
2. Copia LegendaryWeapons-2.0.6.jar a plugins. Deja una sola version del
   plugin cargada: guarda el JAR anterior fuera de plugins.
3. Inicia Paper con Java 25 y conecta Minecraft Java 26.2. Acepta el pack.
   Por defecto se sirve en http://127.0.0.1:8124/legendary-pack.zip para esa PC.
4. Consulta /weapons info thunder_spear y fabrica en una mesa vanilla.
   Si actualizas desde 2.0.x, conserva el diario y los archivos de jugadores.

LANZA
Campo fijo de radio 7 durante 10 segundos, altura de 4 bloques arriba/abajo.
Paraliza a los rivales y al lanzador dentro del campo. Permite mirar y atacar.
Bloquea caminar, saltar, empujones y escapes de juego por teletransporte.
Respeta protecciones y equipos; los comandos administrativos permiten salir.
Siguen cayendo rayos donde impacto. SOLO la entidad alcanzada directamente
recibe los cinco pulsos electricos de 7 puntos antes de reducciones.
Entrar al circulo sin ese impacto paraliza sin causar dano electrico.
Al finalizar/cancelar se libera a todos. La lanza vuelve sin duplicarse.
Conserva Lunge III nativo y cooldown de 40 segundos.

Frost conserva congelacion y lentitud; Hyperion los cinco disparos de ghast;
Bloodlust Fuerza III durante 10 segundos. Modelos, recetas y datos se conservan.
