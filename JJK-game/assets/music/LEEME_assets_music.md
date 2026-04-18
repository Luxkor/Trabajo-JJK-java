# assets/music/ — Instrucciones para añadir música

Aquí se guardan los archivos de audio del juego. El juego lee el catálogo desde
`musica.csv` (en la raíz del proyecto) y busca cada archivo WAV en esta carpeta.

---

## Formato requerido para los archivos de audio

- **Tipo**: WAV (`.wav`) — único formato soportado por Java sin librerías externas.
- **Codificación recomendada**: PCM 16-bit, 44100 Hz, estéreo.
- Los MP3, FLAC u otros formatos **no funcionan** directamente; hay que convertirlos primero.

---

## Cómo añadir una canción

### 1. Consigue el archivo de audio

Descarga o exporta la canción en cualquier formato (MP3, FLAC, etc.) desde una fuente legal.

### 2. Conviértelo a WAV

Con **ffmpeg** (gratuito, multiplataforma):
```
ffmpeg -i entrada.mp3 -ar 44100 -ac 2 -acodec pcm_s16le salida.wav
```

Con **Audacity** (interfaz gráfica):  
`Archivo → Exportar → Exportar como WAV`

### 3. Ponle nombre al archivo y cópialo aquí

Elige el nombre que quieras, por ejemplo `combate.wav`.  
No hace falta seguir ningún patrón concreto — lo que pongas en el CSV es lo que el juego buscará.

### 4. Añade una línea en `musica.csv`

El archivo está en la raíz del proyecto (`JJK-game/musica.csv`).  
Abre el fichero y añade una línea al final con este formato:

```
titulo|artista|contexto|archivo.wav
```

El CSV admite **dos formatos** de columnas, ambos válidos:

**Con artista (4 columnas):**
```
titulo|artista|contexto|archivo.wav
```

**Sin artista (3 columnas):**
```
titulo|contexto|archivo.wav
```

| Campo    | Descripción                                           | Obligatorio |
|----------|-------------------------------------------------------|-------------|
| titulo   | Nombre que aparece en el menú del juego               | Sí          |
| artista  | Nombre del artista o banda                            | No (omitir columna si no se usa) |
| contexto | Descripción libre (temporada, mood, etc.)             | Sí          |
| archivo  | Nombre exacto del fichero WAV dentro de esta carpeta  | Sí          |

**Ejemplos:**
```
Mi cancion|Mi artista|Tema de combate|batalla.wav
Otra cancion|Musica de boss|boss_theme.wav
```

Guarda `musica.csv` y vuelve a ejecutar el juego — la pista aparecerá en el menú automáticamente.

---

## Cómo eliminar una canción

Borra o comenta (añade `#` al inicio) la línea correspondiente en `musica.csv`.  
Puedes dejar el WAV en esta carpeta o borrarlo también; el juego no falla si hay WAVs sin usar.

---

## Qué pasa si el archivo WAV no existe

Si hay una entrada en `musica.csv` pero el WAV no está en esta carpeta, el juego:
- Muestra la pista en el menú con normalidad.
- Al iniciar el combate imprime la ruta exacta donde lo buscó.
- **No interrumpe la partida** — simplemente continúa sin audio.

---

## Qué pasa si `musica.csv` está vacío

El menú de música mostrará solo la opción "Sin música".  
No se produce ningún error.