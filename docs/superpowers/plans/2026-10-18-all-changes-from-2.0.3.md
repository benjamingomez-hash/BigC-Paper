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

Completado: `4c7f5bebddec5f3b37cb0d3f38a27bc252eb4a10` incorpora el conjunto
completo de cambios directamente sobre la base 2.0.3. Build con 74 pruebas
correctas, seis casos de Bloodlust/hacha/Sombras y cuatro casos de lanza con
dos clientes oficiales en Paper. El primer intento inmediato tras conectar no
emitió ambos golpes; se documentó y se repitió con los clientes estabilizados.
Defender en Actions `39092559209`: cinco análisis sin detecciones; ClamAV también
sin detecciones en los dos archivos distribuidos. Se publican los archivos exactos
del runner y sus hashes. Los 28 archivos de modelos conservan las fuentes de 2.0.3;
las 80 clases finales coinciden con la lógica validada de 2.0.9. Instancia detenida,
herramientas privadas retiradas y datos originales restaurados.
