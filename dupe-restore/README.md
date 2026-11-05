# DupeRestore

Plugin **independiente** para **Paper 26.2 / Java 25**. Restaura dupes sin necesitar
BigCasares, LegendaryWeapons, BetterModel ni un mod en el cliente.

**[Descargar ZIP](https://raw.githubusercontent.com/benjamingomez-hash/BigC-Paper/feat/dupe-restore-plugin/dupe-restore/downloads/DupeRestore-1.0.1.zip)** · [Solo JAR](https://raw.githubusercontent.com/benjamingomez-hash/BigC-Paper/feat/dupe-restore-plugin/dupe-restore/downloads/DupeRestore-1.0.1.jar) · [SHA-256](downloads/SHA256SUMS.txt).

Se distribuyen exactamente los archivos comprobados en Windows Defender.

| Dupe | Implementación |
| --- | --- |
| TNT | Opción nativa de Paper `allowPistonDuplication`. |
| Alfombras | La misma opción de pistones; también habilita raíles. |
| Ganchos de cuerda | Opción nativa `skipTripwireHookPlacementValidation`. |
| Hilo | Agua que sale directamente de un bloque `waterlogged`; cae su drop normal y se repone el hilo al tick siguiente. |

Paper agrupa TNT, alfombras y raíles en una sola opción. Las máquinas tienen que
ser compatibles con la física vanilla 26.2; no se recuperan todos los diseños de
bugs de versiones anteriores. El hilo usa el mecanismo descrito aquí.

## Instalar

1. Detené Paper y copiá `DupeRestore-1.0.1.jar` a `plugins/`.
2. Iniciá el servidor: queda habilitado por defecto.
3. Consultá `/duperestore status`. `/duperestore reload` aplica cambios de
   `plugins/DupeRestore/config.yml`. Permiso: `duperestore.admin`, operadores y consola.

El `config.yml` del ZIP es de referencia. El JAR genera su propia configuración
si no existe. No se reescribe `paper-global.yml`: las opciones se habilitan en
memoria y se restauran al desactivar el plugin. Una opción `false` conserva el
valor original del servidor; no fuerza a false una opción que ya estaba habilitada.
Al quitar el JAR y reiniciar quedan los valores que configuraste en Paper.

## Granja de hilo

Colocá dos ganchos enfrentados con uno o más hilos conectados entre ellos,
con un máximo de 40 bloques de hilo. Colocá una trampilla, escalera u otro bloque
**anegado (`waterlogged: true`) al lado del hilo**, de modo que el agua salga
directamente de él hacia el hilo. **Dejá una salida hacia abajo**, para que no
arrastre los ganchos. Podés usar una línea de un solo hilo y tolvas debajo.

El plugin conserva el drop normal y repone el hilo un tick después si los ganchos
y el resto de la línea siguen siendo válidos y el bloque de origen continúa
anegado. **Produce continuamente mientras salga agua de ese bloque**. El agua
común y las corrientes intermedias no activan el dupe, aunque tengan más lejos
un bloque anegado como origen. No requiere tijeras.
No repone hilo suelto, roturas a mano, por pistones ni explosiones. Respeta flujos
de agua cancelados y drops suprimidos; no pisa bloques nuevos ni carga chunks.
Las reposiciones pendientes se descartan al desactivar o recargar el plugin.

## Compilar y verificar

```bash
bash gradlew -p dupe-restore --no-daemon clean build
```

Genera `dupe-restore/build/libs/DupeRestore-1.0.1.jar` y el ZIP con JAR,
configuración y LEEME en `dupe-restore/build/distributions/`.

[Comprobaciones y alcance](VERIFY.md). El código de la prueba nativa vive en
`src/nativeTest/`; `nativeProbeJar` genera un **plugin de prueba separado**, que
no entra en el JAR distribuido. Solo se ejecuta en mundos desechables: crea
mecanismos, modifica bloques y apaga el servidor al terminar.

Opciones verificadas en el [código de Paper 26.2](https://github.com/PaperMC/Paper/blob/ver/26.2/paper-server/src/main/java/io/papermc/paper/configuration/GlobalConfiguration.java).
El puente comprueba los dos campos públicos antes de escribir; si no están
disponibles desactiva el plugin y deja un error claro en el registro.
