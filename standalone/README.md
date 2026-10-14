# LegendaryWeapons independiente

Plugin para **Paper 26.3 y Java 25**. No necesita BigCasares, BetterModel, Vault ni EssentialsX. Incluye las ocho armas, habilidades, altares, fabricación, cooldowns persistentes y recuperación de la lanza.

## Probar en tu PC

1. Instala Java 25 y descarga Paper 26.3 desde https://papermc.io/downloads/paper.
2. Crea una carpeta para el servidor y guarda Paper como `paper.jar`.
3. Ejecuta `java -Xms1G -Xmx2G -jar paper.jar --nogui`. Lee el EULA y, si lo aceptas, cambia `eula=false` a `eula=true` en `eula.txt`.
4. Copia `LegendaryWeapons-1.0.0.jar` dentro de `plugins` y vuelve a iniciar Paper.
5. En la consola escribe `op TuNombre`.
6. En Minecraft Java 26.3 entra a Multijugador → Conexión directa → `localhost` y acepta el paquete de recursos.
7. Ejecuta `/weapons give TuNombre thunder_spear`. Clic izquierdo: Lunge III nativo. Clic derecho: lanzamiento y tormenta; vuelve automáticamente y tiene 40 segundos de cooldown.

`/weapons list` muestra las ocho armas. `/weapons give TuNombre bloodlust` entrega otra. Para fabricar, en suelo plano y libre: `/weapons altar create thunder_spear`, luego interactúa con el pedestal. `/weapons help` muestra los comandos.

El JAR va en `plugins`, no en `mods`. No funciona directamente en un mundo individual. No instales simultáneamente este JAR y BigCasares: comparten comandos e identificadores persistentes. Usa un mundo nuevo para las primeras pruebas.

## Modelos y conexiones de otras PCs

El plugin sirve su paquete Java en `http://127.0.0.1:8124/legendary-pack.zip` para jugar en la misma PC. Puedes instalar manualmente `legendary-weapons-java.zip` en la carpeta `resourcepacks` de Minecraft.

Para otros jugadores, apaga el servidor y edita `plugins/LegendaryWeapons/config.yml`: cambia `resource-pack.bind-address` a `0.0.0.0` y `resource-pack.url` a una URL accesible desde sus equipos, por ejemplo `http://192.168.1.50:8124/legendary-pack.zip` en una LAN. Permite el puerto 8124 para descargar modelos y 25565 para Minecraft. Reinicia después de cambiar dirección o puerto. También puedes desactivar el servidor del pack con `resource-pack.enabled: false` y distribuir el ZIP manualmente.

Esta distribución está destinada a Java; no incluye integración Geyser/Bedrock. WorldGuard es opcional y conserva sus comprobaciones; los equipos usan el scoreboard vanilla. Las funciones de equipos y otros módulos de BigCasares no forman parte de este plugin.

## Compilar

Desde esta carpeta con Java 25: `bash gradlew --no-daemon --max-workers=4 clean build` (Windows: `gradlew.bat build`). JAR: `build/libs/LegendaryWeapons-1.0.0.jar`; pack: `build/packs/legendary-weapons-java.zip`.

No migra automáticamente los datos de BigCasares. Conserva la carpeta completa del plugin y los datos de jugadores juntos en copias de seguridad.
