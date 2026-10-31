# Sistema de habilidades — Armas legendarias SMP

Implementar 5 niveles para cada arma legendaria. Cada nivel desbloquea una nueva habilidad, conservando todas las anteriores.

Las habilidades deben tener partículas, sonidos, cooldowns independientes y estar equilibradas para PvP.

## ⚡ THUNDER SPEAR — Lanza eléctrica

### Nivel 1 — Lightning Dash
**Activación:** Click izquierdo en el aire + Shift.

El jugador se impulsa rápidamente 8 bloques hacia adelante, dejando un rastro eléctrico.

- Inflige 4 HP a los enemigos atravesados.
- Aplica Slowness I durante 2 segundos.
- Partículas eléctricas celestes.
- Cooldown: 20 segundos.

### Nivel 2 — Thunder Mark
**Activación:** Click derecho.

Durante 6 segundos, los golpes con la lanza acumulan cargas eléctricas sobre un enemigo.

- Cada golpe aplica una carga.
- Al llegar a 3 cargas, se produce una descarga.
- Daño principal: 5 HP adicionales.
- Daño en área: 3 HP.
- Radio: 4 bloques.
- Máximo una descarga por activación.
- Cooldown: 40 segundos.

### Nivel 3 — Chain Lightning
**Activación:** Shift + click derecho.

Dispara un rayo eléctrico hacia el enemigo que estés mirando.

- Alcance: 15 bloques.
- Daño inicial: 6 HP.
- La electricidad salta hasta otros 3 enemigos.
- Cada salto reduce el daño un 25%.
- Radio de búsqueda entre enemigos: 5 bloques.
- Cooldown: 35 segundos.

### Nivel 4 — Thunderstorm
**Activación:** Shift + click izquierdo mientras mirás hacia arriba.

Invoca una tormenta eléctrica sobre la zona seleccionada.

- Radio: 6 bloques.
- Duración: 8 segundos.
- Cae un rayo cada 2 segundos sobre un enemigo aleatorio dentro de la zona.
- Cada rayo inflige 4 HP.
- Partículas de tormenta y sonidos de truenos.
- Cooldown: 60 segundos.

### Nivel 5 — Wrath of Zeus
**Activación:** Shift + tecla de intercambio de mano (F).

El jugador concentra electricidad durante 1,5 segundos y libera un enorme rayo hacia donde está apuntando.

- Alcance: 20 bloques.
- Daño principal: 12 HP.
- Daño secundario: 5 HP.
- Radio de explosión eléctrica: 5 bloques.
- Aplica Slowness II durante 3 segundos.
- Gran efecto visual de relámpago dorado y celeste.
- Cooldown: 90 segundos.

---

## 🌪️ WINDWEAVER — Mazo del viento

### Nivel 1 — Wind Dash
**Activación:** Shift + click izquierdo en el aire.

El jugador recibe un impulso de viento en la dirección en la que está mirando.

- Desplazamiento aproximado: 9 bloques.
- Se puede utilizar en el aire.
- No causa daño.
- Cancela el daño por caída asociado al impulso.
- Partículas blancas de viento.
- Cooldown: 18 segundos.

### Nivel 2 — Skybreaker
**Activación:** Click derecho.

El jugador salta aproximadamente 8 bloques hacia arriba.

- Puede controlar su movimiento en el aire.
- Otro click derecho provoca una caída rápida.
- Al aterrizar genera una onda expansiva.
- Radio: 5 bloques.
- Daño central: 8 HP.
- Daño exterior: 4 HP.
- Empuja a los enemigos hacia arriba y hacia atrás.
- No recibe daño por caída durante la habilidad.
- Cooldown: 40 segundos.

### Nivel 3 — Tornado
**Activación:** Shift + click derecho.

Invoca un tornado que avanza en la dirección apuntada.

- Duración: 5 segundos.
- Velocidad: 3 bloques por segundo.
- Radio de atracción: 3 bloques.
- Levanta a los enemigos y los arrastra.
- Inflige 2 HP por segundo, máximo 6 HP por objetivo.
- Partículas `CLOUD` formando una espiral.
- Cooldown: 40 segundos.

### Nivel 4 — Wind Prison
**Activación:** Shift + click izquierdo mientras mirás al suelo.

Crea una prisión circular de viento alrededor de la posición apuntada.

- Alcance de lanzamiento: 12 bloques.
- Radio de la prisión: 5 bloques.
- Duración: 5 segundos.
- Los enemigos dentro son empujados suavemente hacia el centro.
- Aplica Slowness II.
- No impide completamente escapar.
- Anillo de partículas blancas y celestes.
- Cooldown: 50 segundos.

### Nivel 5 — Heaven's Fall
**Activación:** Shift + tecla de intercambio de mano (F).

El jugador se eleva rápidamente y queda suspendido durante un máximo de 2 segundos, pudiendo elegir dónde aterrizar.

Después ejecuta un impacto devastador contra el suelo.

- Altura de elevación: 12 bloques.
- Radio del impacto: 7 bloques.
- Daño central: 12 HP.
- Daño exterior: 6 HP.
- Genera una enorme onda expansiva.
- Empuja a los enemigos hasta 5 bloques horizontalmente.
- Aplica Levitation I durante 1 segundo a los enemigos cercanos al impacto.
- Cancela el daño por caída durante la habilidad.
- Cooldown: 90 segundos.

---

## Reglas generales de implementación

1. Cada arma posee 5 niveles. Cada nivel desbloquea una nueva habilidad.
2. Al subir de nivel, el jugador conserva todas las habilidades anteriores.
3. Las habilidades tienen cooldowns independientes.
4. El sistema debe permitir configurar daño, alcance, duración y cooldown de cada habilidad.
5. Los efectos visuales deben funcionar sin mods del cliente.
6. Los rayos eléctricos no deben generar fuego ni destruir bloques.
7. Las habilidades no deben atravesar paredes sólidas ni zonas protegidas.
8. Los daños indicados son valores base y deben respetar armaduras, encantamientos y protecciones.
9. Evitar que una misma habilidad aplique daño varias veces accidentalmente.
10. Cancelar efectos pendientes cuando el jugador muere o se desconecta.
11. Evitar que las habilidades dañen aliados cuando el fuego amigo esté desactivado.
12. Implementar las habilidades utilizando la API de Paper y mantenerlas separadas para facilitar ajustes futuros.