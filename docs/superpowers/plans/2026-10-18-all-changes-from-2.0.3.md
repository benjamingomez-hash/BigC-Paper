# Todos los cambios desde 2.0.3

El usuario solicita ahora reconstruir partiendo directamente de 2.0.3 e incluir
todos los cambios posteriores juntos. Base exacta:
`d75fa8bc19002f362df5732a430ec83e7bd84c1f`. Se conserva el historial anterior en
sus ramas; la rama nueva no incorpora sus commits intermedios.

- Incorporar la parálisis de la lanza para ambos participantes, rayos dañinos
  solo al impacto directo, limpieza de inmovilidad y devolución única.
- Incorporar el golpe inicial de cuatro puntos de salud del hacha, conservando
  el cráter de radio ocho y las protecciones del servidor.
- Incorporar Fuerza III durante diez segundos y curación de medio corazón por
  golpe directo válido de Bloodlust, limitada a la salud máxima.
- Incorporar Sombras con Invisibilidad y Velocidad III, Debilidad II y Oscuridad;
  limitar la emboscada a seis puntos mágicos, también con configuración antigua.
- Conservar poderes de Frost, Hyperion, maza y katana, modelos, recetas, evento,
  identificadores, diario y cooldowns. Mantener el plugin independiente y la
  plataforma Paper 26.2/Java 25, sin modificar el plugin raíz.
- Compilar como 2.0.10, ejecutar las 74 pruebas y comprobar los poderes en Paper
  con dos clientes. Analizar la distribución nueva en Windows con Defender
  activo y sin exclusiones. Publicar los mismos archivos analizados y sus hashes.

Partir de otra base e incorporar los mismos cambios no identifica una solución
a la detección previa de Defender. Mantener esa limitación explícita.
