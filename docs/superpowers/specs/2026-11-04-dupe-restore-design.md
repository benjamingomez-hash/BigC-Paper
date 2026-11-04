# DupeRestore — plugin independiente

Solicitud: restaurar dupes de TNT, ganchos, hilo y alfombras sin depender de
BigCasares ni de LegendaryWeapons. Plataforma existente: Paper 26.2 / Java 25.
Proyecto separado `dupe-restore/`, con su propio wrapper, configuración y JAR.

Paper expone en `GlobalConfiguration.unsupportedSettings` las opciones nativas
`allowPistonDuplication` (TNT, alfombras y también raíles) y
`skipTripwireHookPlacementValidation` (ganchos). El plugin toma una copia de sus
valores y los habilita en memoria; no reescribe `paper-global.yml`. Al descargarse
restaura los valores originales si continúan bajo su control. Acceso reflectivo
acotado a esos dos campos públicos, comprobando compatibilidad antes de escribir.

El cambio vanilla del hilo requiere una reimplementación: únicamente cuando el
agua rompe un tripwire conectado entre dos ganchos enfrentados, Paper suelta el
hilo normalmente y el plugin repone ese bloque un tick después. No hay entrega
por comandos, generación al romper a mano ni tareas por jugador. Se respetan los
flujos cancelados y las listas de drops vacías. Se comprueban de nuevo chunks,
posición, ganchos y línea antes de reponer; no se pisan bloques nuevos ni se
cargan chunks. Las reposiciones se agrupan por tick y posición y se vacían al
recargar o desactivar. No se prometen todos los diseños históricos de string.

Verificación: pruebas unitarias de geometría, reposición y restauración de
opciones; compilación limpia; prueba aislada con Paper 26.2 y mecanismos reales.
La distribución contendrá solo JAR, configuración de referencia y LEEME. No se
modificarán mundos existentes, datos de armas ni la rama de LegendaryWeapons.
