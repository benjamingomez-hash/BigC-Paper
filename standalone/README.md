# LegendaryWeapons — Crafting PvP

Plugin **independiente** para **Paper 26.2 / Java 25 / Gradle 9.2.1**. El código actual incluye cinco armas y su evento de fabricación. Usa modelos Java nativos y no requiere BigCasares, Vault, EssentialsX ni BetterModel. Si se usa BetterModel para otros plugins del servidor, la versión solicitada para 26.2 es 3.2.0; esta distribución no llama a su API. WorldGuard es opcional. Los equipos usan el scoreboard vanilla. No incluye integración Bedrock/Geyser.

**Última versión publicada: `2.0.16-final.1`.**
[Descargar ZIP final](https://github.com/benjamingomez-hash/BigC-Paper/releases/download/legendary-weapons-2.0.16-final.1/Armas-Legendarias-Final.zip) ·
[Publicación en GitHub](https://github.com/benjamingomez-hash/BigC-Paper/releases/tag/legendary-weapons-2.0.16-final.1) ·
[Instrucciones y archivos finales](downloads/2.0.16-final.1/README.md).

El ZIP contiene el JAR, pack, configuración y LEEME terminados por el usuario,
sin modificaciones. La carpeta de descargas mantiene únicamente esta versión.
El JAR publicado no se reconstruyó desde las fuentes 2.0.16 de esta rama.

Incluye Bloodlust, Shadow Blade, Windweaver, Earthbreaker y Thunder Spear. Frost,
Void Katana e Hyperion están retiradas. Las animaciones están desactivadas por
defecto en la configuración final.

**Niveles y poderes:** una baja de jugador suma un nivel por jugador y arma,
hasta 5, sin perderlo al morir.
[Controles, efectos, cooldowns y configuración](ABILITIES.md).

Al actualizar, conservá los inventarios, el diario y los datos de jugadores.
Los recursos históricos permanecen para que los objetos antiguos no pierdan
su representación. [Comprobaciones de esta entrega](VERIFY.md).

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
| bloodlust | Netherite Sword | Redstone Block | Ghast Tear | Netherite Block | Bloodlust |
| shadow_blade | Netherite Sword | Ender Pearl | Phantom Membrane | Crying Obsidian | Shadow Daggers / Leap |
| windweaver | Mace | Wind Charge | Feather | Iron Block | Cinco niveles de viento |
| earthbreaker | Netherite Axe | Deepslate | Emerald Block | Netherite Ingot | Earthshatter / Stalwart |
| thunder_spear | Netherite Spear | Lightning Rod | Heart of the Sea | Copper Block | Cinco niveles de trueno |

Las recetas y el evento no cambian con los niveles. Para poderes y controles,
consultá [Habilidades y niveles](ABILITIES.md) o `/weapons info <arma>`.
La lanza mantiene su material `NETHERITE_SPEAR` y Lunge III; no se transforma en
un tridente. Sus habilidades nuevas no retiran el arma del inventario.

## Instalación y prueba local en Windows

1. Instala Java 25 y descarga Paper 26.2 desde https://papermc.io/downloads/paper (versiones anteriores).
2. En una carpeta nueva, guarda Paper como `paper.jar`. Ejecuta `java -Xms1G -Xmx2G -jar paper.jar --nogui`.
3. Lee el EULA en `eula.txt` y, si lo aceptas, cambia `eula=false` por `eula=true`.
4. Copia **`LegendaryWeapons-Final.jar`** en `plugins` y vuelve a iniciar Paper. Guarda cualquier JAR anterior fuera de `plugins`; conserva el diario y los datos de jugadores al actualizar desde 2.0.x.
5. En la consola escribe `op TuNombre`. En Minecraft Java **26.2**, entra a Multijugador → Conexión directa → `localhost`; acepta el paquete de recursos.
6. Consulta `/weapons info thunder_spear`, consigue los materiales y fabrica en una mesa. `/weapons events` muestra disponibilidad y tiempo sin filtrar coordenadas anticipadas.

El plugin va en `plugins`, no en `mods`. Lunar puede conectarse si ofrece esa versión; su Host World no carga este plugin. No instales esta distribución junto a BigCasares ni junto a LegendaryWeapons 1.x: comparten comandos e identificadores. Usa un mundo y carpeta de plugin nuevos. No migra automáticamente altares o armas anteriores; detecta registros anteriores de altares/fabricación y detiene el arranque conservando esos datos.

`/weapons give` y `/weapons altar` no forman parte de este modo: no hay entrega manual ni un comando para volver a habilitar una fabricación. `/weapons reload` y `/weapons cooldown reset <jugador>` requieren `bigcasares.weapons.admin` (operadores por defecto). Recargar no reinicia eventos, zonas ni cooldowns. Los cambios de puerto/URL del pack necesitan reiniciar.

## Modelos

Se usan los cinco proyectos Blockbench aportados, sus texturas originales y once animaciones. [Fuentes y exportación](models/reference-study/README.md). No modifica los modelos de cobre.

El JAR incluye el pack Java de las cinco armas y los recursos históricos de compatibilidad, incluida Frost, y lo sirve por defecto en `http://127.0.0.1:8124/legendary-pack.zip`. Esto sirve para jugar en la misma PC. También se genera `build/packs/legendary-weapons-java.zip` para instalación manual en `resourcepacks`.

Para otros equipos, configura `resource-pack.bind-address: 0.0.0.0`, una `resource-pack.url` accesible para ellos y acceso al puerto 8124. O desactiva `resource-pack.enabled` y distribuye el ZIP manualmente. Se usa formato 88 para 26.2.

## Persistencia y límites operativos

El diario atómico es `plugins/LegendaryWeapons/data/legendary/state.properties`. Guarda zonas, ubicaciones, plazos, avisos y transacciones pendientes. Conservarlo junto a todos los datos de jugadores en copias de seguridad; restaurar solo una parte no garantiza consistencia.

El inventario de la mesa se devuelve primero a almacenamiento del jugador y se verifica su guardado antes de reservar/consumir; se exige espacio suficiente para evitar drops. La reserva y el consumo previsto se registran juntos. Una caída antes de confirmar el consumo se recupera al reconectar; el evento sigue avanzando mientras tanto. El premio se reserva antes de entregarlo y se confirma tras verificar el inventario nativo guardado. Los marcadores evitan reaplicar una entrega ya guardada. Ante fallos de persistencia, se bloquean las interacciones del jugador afectado y se desconecta para recuperación. Esto solo afecta transacciones incompletas; el fabricante no tiene protección durante el evento normal.

Los avisos se confirman antes de publicarse para evitar duplicarlos al reiniciar. Una caída entre confirmación y emisión puede omitir un aviso; no se promete entrega exactamente una vez en clientes externos. La aparición se procesa en el primer tick disponible al alcanzar el plazo real; un servidor detenido o con lag no puede dibujar un modelo durante ese intervalo. No se cambia el plazo de 1.200 segundos.

## Desarrollo

Desde `standalone/` con Java 25: `bash gradlew --no-daemon --max-workers=4 clean build` (Windows: `gradlew.bat build`). Artefacto: `build/libs/LegendaryWeapons-2.0.16.jar`.

Consulta [los resultados y capturas](VERIFY.md).

Las pruebas automatizadas cubren reloj, límites negativos, zonas inclusivas anidadas y persistentes, reserva global, concurrencia de reclamos, independencia entre armas, anonimato, espacio protegido, consumo de ingredientes y recuperación de inventarios ante fallos. La validación de servidor real se documenta en el plan de implementación; no equivale a validar todas las integraciones externas.
