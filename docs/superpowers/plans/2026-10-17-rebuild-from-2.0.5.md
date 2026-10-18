# Reconstrucción desde 2.0.5

Base exacta: `a1ca1655e082173cde24f1493da91152a4b01d92`. El usuario informó una nueva detección de Defender y pidió reconstruir desde esa versión con todos los cambios de jugabilidad. La captura muestra `Trojan:Script/Ulthar.A!ml`, estado «Quitado», en el ZIP 2.0.7, 17/10/2026 a las 23:29 del equipo del usuario. No se publica su ruta personal.

- Reaplicar únicamente las fuentes revisadas del hacha (cuatro puntos de salud una vez), Sombras (máximo seis puntos mágicos, movilidad y efectos) y Bloodlust (Fuerza III diez segundos y un punto de curación por golpe válido).
- Conservar lanza con parálisis de ambos, Frost, Hyperion, maza, katana, ocho modelos, recetas, claves persistentes y diario.
- Compilar de nuevo; no reutilizar ni renombrar binarios 2.0.6/2.0.7. Comparar el resultado por clases y recursos. Si las clases coinciden con 2.0.7, declararlo: partir de una base anterior no implica corregir una detección.
- Ejecutar las 74 pruebas y las herramientas de Paper para los poderes modificados. Comparar en Windows, con protección activa y sin exclusiones, las descargas públicas exactas de 2.0.5 y 2.0.7 con el candidato nuevo. Las referencias no se ejecutan ni se incluyen en la distribución.
- Conservar el registro del incidente. Ningún análisis en el runner se presenta como un diagnóstico local, ni como autorización para restaurar archivos bloqueados. La distribución continúa en revisión mientras la detección siga sin explicación.

Estado: reconstrucción y validación en curso.
