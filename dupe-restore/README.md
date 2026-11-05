# DupeRestore 1.1.0

Plugin independiente para **Paper 26.2 / Java 25**. La nueva implementación parte
de las fuentes 1.0.0 y conserva el hilo con origen directo `waterlogged`.
El JAR administra el hilo mediante la API pública; TNT, alfombras, raíles y ganchos
usan las opciones oficiales de Paper. El plugin solo consulta esas opciones.

La descarga 1.1.0 se publicará después de sus comprobaciones de Windows.

## Instalación

1. Detené Paper.
2. En `config/paper-global.yml`, editá estas dos opciones de la sección existente:

   ```yaml
   unsupported-settings:
     allow-piston-duplication: true
     skip-tripwire-hook-placement-validation: true
   ```

3. Reemplazá el JAR anterior por `DupeRestore-1.1.0.jar` en `plugins/` e iniciá Paper.
4. Consultá `/duperestore status` como operador o desde la consola.

La primera opción de Paper agrupa TNT, alfombras y raíles. La segunda permite
los dupes nativos de ganchos compatibles con la física vanilla 26.2.

`plugins/DupeRestore/config.yml` controla solo `string-duplication`.
`/duperestore reload` recarga el hilo. Las claves antiguas del plugin para pistones
y ganchos se ignoran, con un aviso; ahora se configuran en Paper y requieren reinicio.
El comando muestra el valor **configurado en el archivo**, no un valor leído de
campos internos de la JVM. Quitar el plugin detiene el hilo; las opciones nativas
continúan como las hayas configurado en Paper.

## Granja de hilo

Colocá dos ganchos enfrentados y una línea de hasta 40 bloques de hilo. Una
trampilla, escalera u otro bloque anegado debe dejar salir agua **directamente
hacia el hilo**. Dejá salida hacia abajo para que no arrastre los ganchos.

Produce continuamente mientras salga agua de ese bloque. Agua común y corrientes
intermedias no activan la reposición. El hilo cae normalmente y se repone un tick
después si la línea y el origen siguen siendo válidos. No requiere tijeras.
Se respetan flujos cancelados, drops suprimidos, bloques nuevos y chunks descargados.
Las roturas manuales, por pistones y explosiones no reponen hilo.

## Cambio de implementación y alerta local

El usuario informó una detección local de 1.0.1, sin nombre de amenaza ni archivo
precisos. Se retira su descarga recomendada; no se ha identificado la causa.
Se eliminaron del JAR el puente reflectivo y la modificación de opciones internas.
El nuevo JAR no incorpora ejecutables auxiliares ni scripts de instalación.

La inspección de referencias de clases y los análisis de Defender se documentan
en [VERIFY.md](VERIFY.md). Una comprobación en GitHub no garantiza el resultado
en otra PC ni demuestra que la alerta anterior fuera falsa. Los archivos que
Windows bloquee deben quedar en cuarentena mientras se revisan sus detalles.

## Desarrollo

```bash
bash gradlew -p dupe-restore --no-daemon clean build
python3 dupe-restore/tools/audit_jar.py dupe-restore/build/libs/DupeRestore-1.1.0.jar --output dupe-restore/build/reports/jar-audit.json
```

El ZIP incluye JAR, configuración del hilo y LEEME. `nativeProbeJar` genera un
plugin de prueba separado, excluido de la distribución, para mundos desechables:
crea granjas y apaga el servidor. Ese probe puede leer internamente el servidor
para diagnóstico; el JAR de producción solo usa la API pública.
