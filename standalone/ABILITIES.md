# Habilidades y niveles — 2.0.15

Paper 26.2, Java 25. Cada baja válida de otro jugador con un arma legendaria
sube **un nivel de esa arma para ese jugador**, hasta 5. Se empieza en nivel 0;
los niveles se conservan al morir, cambiar de arma y reiniciar. Matar criaturas
no suma niveles. Se acredita el arma del golpe, incluidos sus efectos pendientes,
si corresponde al asesino registrado por Minecraft dentro de los últimos 10 s.

El cambio de arma o «attribute swapping» **no reparte la baja**. El golpe normal
guarda su arma antes de que otros manejadores puedan cambiar el inventario; un
efecto pendiente conserva el arma con que se lanzó. Gana el último golpe válido
aceptado por el servidor, incluso si dos llegan en el mismo tick. Cada muerte
consume un solo crédito: no hay doble premio ni elección 50/50. Un golpe final
con un arma normal no hereda el crédito de otra legendaria anterior.

**F/G** significa «Intercambiar objetos con la mano secundaria»: F por defecto;
podés reasignarlo a G en Controles. No requiere un mod del cliente. Las habilidades
se usan desde la mano principal. El clic izquierdo normal conserva el ataque.

## Thunder Spear

| Nivel | Habilidad | Activación | Efecto predeterminado | Cooldown |
| --- | --- | --- | --- | --- |
| 1 | Lightning Dash | Shift + izquierdo al aire | 8 bloques hacia delante; 4 HP una vez por enemigo atravesado; Lentitud I 2 s | 20 s |
| 2 | Thunder Mark | Derecho | 6 s para dar 3 golpes al mismo enemigo; una descarga: 5 HP extra al objetivo y 3 HP alrededor, radio 4 | 40 s |
| 3 | Chain Lightning | Shift + derecho | Alcance 15; hasta 4 enemigos distintos; saltos de hasta 5 bloques; 6 / 4,5 / 3,375 / 2,53125 HP | 35 s |
| 4 | Thunderstorm | Shift + izquierdo mirando arriba | Selecciona el suelo hacia delante, hasta 20 bloques; radio 6; 8 s; un rayo cada 2 s contra un enemigo aleatorio: 4 HP | 60 s |
| 5 | Wrath of Zeus | Shift + F/G | Carga 1,5 s; alcance 20; 12 HP al objetivo, 5 HP alrededor en radio 5; Lentitud II 3 s | 90 s |

## Windweaver

| Nivel | Habilidad | Activación | Efecto predeterminado | Cooldown |
| --- | --- | --- | --- | --- |
| 1 | Wind Dash | Shift + izquierdo al aire | Impulso de unos 9 bloques hacia donde mirás; usable en el aire, sin daño | 18 s |
| 2 | Skybreaker | Derecho | Sube unos 8 bloques; otro derecho fuerza la caída; impacto radio 5: 8 HP en el centro y 4 HP fuera, con empuje | 40 s |
| 3 | Tornado | Shift + derecho | Avanza 3 bloques/s durante 5 s; atrae en radio 3; 2 HP/s con máximo 6 HP por objetivo | 40 s |
| 4 | Wind Prison | Shift + izquierdo mirando al suelo | Alcance 12; radio 5; 5 s de atracción suave y Lentitud II; se puede escapar | 50 s |
| 5 | Heaven's Fall | Shift + F/G | Sube unos 12 bloques, se suspende hasta 2 s; repetí para bajar antes; impacto radio 7: 12 HP centro / 6 HP exterior, empuje y Levitación I 1 s | 90 s |

Cada habilidad conserva su propio cooldown, aunque uses otra del mismo arma.
Al desbloquear un nivel conservás los anteriores. Para seleccionar las zonas de
nivel 4 mirá más de 45° hacia arriba o abajo. Los impulsos protegen únicamente
la caída de esa habilidad; la protección termina al aterrizar o al vencer su plazo.
No se puede acumular otro vuelo mientras hay uno activo.

## Bloodlust

Progresión pedida de Bloodlust de Altar SMP, implementada a partir de descripciones
públicas, con código propio:

- Base: **Infection**, 15% de probabilidad al hacer daño cuerpo a cuerpo; sangrado
  de 1 HP/s durante 5 s, cooldown de 15 s.
- Nivel 1: **Velocidad II** mientras la sostenés.
- Nivel 2: **Blood Tracker**, trazas rojas privadas hacia jugadores enemigos
  cercanos, hasta 24 bloques; los demás no las ven.
- Nivel 3: **Blood Trail**, Shift + F/G: postura de nado, invisibilidad, Velocidad III
  y charco de sangre durante 15 s; repetí para salir. Cooldown 60 s.
- Nivel 4: **Fuerza I** mientras la sostenés.
- Nivel 5: **Blood Hook**, F/G: cadena de sangre que alcanza hasta 16 bloques,
  causa 4 HP base y atrae al enemigo; ventana activa de 5 s. Cooldown 30 s.

Durante Trail o Hook, cada golpe cuerpo a cuerpo válido cura **medio corazón**,
sin superar la salud máxima. No cura con habilidades, proyectiles, barridos,
golpes cancelados ni golpes sin daño. Se respetan los vetos de curación.

## Shadow Blade y Earthbreaker

Estas habilidades se inspiran en las descripciones públicas de Shadow Blade y
Paladin Battle Axe, con límites de daño para este servidor.

| Arma | F/G, desde nivel 1 | Shift + F/G |
| --- | --- | --- |
| Shadow Blade | **Shadow Daggers**: 1/1/2/2/3 dagas según nivel; alcance 8–16; 2–4 HP base por enemigo una sola vez; Ceguera y Lentitud | **Shadow Leap**, desde nivel 2: sombra que avanza hasta 1 s; repetí para viajar antes; solo lugares y trayectos despejados |
| Earthbreaker | **Earthshatter**: hacha gigante y onda radial, radio 3–7 según nivel; un único golpe de **4 HP de salud** por enemigo | **Stalwart Absorption**, desde nivel 3: absorbe daño normal durante 3–5 s y refleja lo recibido a enemigos cercanos, hasta 2,8–4 HP base por rival y activación |

Shadow conserva Velocidad II al sostenerse y un 30% de probabilidad de atraer al
enemigo al golpear por detrás. Las dos acciones tienen cooldown separado de 30 s.
No hay golpe de siete corazones.

Las acciones del hacha tienen cooldown separado de 45 s. **No destruyen terreno.**
Earthshatter conserva la excepción solicitada de dos corazones exactos: no lo
reducen armadura, Resistencia, escudo ni absorción; respeta invulnerabilidad,
equipos, regiones, cancelaciones y tótems. Stalwart no absorbe ese golpe especial
ni daño del vacío; su reflejo no se refleja de nuevo.

## Configuración y conservación de datos

`legendary-weapons.abilities.<id>` permite ajustar `damage`, `range`, `radius`,
`duration` y `cooldown` para las diez habilidades elementales. Los IDs y valores
están en [config.yml](src/main/resources/config.yml). Los daños secundarios,
velocidad y máximo del tornado tienen sus propias claves. Cada activación toma
una copia de los ajustes; `/weapons reload` afecta las siguientes activaciones.

**2 HP = 1 corazón.** Los daños nuevos de la lanza y la maza son base y pasan por
armadura, encantamientos, escudo, Resistencia y eventos de Paper. Los rayos son
visuales: no generan fuego ni rompen bloques. Los impulsos y rayos revisan
paredes, chunks cargados, borde del mundo, equipos y regiones. Se ofrece
`LegendaryProtectionEvent` para otras protecciones; WorldGuard es opcional.
Los efectos pendientes se cancelan al morir, desconectarse o deshabilitarse.

`/weapons level <jugador> <arma> <0-5>` permite al operador ajustar niveles para
pruebas. `/weapons cooldown reset <jugador>` limpia sus cooldowns, sin tocar
niveles, eventos ni inventarios. No concede armas.

El diario conserva sus claves históricas, datos de fabricación y devoluciones
pendientes de lanzas antiguas. Conservá una copia coherente del mundo, jugadores
y `plugins/LegendaryWeapons` al actualizar. Frost, Void Katana e Hyperion siguen
retiradas y sus recursos históricos permanecen.

Referencias públicas consultadas: [progresión Bloodlust](https://modrinth.com/plugin/unknowns-altar-smp),
[habilidades de las armas](https://modrinth.com/plugin/altar-smp-there-is-no-arc-version).
Son recreaciones públicas; no se presenta este código como código oficial de Altar.
La especificación de Thunder/Windweaver es [el documento del usuario](specs/user-elemental-abilities.md).
