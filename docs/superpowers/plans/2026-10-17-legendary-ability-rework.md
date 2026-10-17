# Corrección de habilidades del plugin independiente

El usuario probó la distribución 2.0.1 e informó de poco daño o efecto en la mayoría de habilidades, especialmente Earthbreaker. Pidió que Windweaver impulse al usuario hacia arriba algo más que una wind charge y que Bloodlust aplique Fuerza III durante diez segundos. Void Katana y Thunder Spear deben conservar sus habilidades.

## Cambios

- Bloodlust: efecto nativo de Fuerza III, 200 ticks; retirar multiplicador, robo de vida y golpe final anteriores. Usar la propiedad existente de efectos temporales para respetar mejoras ajenas y restaurar efectos anteriores.
- Windweaver: reemplazar tornado por impulso vertical compatible con smash de la maza; conservar movimiento horizontal y no acumular impulso sobre una subida más fuerte. Proteger la primera caída de ese salto hasta aterrizar o diez segundos como máximo. Conservar el ID persistente `divine-tornado` para mantener cooldowns.
- Earthbreaker: tres ondas expansivas en sesenta ticks, radio nueve en horizontal y tolerancia vertical de 2,5 bloques. Daño una vez por objetivo y onda, elevación y Lentitud II. Un golpe rechazado por inmunidad temporal o cancelación no consume el impacto de esa onda. Partículas de suelo sin destruir bloques.
- Frost Scythe y Hyperion: daño mágico atribuido al jugador, con pulsos perceptibles. Conservar protección de equipos, regiones, objetivos y cancelación de daño; armadura normal no absorbe el daño mágico, pero Protección y Resistencia siguen siendo nativas.
- Shadow Blade: golpe mágico al alcanzar al enemigo mediante el teletransporte, en lugar de un efecto de 0,001 y un bonus pendiente del siguiente ataque físico.
- No modificar recetas, eventos de veinte minutos, modelos, persistencia, el daño de Void Katana ni la tormenta y devolución de Thunder Spear.

## Verificación y distribución

Pruebas de ondas repetidas, reintento de golpes rechazados, alcance terrestre y conservación de movimiento al impulsar. Compilación completa con Java 25. Prueba en Paper 26.2-56 con cliente real: Fuerza III/duración/efectos previos; altura frente a wind charge y smash nativo; daño con netherite y Protección IV; cancelación de daño y activación protegida. Documentar mediciones y límites. Versionar 2.0.2 y publicar un ZIP actualizado en la misma rama, conservando el anterior.

## Resultado

Implementadas las seis correcciones. El terremoto conserva la propiedad de sus estados negativos durante su duración completa después de dejar de emitir ondas. Las claves nuevas usan valores por defecto sin sobrescribir la configuración del usuario ni migrar su diario.

Compilación completa: 59 pruebas correctas. El servidor disponible para la prueba real es Paper 26.2-133 estable, con la API de compilación anterior conservada. Medidos impulso de 9,43 bloques frente a 6,91 de la wind charge, Fuerza III por 200 ticks, daño mágico y reducción por Protección IV. Se verificaron cancelaciones y reintento de ondas. Resultados y límites en `standalone/VERIFY.md` y el informe JSON de la prueba.

La distribución 2.0.2 conserva los ocho modelos, recetas, evento, katana y lanza. El ZIP anterior se conserva; la descarga principal de `standalone/README.md` apunta a la versión corregida.
