# LegendaryWeapons — Crafting PvP

Plugin **independiente** para **Paper 26.2 / Java 25 / Gradle 9.2.1**. Incluye únicamente las ocho armas y su evento de fabricación. Usa modelos Java nativos y no requiere BigCasares, Vault, EssentialsX ni BetterModel. Si se usa BetterModel para otros plugins del servidor, la versión solicitada para 26.2 es 3.2.0; esta distribución no llama a su API. WorldGuard es opcional. Los equipos usan el scoreboard vanilla. No incluye integración Bedrock/Geyser.

**Actualización 2.0.7**: Bloodlust mantiene Fuerza III durante 10 s y añade **medio corazón de curación por golpe directo** con la espada mientras la habilidad está activa. Shadow Blade baja a **6 puntos mágicos (3 corazones antes de reducciones)**, también al actualizar desde una configuración con 14. Conserva sus efectos y teletransporte. Earthbreaker, Thunder Spear, modelos, recetas y datos persistentes no cambian.

**Instalación 2.0.7 en revisión:** una captura confirma `Trojan:Script/Ulthar.A!ml` en `Armas-Legendarias-2.0.7.zip`, estado «Quitado», el 17/10/2026 a las 23:29 del equipo del usuario. Se suspende la recomendación de instalarla. Los archivos y [sus hashes](downloads/SHA256SUMS-2.0.7.txt) se conservan para revisión. Las 74 pruebas, la validación en Paper y el análisis Windows sin detecciones son resultados limitados al entorno de prueba; no resuelven el aviso local. [Registro y límites](SECURITY-REVIEW-2.0.7.md).

El robo aprovecha el daño del golpe normal, sin añadir daño extra al objetivo. Funciona contra jugadores y entidades vivas permitidas, excluyendo maniquíes y objetivos protegidos. No cura si el golpe se cancela o no hace daño de salud; los objetivos secundarios del barrido no multiplican la curación. No funciona al cambiar de arma ni con proyectiles u otras habilidades. La curación nativa respeta el máximo de salud y los eventos de curación cancelados. Duración 200 ticks y cooldown 40 s, conservados.

### Earthbreaker y Shadow Blade en 2.0.7

- **Earthbreaker:** conserva el cráter de radio 8 y profundidad 4/3/2/1. Aplica un único golpe de **4 puntos de salud (dos corazones)** a enemigos dentro de radio 8 y altura ±4, al activar el poder. Ignora armadura, Protección, Resistencia, bloqueo y absorción; los corazones amarillos no sustituyen esa pérdida de salud. Respeta equipos, regiones, invulnerabilidad y eventos de daño cancelados. Las caídas posteriores son daño vanilla aparte. La selección usa el límite existente `max-targets-per-pulse` (12 por defecto).
- **Shadow Blade:** conserva el alcance de 16 y el teletransporte seguro detrás del rival. Golpe de **6 puntos mágicos (3 corazones antes de reducciones)**, **Debilidad II por 4 s** y **Oscuridad por 3 s** si el golpe es aceptado. El lanzador recibe **Invisibilidad y Velocidad III por 5 s**; sin rival, mantiene el desplazamiento corto seguro y esos beneficios. No atraviesa destinos inseguros ni regiones protegidas. La invisibilidad es vanilla: el equipo sigue siendo visible. El daño usa ahora `shadow-blade.ambush-damage` (6 por defecto y máximo), de modo que el valor 14 de 2.0.6 queda limitado a 6 al actualizar; `strike-damage` deja de usarse. Cooldowns de 40 s y las demás habilidades conservados.

## Evento

1. Coloca los nueve ingredientes de una receta en una mesa de crafteo vanilla. Es una receta sin forma: cada casilla lleva un ingrediente (no acumules los cuatro del mismo tipo en una sola casilla).
2. Haz clic en la vista previa «Iniciar fabricación». Se consumen una unidad por casilla y la mesa queda registrada. **No recibes el arma ni un objeto provisional**. Shift-clic también inicia únicamente un evento.
3. Un contador y partículas aparecen encima de la mesa normal. No se construyen altares. El evento dura **1.200 segundos reales**, sin propietario ni bonificaciones para el fabricante.
4. A los **5 minutos** se anuncia un cuadrado de **1.000×1.000**; a los **10**, uno de **500×500** dentro del anterior. Ambos incluyen la mesa. Sus desplazamientos aleatorios se guardan al inicio. A los **15**, se revelan X/Y/Z. Cada pista indica la dimensión; no modifica el WorldBorder.
5. A los **20 minutos**, aparece el modelo horizontal sobre la mesa. Cualquier jugador cercano puede reclamarlo haciendo clic derecho sobre el modelo o la mesa. Necesita un espacio libre de inventario y respetar las protecciones del servidor.
6. Solo el primer reclamo obtiene un arma. El anuncio público **no identifica al ganador**. Cada arma puede fabricarse una sola vez por servidor, incluso si posteriormente se pierde. Armas distintas pueden tener eventos simultáneos, cada uno en su propia mesa.

El fabricante puede morir, alejarse o desconectarse; el evento continúa. El diario guarda los estados AVAILABLE (ausencia de reserva), CRAFTING, READY_TO_CLAIM y CLAIMED. Los relojes continúan durante reinicios y descargas de chunks. Un chunk descargado no se mantiene cargado: la disponibilidad se registra y el modelo se restaura al cargarlo.

La protección alcanza únicamente la mesa y la celda inmediatamente superior. Impide roturas, colocaciones, pistones, explosiones, fluidos y cambios de bloques en esas dos celdas; permite construir defensas y combatir alrededor según las reglas normales. La protección acaba al completar el reclamo.

## Recetas y habilidades

| ID | Ingrediente base ×1 | Material ×4 | Material ×2 | Material ×2 | Habilidad |
| --- | --- | --- | --- | --- | --- |
| bloodlust | Netherite Sword | Redstone Block | Ghast Tear | Netherite Block | Furia de Sangre |
| frost_scythe | Diamond Hoe | Blue Ice | Heart of the Sea | Diamond Block | Tormenta Glacial |
| shadow_blade | Netherite Sword | Ender Pearl | Phantom Membrane | Crying Obsidian | Asesino de Sombras |
| hyperion | Blaze Rod | Amethyst Block | Nether Star | Gold Block | Destrucción Arcana |
| windweaver | Mace | Wind Charge | Feather | Iron Block | Impulso Divino |
| earthbreaker | Netherite Axe | Deepslate | Emerald Block | Netherite Ingot | Terremoto Titán |
| void_katana | Diamond Sword | Ender Eye | Chorus Fruit | Purpur Block | Dimensión del Vacío |
| thunder_spear | Netherite Spear | Lightning Rod | Heart of the Sea | Copper Block | Jabalina del Trueno |

Cada arma conserva una habilidad de clic derecho y cooldown persistente de **40 segundos** por jugador y habilidad. Thunder Spear usa `NETHERITE_SPEAR` con **Lunge III nativo** a la izquierda; a la derecha se lanza inmediatamente, genera una tormenta de 10 segundos y regresa. No se sustituye por un tridente.

### Thunder Spear reconstruida en 2.0.5

Al impactar crea un campo fijo de radio 7 durante 10 s, hasta 4 bloques arriba/abajo. Paraliza a los rivales y al lanzador dentro del área: bloquea caminar, saltar, empujones y escapes de juego mediante teletransporte. Permite mirar y atacar; conserva protecciones, equipos y escape mediante comandos administrativos.

Siguen cayendo rayos visuales donde quedó clavada. **Solo la entidad alcanzada directamente por la lanza recibe los cinco pulsos eléctricos** de 7 puntos antes de reducciones. Estar dentro del círculo sin ese impacto paraliza sin daño eléctrico. Al finalizar o cancelar se libera a todos y la lanza regresa sin duplicarse. Lunge III y el cooldown de 40 s se conservan. La selección por tick está limitada a 64 entidades más el lanzador, configurable.

La base es el commit `d75fa8bc19002f362df5732a430ec83e7bd84c1f`; las otras siete armas, recetas, modelos y datos son los de 2.0.3. Las concesiones de inmovilidad y su adaptador Bukkit se han escrito de nuevo; no se ha renombrado el JAR 2.0.4.

### Habilidades actualizadas en 2.0.3

| Arma | Efecto por defecto |
| --- | --- |
| Bloodlust | **Fuerza III durante 10 s**, mediante el efecto nativo. Sustituye el multiplicador y robo de vida anteriores. |
| Frost Scythe | Aura de **radio 6 durante 8 s**: aplica **Lentitud IV y congelación nativa** directamente, sin requerir un golpe. Incluye jugadores y cualquier tipo de entidad viva: animales, aldeanos, mascotas y mobs hostiles. Excluye al lanzador y respeta equipos y protecciones. Hasta 64 objetivos por actualización, configurable. |
| Shadow Blade | Teletransporte detrás del objetivo a un máximo de 16 bloques y **10 puntos de daño mágico inmediato**, Oscuridad durante 2 s y Velocidad II durante 3 s. Sin enemigo, conserva el desplazamiento corto a un lugar seguro. |
| Hyperion | Dispara **cinco bolas de fuego grandes de ghast**, separadas por 0,25 s, hacia donde apuntas en cada disparo. Son proyectiles vanilla con colisión y explosión reales; potencia de explosión 2 por defecto. Se eliminan al finalizar, morir o desconectar. Las explosiones respetan protección de entidades, regiones, bloques y mesas activas. `ignite-blocks` permite fuego en bloques, desactivado por defecto. |
| Windweaver | Impulso vertical para preparar un smash de maza, conservando movimiento horizontal. Altura aproximada de 9 bloques desde suelo llano. Protege la primera caída del salto hasta aterrizar o diez segundos como máximo; no aplica levitación ni ralentiza la caída. |
| Earthbreaker | Abre un **cráter circular de radio 8 bajo el jugador**, sin daño directo. Profundidad 4 a menos de 2 bloques del centro; 3 entre 2 y 4; 2 entre 4 y 6; 1 entre 6 y 8 inclusive. Elimina terreno permanentemente por lotes, sin drops; conserva bloques irrompibles, contenedores, datos de bloques y celdas protegidas. Retirar el suelo puede causar caídas normales. Requiere suelo a un máximo de cuatro bloques bajo los pies. |
| Void Katana | Habilidad conservada. |
| Thunder Spear | Base anterior; sustituida por el campo descrito arriba en 2.0.5. |

**Dos puntos equivalen a un corazón antes de reducciones.** Shadow Blade conserva daño mágico; Protección y Resistencia siguen reduciéndolo. Frost aplica estados directamente y usa la congelación vanilla, que puede producir daño nativo de frío. Hyperion usa el daño de impactos y explosiones vanilla. Las habilidades de las otras cinco armas conservan su funcionamiento. `/weapons info <arma>` explica cada poder.

Para actualizar desde 2.0.1 o 2.0.2, detén Paper, sustituye el JAR y conserva la carpeta `plugins/LegendaryWeapons`. No borres el diario ni los datos de jugadores: las armas existentes usan las habilidades nuevas y mantienen sus cooldowns. Las claves nuevas de configuración tienen valores predeterminados aunque el archivo existente no las contenga. Las claves anteriores de daño de Frost, Hyperion y Earthbreaker dejan de usarse, al igual que las antiguas de multiplicador/curación de Bloodlust y bonus de Shadow Blade. Los IDs persistentes de habilidades se conservan.

## Instalación y prueba local en Windows

1. Instala Java 25 y descarga Paper 26.2 desde https://papermc.io/downloads/paper (versiones anteriores).
2. En una carpeta nueva, guarda Paper como `paper.jar`. Ejecuta `java -Xms1G -Xmx2G -jar paper.jar --nogui`.
3. Lee el EULA en `eula.txt` y, si lo aceptas, cambia `eula=false` por `eula=true`.
4. Copia **`LegendaryWeapons-2.0.7.jar`** en `plugins` y vuelve a iniciar Paper.
5. En la consola escribe `op TuNombre`. En Minecraft Java **26.2**, entra a Multijugador → Conexión directa → `localhost`; acepta el paquete de recursos.
6. Consulta `/weapons info thunder_spear`, consigue los materiales y fabrica en una mesa. `/weapons events` muestra disponibilidad y tiempo sin filtrar coordenadas anticipadas.

El plugin va en `plugins`, no en `mods`. Lunar puede conectarse si ofrece esa versión; su Host World no carga este plugin. No instales esta distribución junto a BigCasares ni junto a LegendaryWeapons 1.x: comparten comandos e identificadores. Usa un mundo y carpeta de plugin nuevos. No migra automáticamente altares o armas anteriores; detecta registros anteriores de altares/fabricación y detiene el arranque conservando esos datos.

`/weapons give` y `/weapons altar` no forman parte de este modo: no hay entrega manual ni un comando para volver a habilitar una fabricación. `/weapons reload` y `/weapons cooldown reset <jugador>` requieren `bigcasares.weapons.admin` (operadores por defecto). Recargar no reinicia eventos, zonas ni cooldowns. Los cambios de puerto/URL del pack necesitan reiniciar.

## Modelos

La versión **2.0.1** rediseña las ocho armas con metal oscuro, bordes de color, gemas y siluetas distintas, inspiradas en la referencia de inventario del usuario. Consulta [la galería y el detalle de los modelos](MODELS.md). No modifica los modelos de cobre.

El JAR incluye el pack Java de las ocho armas y lo sirve por defecto en `http://127.0.0.1:8124/legendary-pack.zip`. Esto sirve para jugar en la misma PC. También se genera `build/packs/legendary-weapons-java.zip` para instalación manual en `resourcepacks`.

Para otros equipos, configura `resource-pack.bind-address: 0.0.0.0`, una `resource-pack.url` accesible para ellos y acceso al puerto 8124. O desactiva `resource-pack.enabled` y distribuye el ZIP manualmente. Se usa formato 88 para 26.2.

## Persistencia y límites operativos

El diario atómico es `plugins/LegendaryWeapons/data/legendary/state.properties`. Guarda zonas, ubicaciones, plazos, avisos y transacciones pendientes. Conservarlo junto a todos los datos de jugadores en copias de seguridad; restaurar solo una parte no garantiza consistencia.

El inventario de la mesa se devuelve primero a almacenamiento del jugador y se verifica su guardado antes de reservar/consumir; se exige espacio suficiente para evitar drops. La reserva y el consumo previsto se registran juntos. Una caída antes de confirmar el consumo se recupera al reconectar; el evento sigue avanzando mientras tanto. El premio se reserva antes de entregarlo y se confirma tras verificar el inventario nativo guardado. Los marcadores evitan reaplicar una entrega ya guardada. Ante fallos de persistencia, se bloquean las interacciones del jugador afectado y se desconecta para recuperación. Esto solo afecta transacciones incompletas; el fabricante no tiene protección durante el evento normal.

Los avisos se confirman antes de publicarse para evitar duplicarlos al reiniciar. Una caída entre confirmación y emisión puede omitir un aviso; no se promete entrega exactamente una vez en clientes externos. La aparición se procesa en el primer tick disponible al alcanzar el plazo real; un servidor detenido o con lag no puede dibujar un modelo durante ese intervalo. No se cambia el plazo de 1.200 segundos.

## Desarrollo

Desde `standalone/` con Java 25: `bash gradlew --no-daemon --max-workers=4 clean build` (Windows: `gradlew.bat build`). Artefacto: `build/libs/LegendaryWeapons-2.0.7.jar`.

Consulta [los resultados y capturas](VERIFY.md).

Las pruebas automatizadas cubren reloj, límites negativos, zonas inclusivas anidadas y persistentes, reserva global, concurrencia de reclamos, independencia entre armas, anonimato, espacio protegido, consumo de ingredientes y recuperación de inventarios ante fallos. La validación de servidor real se documenta en el plan de implementación; no equivale a validar todas las integraciones externas.
