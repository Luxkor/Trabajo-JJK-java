# assets/music/ — Carpeta de archivos de audio

Aquí deben colocarse los archivos WAV de las canciones del juego.

## Formato requerido

- **Tipo de archivo**: WAV (.wav) — soportado nativamente por Java (javax.sound.sampled).
- **Codificación recomendada**: PCM 16-bit, 44100 Hz, estéreo.
- Los archivos MP3 NO son compatibles sin librerías externas.

## Nombres de archivo esperados

Estos nombres deben coincidir exactamente con la columna `archivo` de `musica.csv`:

| Archivo                   | Canción            | Artista          |
|---------------------------|--------------------|------------------|
| 01_kaikai_kitan.wav       | Kaikai Kitan       | Eve              |
| 02_specialz.wav           | SPECIALZ           | King Gnu         |
| 03_ao_no_sumika.wav       | Ao no Sumika       | Tatsuya Kitani   |
| 04_lost_in_paradise.wav   | Lost in Paradise   | ALI ft. AKLO     |
| 05_aizo.wav               | AIZO               | King GNU         |

## Cómo obtener los WAV

1. Descarga la canción en formato MP3/FLAC desde una fuente legal.
2. Conviértela a WAV con Audacity (gratis) o ffmpeg:
   ```
   ffmpeg -i cancion.mp3 -ar 44100 -ac 2 -acodec pcm_s16le 01_kaikai_kitan.wav
   ```
3. Coloca el .wav resultante en esta carpeta.

## Comportamiento si falta un archivo

Si el WAV no existe, el juego muestra la ruta exacta donde buscó el archivo
y continúa normalmente sin audio (solo se imprime el anuncio en consola).
No se lanza ningún error que interrumpa la partida.

## Añadir canciones nuevas

1. Coloca el WAV aquí con el nombre que quieras.
2. Añade una línea al final de `musica.csv`:
   ```
   Nombre canción|Artista|Contexto|nombre_del_archivo.wav
   ```
3. Reinicia el juego — se cargará automáticamente.
