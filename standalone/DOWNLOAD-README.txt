ARMAS LEGENDARIAS 2.0.15 - PAPER 26.2 / JAVA 25

Plugin independiente. No necesita BigCasares, BetterModel ni mods del cliente.

MODELOS Y ANIMACIONES
Se integran los cinco modelos del usuario: espada rubi (Bloodlust), cuchillo de
sombras (Shadow Blade), Windweaver, hacha dorada (Earthbreaker) y cetro de cristal
de rayo (Thunder Spear). Se conservan sus once animaciones originales.
El hacha no incluia animaciones propias y usa el movimiento normal de Minecraft.

NIVELES Y CONTROLES
Una baja de jugador con un arma suma un nivel de esa arma para ese jugador,
hasta 5. Se conservan al morir. Las criaturas no suman niveles.
F/G = Intercambiar objetos con la mano secundaria; asignale G en Controles.

THUNDER SPEAR (se conservan las habilidades anteriores al subir)
N1 Shift+izquierdo al aire: Lightning Dash, 8 bloques, 4 HP, 20 s cooldown.
N2 derecho: Thunder Mark, 3 golpes en 6 s, descarga 5 HP + area 3 HP, CD40.
N3 Shift+derecho: Chain Lightning, 4 enemigos, 6 HP reduciendo 25% por salto, CD35.
N4 Shift+izquierdo mirando arriba: Thunderstorm, radio6, 8 s, 4 HP cada2 s, CD60.
N5 Shift+F/G: Wrath of Zeus, carga1.5 s, 12 HP + area5 HP, CD90.

WINDWEAVER
N1 Shift+izquierdo al aire: Wind Dash, unos9 bloques, sin dano, CD18.
N2 derecho: Skybreaker, sube unos8 bloques; repeti para caer, 8/4 HP, CD40.
N3 Shift+derecho: Tornado, 5 s, 2 HP/s con maximo6 HP por objetivo, CD40.
N4 Shift+izquierdo mirando al suelo: Wind Prison, atraccion suave 5 s, CD50.
N5 Shift+F/G: Heaven's Fall, sube unos12 bloques, suspende hasta2 s,
impacto12/6 HP; repeti para bajar antes, CD90.
Los impulsos protegen la caida asociada. Los rayos no rompen bloques ni prenden
fuego. 2 HP = 1 corazon base: armadura y protecciones reducen estos danos.
Cada habilidad tiene su propio cooldown y ajustes en config.yml.

BLOODLUST: Infection 15% sangrado; N1 Velocidad II, N2 rastreo privado,
N3 Blood Trail con Shift+F/G (15 s, CD60), N4 Fuerza I, N5 Blood Hook con F/G
(cadena que atrae, 4 HP, CD30). Medio corazon de curacion por golpe directo
valido durante Trail/Hook. No hay Fuerza III activa.
SHADOW BLADE: F/G dagas de sombra, Ceguera/Lentitud, maximo2 corazones base
por enemigo; N2 Shift+F/G Shadow Leap (repeti para terminar antes). CD30 cada una.
EARTHBREAKER: F/G hacha gigante y onda radial, exactamente2 corazones de salud,
sin romper terreno. N3 Shift+F/G absorbe/refleja dano con limite. CD45 cada una.
Frost Scythe, Void Katana e Hyperion siguen retiradas.
Usa /weapons info <arma> para ver los controles en el juego.

ACTUALIZACION
1. Detene Paper y guarda una copia coherente del mundo, jugadores y
   plugins/LegendaryWeapons. Java 25 es obligatorio.
2. Guarda el JAR anterior fuera de plugins y copia LegendaryWeapons-2.0.15.jar.
3. Conserva el diario y los datos de jugadores. No borres inventarios ni cooldowns.
   Los objetos retirados quedan sin poder; sus registros y recursos se conservan.
4. Inicia Paper con Java 25 y conecta Minecraft Java 26.2. Acepta el nuevo pack.
5. Usa /weapons list y /weapons info <arma>.

CONTENIDO
- LegendaryWeapons-2.0.15.jar: plugin y pack incluido.
- legendary-weapons-java.zip: pack manual opcional, con recursos historicos.
- Este archivo.

El servidor debe servir el pack por una URL accesible para cada jugador. La
URL 127.0.0.1 predeterminada sirve para probar en la misma computadora.

AVISO DE WINDOWS
Las versiones anteriores tuvieron avisos locales de Defender sin diagnostico
confirmado. Si vuelve a bloquear el archivo, dejalo en cuarentena; no desactives
la proteccion ni agregues exclusiones para instalarlo.
