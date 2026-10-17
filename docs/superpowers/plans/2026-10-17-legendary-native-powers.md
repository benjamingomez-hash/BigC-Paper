# Nuevos poderes de Frost, Hyperion y Earthbreaker

El usuario sigue sin percibir daño y solicita reemplazar por completo tres habilidades: Frost aplica congelación y lentitud a enemigos de cualquier tipo de entidad; Hyperion dispara cinco proyectiles nativos de ghast; Earthbreaker elimina terreno formando un cráter circular de radio ocho y hasta cuatro bloques de profundidad, sin daño directo.

## Alcance y decisiones

- Frost conserva un aura de ocho segundos; aplica Lentitud IV y congelación nativa directamente, sin condicionar sus efectos a un golpe. Incluye jugadores, mobs hostiles, animales, aldeanos y mascotas. Excluye al lanzador y respeta equipos, modos de juego y protección de efectos. Los ítems y otros objetos sin vida no admiten pociones.
- Hyperion usa cinco entidades Fireball grandes, con apariencia nativa de ghast, a intervalos de cinco ticks. Sus impactos, explosiones y colisiones son vanilla, atribuidos al usuario. Las bolas son temporales y se eliminan al desconectar, morir, cambiar de mundo o terminar la sesión. Se conservan las protecciones de jugadores, regiones, bloques y mesas de eventos.
- Earthbreaker talla desde el suelo bajo el jugador: profundidad cuatro a menos de dos bloques del centro, tres entre dos y cuatro, dos entre cuatro y seis, uno entre seis y ocho inclusive. Eliminación por lotes acotados; sin golpes, levitación ni ondas de daño. Preserva bedrock, contenedores/datos de bloques y zonas protegidas. Puede causar caídas naturales al retirar el suelo.
- Conservar los ocho modelos, IDs, recetas, evento de veinte minutos, cooldowns y las otras cinco habilidades. No sobrescribir configuración ni diario del usuario. Distribución independiente 2.0.3 en la misma rama, con nuevo ZIP.

## Verificación

Pruebas de geometría, simetría, bordes, límites por tick y exactamente cinco disparos. Compilación completa. Cliente Java 26.2 y Paper 26.2-133: congelación de mobs pasivos/hostiles sin necesitar daño, exclusión del lanzador y veto de efectos; cinco Fireball reales, colisión, explosión y limpieza; profundidad y anchura del cráter sobre una plataforma de prueba, ausencia de daño directo, protección de bloques y mesas activas. Registrar resultados y límites antes de publicar.

## Resultado

Compilación completa: 60 pruebas correctas. Doce casos nativos verificaron congelación y Lentitud IV incluso en una vaca invulnerable, cinco Fireball reales con explosión y daño vanilla, cancelación de daño/rotura, limpieza y expiración. El cráter eliminó 360 bloques en 197 columnas, con profundidades 4/3/2/1 hacia el borde; las protecciones redujeron el trabajo conservando contenedor, bedrock y una mesa de evento activa. El único daño del cráter fue la caída vanilla al retirarse el suelo.

Captura real e informe JSON en `standalone/docs/`; detalles y límites en `standalone/VERIFY.md`. Modelos y las otras cinco habilidades conservados. Distribución 2.0.3 y ZIP nuevo en la misma rama, sin sobrescribir archivos de configuración ni diario del usuario.
