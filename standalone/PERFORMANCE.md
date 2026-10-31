# Pack optimizado — 2.0.16

Mantiene los cinco modelos del usuario y las once animaciones. Los originales
Blockbench y sus PNG siguen intactos. Las habilidades, niveles, recetas, daño,
cooldowns y la atribución de bajas de 2.0.15 conservan su funcionamiento.

Se eliminan caras completamente tapadas por piezas opacas y se fusionan piezas
contiguas compatibles. Las texturas con transparencia o dibujos no se estiran. Shadow conserva todas sus piezas y el orden de sus caras para respetar sus transparencias.
Las partes que se mueven juntas comparten una geometría y usan la transformación
nativa de Minecraft 26.2; cada pose deja de copiar el arma completa.

| Modelo de reposo | Piezas antes → ahora | Caras antes → ahora |
| --- | --- | --- |
| Bloodlust | 39 → 39 | 234 → 224 |
| Shadow Blade | 138 → 138 | 778 → 778 |
| Windweaver | 47 → 46 | 282 → 239 |
| Earthbreaker | 344 → 213 | 2.064 → 841 |
| Thunder Spear | 140 → 133 | 840 → 681 |

Los datos geométricos almacenados bajan de 188.184 a 16.711 caras, aproximadamente
**91% menos**, al compartir piezas y reducir poses. Esto mide el pack completo,
no el número de caras dibujadas simultáneamente. Las poses ambientales pasan a
5 por segundo y los ataques a 10; las duraciones siguen siendo las originales.
No se acelera ni se retrasa el daño de ninguna habilidad. Los JSON mantienen
rutas para todos los identificadores de fotograma de 2.0.15.

## Actualizar

1. Detené Paper y guardá una copia coherente del mundo y `plugins/LegendaryWeapons`.
2. Guardá el JAR anterior fuera de `plugins` y colocá `LegendaryWeapons-2.0.16.jar`.
3. Iniciá Paper y aceptá el pack actualizado en Minecraft Java 26.2.

Si el pack anterior es una versión distribuida por este plugin, se guarda una
copia exacta en `plugins/LegendaryWeapons/backups/legendary-pack-<hash>.zip` antes
de instalar el nuevo. Un pack personalizado se conserva; el registro de Paper
lo indica. Para actualizar ese pack manualmente, respaldalo, reemplazá
`plugins/LegendaryWeapons/legendary-pack.zip` por el ZIP optimizado y reiniciá.
El archivo debe contener **`pack.mcmeta` en su raíz**, sin otra carpeta exterior.
El servidor sirve el archivo que cargó al iniciar, por eso requiere reinicio.

No borres modelos referenciados: el pack ya incluye el conjunto de JSON y poses
que corresponde. No borres el diario ni los datos de jugadores.

## Ajustes opcionales

En `plugins/LegendaryWeapons/config.yml`:

```yaml
legendary-weapons:
  model-animations:
    enabled: true
    idle: false
```

`idle: false` deja las armas quietas en reposo y conserva las animaciones de
ataque y poder. `enabled: false` deja todos los modelos estáticos; las habilidades
siguen funcionando. Aplicá el cambio con `/weapons reload`.

Los FPS dependen también del cliente, la GPU, shaders y ajustes. Una caída incluso
sin armas visibles requiere comparar la misma escena con el pack activado y
quitado; el número de piezas por sí solo no demuestra la causa.

## Fuentes y validación

[Informe geométrico](docs/model-optimization-2.0.16.json).
El script `tools/optimize_legendary_pack.py` parte de la distribución original
2.0.15 y comprueba las transformaciones de sus vértices antes de compartirlos.
`models/archive/legendary-pack-2.0.15.zip` conserva una copia exacta del pack previo.
Las pruebas revisan caras ocultas, transparencia, texturas dibujadas, tiempos,
rutas de modelos, compatibilidad de fotogramas y recuperación del pack.

[Validación completa y límites](docs/performance-verification-2.0.16.json) ·
[Captura real](docs/legendary-models-optimized-2.0.16.png) ·
[Descarga e instrucciones de la versión final vigente](downloads/2.0.16-final.1/README.md).

Para instalar el pack manual, guardá una copia anterior y copiá el ZIP como
`plugins/LegendaryWeapons/legendary-pack.zip`, con ese nombre exacto, y reiniciá.
