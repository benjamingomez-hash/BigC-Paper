# Distribución independiente de armas legendarias

El usuario pidió separar las armas para probarlas en su Minecraft. Se mantiene intacta la implementación integrada y se añade `standalone/` como proyecto Gradle autónomo para Paper 26.3 / Java 25. Copia el dominio y ciclo de vida de las armas; reemplaza el bootstrap, catálogo y equipos de BigCasares por un plugin mínimo, catálogo fijo y scoreboard vanilla. No carga BetterModel, Vault ni otros módulos.

Incluye un pack Java limitado a los ocho modelos y un servidor HTTP de dos hilos, con dirección loopback por defecto para pruebas locales y URL configurable. Cierra el servidor HTTP, tareas, efectos y altares al deshabilitarse. Entrega JAR y ZIP con guía de instalación. Bedrock no forma parte de esta distribución.

Validación: build completo del proyecto independiente, pruebas de dominio heredadas y arranque real en Paper con únicamente LegendaryWeapons instalado; descarga del pack y comandos administrativos.

Resultado: `clean build` exitoso, 27 pruebas sin fallos; servidor real Paper 26.3-183 con únicamente LegendaryWeapons 1.0.0, lista de ocho armas y descarga HTTP del pack verificados. Integridad del JAR/ZIP y las ocho referencias de modelos comprobadas. No se repitió la prueba gráfica ni multiplayer de esta distribución independiente.
