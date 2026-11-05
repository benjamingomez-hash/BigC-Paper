# DupeRestore 1.1.0 — API pública y opciones nativas explícitas

El usuario informó que Windows bloqueó 1.0.1 y pidió una implementación distinta
partiendo de la anterior. No aportó identificador de amenaza o archivo afectado;
no se atribuye una causa ni se considera demostrada una detección falsa.

Se reutilizan las fuentes de 1.0.0 (`6de5a55`), reaplicando la restricción de origen
waterlogged y producción continua que el usuario confirmó. Se elimina el puente
que modificaba opciones internas de Paper por reflexión y su ciclo de leases.

El plugin administra solo el hilo mediante eventos públicos, sin tocar opciones
nativas. TNT/alfombras/raíles y ganchos se habilitan mediante las dos claves
oficiales de `config/paper-global.yml`, con reinicio. Se usa la API pública
`Server.Spigot#getPaperConfig` únicamente para consultar una instantánea del
archivo y mostrar el estado configurado; no se afirma leer un valor vivo de RAM.
Las claves antiguas del plugin se conservan en archivos del usuario pero se
ignoran con un aviso. Los datos, mundos y otros plugins no se modifican.

Se verifica que el JAR de producción no incluya el probe ni referencias directas
a reflexión, cargadores de clases, procesos, red o clases internas del servidor.
Esta inspección no equivale a un análisis antimalware. La prueba de servidor
incluye las mismas granjas y controles con flags nativos false y true, comprobando
que el plugin preserve los valores y el archivo de Paper. El probe puede leer
internos solo para diagnóstico y nunca se incluye en el JAR de distribución.

Windows compila y escanea los bytes nuevos; además reanaliza la distribución
1.0.1 como referencia, sin ejecutar ninguno de sus archivos. Los resultados del
runner no se extrapolan al equipo del usuario. La publicación debe conservar
los hashes exactos y registrar las limitaciones de la detección local.
