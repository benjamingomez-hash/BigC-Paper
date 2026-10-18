# Reconstrucción independiente desde 2.0.3

Base: `d75fa8bc19002f362df5732a430ec83e7bd84c1f`. Solicitud: reconstruir desde la versión anterior y rehacer la parálisis de Thunder Spear. La detección local de Defender de 2.0.4 sigue sin diagnóstico confirmado; reconstruir no demuestra que fuera un falso positivo ni garantiza otro resultado del antivirus.

- Conservar los siete poderes restantes, las ocho recetas, modelos, claves persistentes, cooldowns, diario y devolución transaccional.
- Implementar de nuevo las concesiones de inmovilidad por campo, con ancla estable y liberación independiente de campos superpuestos. Separar el adaptador de eventos de movimiento del runtime de habilidades.
- Campo de radio 7, altura ±4 y duración 200 ticks. Incluye al lanzador. Solo el objetivo alcanzado directamente recibe los cinco pulsos eléctricos. Mantener cámara y combate normales, protecciones y escape administrativo.
- Liberar al morir, desconectar, cambiar de mundo, terminar/cancelar la habilidad o apagar el plugin. No cambiar AI, gravedad ni velocidad de caminar como estados persistentes.
- Compilar desde fuentes con Java 25, probar concesiones/colisión, comprobar Paper con dos clientes, comparar modelos y efectuar análisis de archivos en Linux y Windows. Registrar resultados y límites, sin desactivar protección ni restaurar archivos en cuarentena.

Estado: implementación y validación pendientes. La rama anterior conserva sus archivos y el registro de la alerta.
