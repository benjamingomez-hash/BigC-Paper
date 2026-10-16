# Verificación — 16 de octubre de 2026

## Automatización

- `standalone/`: `bash gradlew --no-daemon --max-workers=4 clean build`, Java 25, **46 pruebas sin fallos, errores ni exclusiones**.
- Repositorio raíz: mismo comando, **1.014 pruebas sin fallos, errores ni exclusiones**. No se modifican sus fuentes, dependencias ni módulos.
- El JAR independiente no contiene el entrypoint BigCasares ni declara dependencias obligatorias. Incluye el pack Java y las ocho definiciones de modelo, con referencias resueltas. No contiene `LegendaryAltars`.

## Cliente y servidor reales

Paper **26.2-56**, un único plugin de producción (`LegendaryWeapons 2.0.0`), cliente oficial Java **26.2**, entorno local aislado. El usuario de prueba inicial y el que reclama son diferentes. No se utilizó ningún servidor de jugadores real.

1. Se colocó la receta de Thunder Spear en la mesa vanilla: lanza de netherite, cuatro pararrayos, dos corazones del mar y dos bloques de cobre. El resultado era únicamente una vista previa de inicio.
2. Al hacer clic, se consumieron los nueve materiales. El inventario del fabricante quedó vacío; no había ItemDisplay de arma. Se mostró el anuncio de inicio sin coordenadas y un TextDisplay con `Thunder Spear` y el contador.
3. La mesa resistió una rotura en creativo. Matar y desconectar al fabricante no canceló el estado CRAFTING.
4. Se detuvo Paper y se adelantaron exclusivamente los tiempos del diario de la instancia de prueba a 5/10/15/20 minutos transcurridos, reiniciando entre fases. **No se esperaron veinte minutos seguidos en esta prueba manual**; los límites exactos de milisegundos se verifican mediante las pruebas automatizadas. No existe un comando ni configuración de producción para acortar el evento.
5. Los límites persistidos se mantuvieron en cada reinicio:

| Pista | X inclusiva | Z inclusiva |
| --- | --- | --- |
| 1.000×1.000 | -453 a 546 | -880 a 119 |
| 500×500 | 2 a 501 | -387 a 112 |
| Coordenadas exactas | 100 | 102 (Y: -60) |

La zona interior está completamente dentro de la exterior. Ambas incluyen la mesa; se anunció Overworld. No se repitieron los avisos ya confirmados.

6. READY_TO_CLAIM quedó registrado tras el reinicio sin cargar el chunk de la mesa. Al acercar al segundo usuario, apareció el modelo horizontal sobre la mesa.
7. Con el inventario lleno de 2.304 bloques de piedra, el reclamo se rechazó y el arma continuó disponible. Tras liberar un espacio, ese segundo usuario obtuvo una sola `NETHERITE_SPEAR`, con `minecraft:lunge: 3`, modelo `bigcasares:thunder_spear` e identidad de instancia persistente.
8. El estado pasó a CLAIMED. El anuncio fue únicamente «THUNDER SPEAR HA SIDO RECLAMADA», sin nombre. El ItemDisplay y la Interaction desaparecieron.
9. El arma obtenida se lanzó con clic derecho; ocupó temporalmente su espacio con el recibo y regresó como una única lanza. El estado global permaneció CLAIMED. Tras otro reinicio y reconexión, el jugador conservó exactamente una lanza, el estado siguió CLAIMED y no se repitieron los anuncios de aparición o reclamo.

![Receta vanilla con vista previa de inicio](docs/crafting-recipe-preview.png)

![Modelo disponible sobre la mesa](docs/crafting-ready.png)

![Reclamo anónimo y premio único](docs/crafting-claimed.png)

## Alcance

La concurrencia de dieciséis reclamos se probó sobre el coordinador persistente mediante hilos; la prueba manual usó dos identidades sucesivas, no dos clientes atacando simultáneamente. Las ocho recetas y la aritmética de consumo tienen pruebas automatizadas; el recorrido manual completo usó Thunder Spear. WorldGuard, combate PvP con varios clientes, Windows nativo y Bedrock no se probaron en esta instancia Linux. La rama de persistencia Windows se probó mediante su adaptador de plataforma.

En Windows Java no permite abrir directorios como FileChannel: se fuerzan los archivos y se exige renombrado atómico, pero no se realiza fsync adicional del directorio. Las garantías frente a cortes de energía dependen del sistema de archivos; conservar copias de seguridad coherentes de diario y jugadores.
