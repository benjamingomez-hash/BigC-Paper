# BIGCASARES — LEGENDARY WEAPONS: CRAFTING PvP EVENT

Repositorio:
https://github.com/benjamingomez-hash/BigC-Paper

## OBJETIVO

Implementar un sistema de fabricación competitiva de armas legendarias inspirado en los eventos de Altar SMP.

IMPORTANTE: El arma NO se entrega al jugador que la craftea. Tampoco aparece en su inventario durante el evento.

El arma aparece físicamente sobre la mesa de crafteo únicamente cuando terminan los 20 minutos.

Cualquier jugador puede conseguirla.

**La identidad del jugador que finalmente consiga el arma debe permanecer en secreto.**

## 1. INICIO DE FABRICACIÓN

El jugador debe reunir los materiales correspondientes a una de las ocho armas legendarias.

Utilizar una mesa de crafteo vanilla normal.

Cuando complete la receta:

1. Consumir los materiales.
2. Registrar la ubicación exacta de la mesa de crafteo.
3. Reservar globalmente esa arma.
4. Iniciar un temporizador de 20 minutos.
5. Anunciar el inicio del evento a todo el servidor.
6. NO entregar el arma.
7. NO entregar ningún objeto inestable.
8. Mantener la mesa como punto central del evento.

El jugador que inició el crafteo puede quedarse en la zona, construir defensas y prepararse para proteger la mesa.

No recibe ninguna ventaja artificial de daño o protección.

Su única ventaja es conocer la ubicación desde el principio y poder preparar la defensa.

## 2. MESA DE CRAFTEO LEGENDARIA

Cuando comienza el evento, la mesa de crafteo se convierte temporalmente en una estación de fabricación legendaria.

La mesa debe seguir siendo visualmente una CRAFTING_TABLE normal.

No generar altares, templos ni estructuras grandes.

Sobre la mesa debe aparecer un indicador visual del proceso:

- Partículas temáticas.
- Un TextDisplay con el nombre del arma.
- Un contador con el tiempo restante.
- Efectos visuales que aumentan al acercarse la finalización.

El objeto legendario NO debe aparecer todavía.

La mesa y el punto de aparición del arma deben quedar registrados mediante coordenadas persistentes.

## 3. TEMPORIZADOR DE 20 MINUTOS

La fabricación dura exactamente 1200 segundos.

El temporizador comienza inmediatamente después de consumir los materiales.

El evento continúa aunque:

- El creador se aleje.
- El creador muera.
- El creador se desconecte.
- Otros jugadores lleguen a la mesa.
- La zona quede temporalmente descargada.

Guardar los tiempos de inicio y finalización de forma persistente.

No depender exclusivamente de contar ticks.

Un reinicio no debe permitir duplicar ni reiniciar la fabricación.

## 4. SISTEMA DE REVELACIÓN PROGRESIVA DE UBICACIÓN

IMPORTANTE: Reemplazar completamente el sistema anterior de revelación individual de coordenadas X y Z.

No quiero revelar X a los 5 minutos y Z a los 10 minutos.

Quiero revelar áreas cuadradas de búsqueda que se van reduciendo a medida que avanza el evento.

El sistema debe utilizar la ubicación ORIGINAL de la mesa de crafteo.

NO utilizar la ubicación actual del jugador.

### MINUTO 0 — INICIO

Enviar globalmente:

"⚔ ¡SE HA INICIADO UNA FABRICACIÓN LEGENDARIA!"

"Arma: THUNDER SPEAR"

"⏳ Tiempo restante: 20 minutos."

"📍 La primera zona de búsqueda se revelará en 5 minutos."

No revelar coordenadas.

### MINUTO 5 — CUADRADO DE 1000×1000

Revelar un área de búsqueda cuadrada de 1000×1000 bloques.

Ejemplo:

"📍 ¡PRIMERA ZONA REVELADA!"

"⚔ Arma: THUNDER SPEAR"

"🗺️ Área de búsqueda: 1000×1000 bloques"

"X: entre 1000 y 1999"

"Z: entre -1000 y -1"

"Dimensión: Overworld"

El área debe contener siempre la mesa de crafteo.

No debe revelar la coordenada exacta del arma.

Los valores son ejemplos y deben calcularse usando la ubicación real.

### MINUTO 10 — CUADRADO DE 500×500

Revelar un nuevo cuadrado de 500×500 bloques.

Este cuadrado debe estar completamente contenido dentro del área de 1000×1000 revelada anteriormente.

También debe contener la mesa de crafteo.

Ejemplo:

"⚡ ¡LA ZONA SE HA REDUCIDO!"

"⚔ Arma: THUNDER SPEAR"

"🗺️ Nueva área: 500×500 bloques"

"X: entre 1250 y 1749"

"Z: entre -750 y -251"

"Dimensión: Overworld"

No revelar todavía la altura exacta ni la ubicación precisa de la mesa.

### MINUTO 15 — COORDENADAS EXACTAS

Revelar las coordenadas completas de la mesa de crafteo.

Ejemplo:

"🚨 ¡UBICACIÓN LEGENDARIA REVELADA!"

"⚔ Arma: THUNDER SPEAR"

"📍 X: 1427 | Y: 84 | Z: -623"

"🌍 Dimensión: Overworld"

"⏳ ¡Quedan 5 minutos para que aparezca el arma!"

Todos los jugadores tienen cinco minutos para llegar al lugar y disputar la zona.

### MINUTO 20 — APARICIÓN

Enviar globalmente:

"⚔ ¡THUNDER SPEAR HA APARECIDO!"

"🏆 ¡El arma legendaria ya está disponible para reclamar!"

### LÓGICA DE LAS ZONAS

Implementar un algoritmo que:

1. Obtenga las coordenadas X y Z reales de la mesa.
2. Calcule un cuadrado de 1000×1000 que contenga esa posición.
3. Calcule dentro del anterior un cuadrado de 500×500 que también contenga esa posición.
4. Revele los límites de cada área en el momento correspondiente.
5. Revele las coordenadas exactas a los 15 minutos.

Los cuadrados no deben estar obligatoriamente centrados en la mesa, porque eso permitiría deducir la ubicación exacta a partir de los límites.

Utilizar un desplazamiento aleatorio controlado para que la ubicación de la mesa sea impredecible dentro de cada área revelada.

El desplazamiento debe ser persistente: los límites de las zonas se calculan y guardan al comenzar el evento.

No recalcularlos aleatoriamente cada vez que se envía un mensaje o se reinicia el servidor.

Requisitos adicionales:

- El cuadrado de 500×500 siempre debe estar dentro del de 1000×1000.
- Ambos cuadrados deben incluir la mesa.
- Utilizar intervalos enteros inclusivos que correspondan exactamente a 1000 y 500 bloques.
- Evitar errores con coordenadas negativas.
- Informar siempre la dimensión correspondiente.
- No revelar la ubicación mediante mensajes de depuración públicos.
- Evitar mostrar la posición exacta antes del minuto 15.

No es necesario modificar el WorldBorder del servidor. Las zonas son pistas de búsqueda, no barreras físicas.

## 5. APARICIÓN DEL ARMA A LOS 20 MINUTOS

Esta es la mecánica MÁS IMPORTANTE.

Al terminar el temporizador:

1. Finalizar la animación de fabricación.
2. Generar una representación real del arma legendaria sobre la mesa de crafteo.
3. Mostrarla como si estuviera en un ItemFrame horizontal encima de la mesa.
4. El arma debe verse claramente.
5. Debe poder reclamarse mediante click derecho.
6. El primer jugador que la reclame correctamente recibe el arma.
7. El objeto visual desaparece.
8. Anunciar globalmente que el arma fue reclamada, SIN revelar quién la consiguió.

Ejemplo de anuncio:

"⚔ ¡THUNDER SPEAR HA SIDO RECLAMADA!"

NO incluir el nombre ni ningún dato que identifique al jugador que la reclamó.

NO entregar automáticamente el arma al jugador que inició la fabricación.

NO utilizar un sistema de propietario previo.

NO convertir el evento en un objeto que se puede robar durante los 20 minutos.

La competencia ocurre alrededor de la mesa, y el objetivo es controlar el lugar para reclamar el arma cuando aparezca.

Utilizar ItemDisplay o un ItemFrame invisible, según cuál resulte más estable para Paper 26.2.

La representación visual no debe ser un ItemStack que pueda duplicarse mediante mecánicas vanilla.

El reclamo debe ser controlado por el servidor y ser atómico: solamente un jugador puede ganar.

## 6. PROTECCIÓN DEL PUNTO DE FABRICACIÓN

Mientras la fabricación está activa:

- No permitir romper la mesa registrada.
- No permitir moverla mediante pistones.
- No permitir reemplazarla con explosiones.
- No permitir que fluidos destruyan la estación.
- No permitir duplicar la estación.
- No permitir mover el punto donde aparecerá el arma.

Esta protección se aplica únicamente a la mesa y al espacio necesario para representar el arma.

NO proteger automáticamente a los jugadores.

NO impedir PvP alrededor de la mesa.

NO crear una región invulnerable alrededor de la estación.

Los jugadores deben poder construir defensas, atacar y disputar la zona conforme a las reglas normales del servidor.

## 7. UNA SOLA UNIDAD POR ARMA

Cada una de las ocho armas legendarias solo puede fabricarse UNA vez en todo el servidor.

Estados:

AVAILABLE
CRAFTING
READY_TO_CLAIM
CLAIMED

AVAILABLE: todavía no se fabricó.

CRAFTING: temporizador de 20 minutos activo.

READY_TO_CLAIM: el arma ya apareció sobre la mesa.

CLAIMED: un jugador la reclamó y no puede volver a fabricarse.

La receta debe bloquearse desde que comienza la primera fabricación.

Si el arma está pendiente de ser reclamada, nadie puede iniciar otro crafteo.

Si el arma fue reclamada, nadie puede crear otra mediante fabricación normal.

Guardar este estado de manera persistente.

## 8. VARIOS EVENTOS SIMULTÁNEOS

Permitir que distintas armas estén siendo fabricadas al mismo tiempo.

Por ejemplo:

Bloodlust puede estar en minuto 12 mientras Thunder Spear está en minuto 7.

Cada arma debe tener:

- Su propio temporizador.
- Su propia mesa.
- Sus propias zonas de búsqueda.
- Sus propios anuncios.
- Su propio estado.

Nunca permitir dos eventos simultáneos de la misma arma.

## 9. REINICIOS Y RECUPERACIÓN

Al reiniciar el servidor:

- Recuperar los eventos activos.
- Calcular cuánto tiempo real transcurrió.
- Restaurar los indicadores visuales.
- No reiniciar el temporizador.
- Evitar mensajes duplicados.
- Mantener las zonas de búsqueda previamente calculadas.
- Crear el arma visual solo cuando corresponda.
- Conservar armas pendientes de reclamo.
- No generar copias adicionales.

Si el chunk está descargado cuando finalizan los 20 minutos, registrar READY_TO_CLAIM y mostrar el arma cuando sea posible cargar la mesa de manera segura.

No mantener chunks permanentemente cargados sin necesidad.

## 10. INTEGRACIÓN CON BIGCASARES

Mantener:

- Paper 26.2.
- Java 25.
- Gradle 9.2.1.
- BetterModel 3.2.0.
- CustomItemRegistry.
- ItemCatalogModule.
- ResourcePackModule.
- ModuleManager.
- RuntimeRegistrationScope.

Implementar dentro de LegendaryWeaponsModule.

Mantener las ocho armas y sus recetas originales.

Cada arma conserva una única habilidad especial con cooldown de 40 segundos.

Thunder Spear conserva su ataque de Lunge y su habilidad para lanzarse y generar rayos durante 10 segundos.

No crear un segundo plugin ni otro sistema de ítems.

## 11. PRUEBAS OBLIGATORIAS

Probar:

- Inicio del crafteo.
- Consumo de ingredientes.
- Ausencia del arma antes de los 20 minutos.
- Anuncio de la zona de 1000×1000 al minuto 5.
- Anuncio de la zona de 500×500 al minuto 10.
- Revelación de coordenadas exactas al minuto 15.
- Inclusión de la mesa dentro de ambas zonas.
- Inclusión del cuadrado de 500×500 dentro del de 1000×1000.
- Aleatorización que no revele el centro exacto.
- Coordenadas negativas.
- Persistencia de las zonas ante reinicios.
- Aparición del arma exactamente al finalizar.
- Reclamo por un jugador distinto al fabricante.
- Reclamo simultáneo de varios jugadores.
- Anuncio anónimo al reclamar.
- Límite global de una unidad.
- Protección de la mesa.
- Desconexión del fabricante.
- Muerte del fabricante.
- Reinicio del servidor.
- Descarga y recarga del chunk.
- Ausencia de duplicaciones.

Compilar mediante el Gradle existente y conservar todos los demás módulos de BigCasares.

## RESULTADO FINAL

Quiero que el evento funcione así:

Un jugador consigue materiales y craftea un arma legendaria.

La fabricación comienza y dura 20 minutos.

Durante ese tiempo puede construir defensas alrededor de la mesa.

A los 5 minutos, todo el servidor recibe un cuadrado de búsqueda de 1000×1000 bloques.

A los 10 minutos, se revela un cuadrado más pequeño de 500×500 dentro del primero.

A los 15 minutos, se revela la ubicación exacta de la mesa.

A los 20 minutos aparece el arma físicamente, como un objeto exhibido encima de la mesa de crafteo.

El PRIMER jugador que consiga hacer click derecho y reclamarla se queda con ella.

No importa quién inició el crafteo.

El servidor solo recibe un anuncio de que el arma fue reclamada. Nunca se anuncia el nombre del ganador.

Solo existe una fabricación legítima por tipo de arma en todo el servidor.

Ese es el sistema exacto que quiero implementar.