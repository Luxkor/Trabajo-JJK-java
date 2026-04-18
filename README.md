# Jujutsu Kaisen — Battle System v4.1

Juego de combate por turnos para dos jugadores inspirado en el universo de *Jujutsu Kaisen*. Desarrollado en Java puro (JDK 22), con interfaz en consola y colores ANSI.

---

## Estructura del proyecto

```
JJK-game/
├── src/
│   ├── JuegoJJK.java            ← Clase principal y lógica de combate
│   ├── Personaje.java           ← Clase abstracta base de todos los luchadores
│   ├── Hechicero.java           ← Implementa Personaje + Combatiente
│   ├── Maldicion.java           ← Implementa Personaje + Combatiente
│   ├── Combatiente.java         ← Interfaz: esMaldicion() + manifestarAura()
│   ├── Habilidad.java           ← Modelo de una habilidad (nombre, daño, coste…)
│   ├── Efecto.java              ← Enum de estados: NORMAL, ATURDIDO, DEBILITADO, POTENCIADO
│   ├── MusicaJJK.java           ← Clase abstracta del sistema de música + MusicaConsola
│   └── SeleccionInvalidaException.java
└── personajes.csv               ← Catálogo de personajes (separador '|')
```

---

## Cómo ejecutar

1. Abre el proyecto en IntelliJ IDEA (o cualquier IDE con soporte JDK 22).
2. Asegúrate de que `personajes.csv` está en la **raíz del módulo** (`JJK-game/`), al mismo nivel que la carpeta `src/`.
3. Ejecuta `JuegoJJK.main()`.

> En IntelliJ el directorio de trabajo predeterminado es la raíz del módulo, donde vive el CSV.  
> Si lo ejecutas desde línea de comandos (`java JuegoJJK`), hazlo desde la misma carpeta que contiene `personajes.csv`.

---

## Menú principal

Al iniciar se piden los nombres de los dos jugadores. Desde el menú se puede:

| Opción | Descripción |
|--------|-------------|
| **1. JUGAR PARTIDA** | Selección de personajes y combate |
| **2. MÚSICA DE FONDO** | Elegir la pista que sonará al empezar la pelea |
| **3. SALIR** | Cerrar el juego |

---

## Personajes disponibles (25)

Los personajes se cargan automáticamente desde `personajes.csv`. Cada uno tiene **tipo**, **HP**, **energía maldita (CE)** y **5 habilidades**. Los valores base son:

| Personaje | Tipo | HP | CE | Nota especial |
|-----------|------|----|----|---------------|
| Gojo Satoru | Hechicero | 600 | 450 | Costes de CE casi nulos (los seis ojos) |
| Ryomen Sukuna | Hechicero | 700 | 2000 | Dominio con daño pasivo en área |
| Itadori Yuji | Hechicero | 550 | 250 | Totalmente físico; alta probabilidad de Black Flash |
| Maki Zenin | Hechicero | 650 | 0 | Sin CE; usa herramientas malditas; puede curar |
| Toji Fushiguro | Hechicero | 650 | 0 | Sin CE; usa herramientas malditas; puede curar |
| Yuta Okkotsu | Hechicero | 500 | 1000 | Mayor reserva de CE del juego, después de Sukuna |
| Kinji Hakari | Hechicero | 500 | 300 | gana inmortalidad al conseguir el Jackpot en su dominio |
| Mahito | Maldición | 450 | 350 | Dominio potenciador |
| Jogo | Maldición | 380 | 450 | Dominio potenciador |
| Megumi Fushiguro | Hechicero | 420 | 350 | Puede invocar a Mahoraga |
| Suguru Geto | Hechicero | 500 | 500 | — |
| Nanami Kento | Hechicero | 480 | 250 | Portador de herramienta maldita |
| Choso | Maldición | 460 | 320 | — |
| Aoi Todo | Hechicero | 520 | 220 | — |
| Nobara Kugisaki | Hechicero | 400 | 250 | — |
| Hanami | Maldición | 550 | 300 | Dominio drenador de vida |
| Hajime Kashimo | Hechicero | 490 | 400 | — |
| Mei Mei | Hechicero | 450 | 250 | Portadora de herramienta maldita |
| Inumaki Toge | Hechicero | 360 | 300 | — |
| Panda | Hechicero | 550 | 200 | — |
| Hiromi Higuruma | Hechicero | 470 | 380 | Dominio con mecánica de juicio único |
| Angel (Hana Kurusu) | Hechicero | 440 | 420 | Daño doble + instakill a Maldiciones |
| Kenjaku | Hechicero | 580 | 550 | Dominio que drena CE; ventaja en choque de dominios |
| Naoya Zenin | Hechicero | 460 | 300 | Resucita como Maldición si muere por golpe físico |
| Yuki Tsukumo | Hechicero | 530 | 380 | Escudo con contragolpe |

---

## Mecánicas de combate

### Estructura de un turno

Cada turno, el personaje activo elige una de estas acciones:

| Acción | Descripción |
|--------|-------------|
| **1 — Habilidades Especiales** | Usa una de las 5 habilidades del personaje (requiere CE suficiente) |
| **2 — Ataque Básico (Físico)** | Inflige 30 de daño sin coste de CE. Puede activar Black Flash |
| **3 — Guardia** | Reduce a la mitad el daño recibido durante ese turno |
| **4 — Recargar Energía** | Recupera 80 CE. Solo disponible si el personaje usa CE |
| **5 — Curarse (RCT / Regeneración)** | Recupera HP (solo ciertos personajes) |

### Curación

Solo pueden curarse: **Gojo Satoru**, **Sukuna**, **Yuta Okkotsu**, **Maki Zenin** y **Toji Fushiguro**.

- Maki y Toji usan regeneración física extrema: +150 HP sin coste.
- Gojo activa la Técnica Inversa al instante por solo 5 CE: +250 HP.
- El resto gasta 50 CE para recuperar +250 HP.

### Esquiva

Cualquier personaje tiene un **15 % de probabilidad** de esquivar un ataque, salvo que esté dentro de un Dominio Enemigo activo (golpe garantizado).

---

## Black Flash (Destello Negro)

Cuando un ataque **físico** conecta (acción básica o habilidad con `isFisico = true`), existe una probabilidad de activar el Black Flash:

- Ataque básico: **5 %** de probabilidad → daño ×2,5.
- Habilidades físicas: **5 %** de probabilidad (o garantizado si el nombre es "Destello Negro").
- Personajes **sin CE** (Maki, Toji) nunca activan el Black Flash con su ataque básico.

---

## Estados alterados

| Estado | Duración | Efecto |
|--------|----------|--------|
| **ATURDIDO / Inmovilizado** | N turnos | El personaje salta su turno por completo |
| **DEBILITADO** | N turnos | El daño de sus ataques se multiplica ×0,6 |
| **POTENCIADO** | N turnos | El daño de sus ataques se multiplica ×1,5 |
| **Burnout** | N turnos | No puede usar habilidades especiales (solo básico, guardia, recarga, curación) |
| **Inmortal** | N turnos | Cualquier daño recibido (incluyendo fijo) es ignorado |

`turnosPotenciado` puede ser negativo (debilitado) o positivo (potenciado). Se aplica de forma acumulativa hasta un máximo de −2.

---

## Dominios de Expansión

Los dominios son habilidades especiales identificadas por nombres que contienen `EXPANSIÓN`, `IDLE DEATH`, `AUTOENCARNACIÓN` o `ATAÚD`.

### Activar un dominio

Al elegir una habilidad de dominio, el sistema entra en **modo de respuesta simultánea**: el rival debe elegir su acción antes de que el dominio surta efecto.

### Efectos de un dominio activo

- Dura **4 turnos** (1 para el Tribunal Maldito).
- Todos los rivales quedan **dentro del dominio**: el golpe seguro elimina la probabilidad de esquiva.
- **Maki Zenin** y **Toji Fushiguro** son inmunes al golpe seguro (carecen de CE; el dominio no puede encerrarlos).
- Al expirar, el dueño sufre **burnout de 2 turnos**.

### Dominios con efectos pasivos

| Dominio | Efecto pasivo (cada turno) |
|---------|---------------------------|
| Santuario Malévolo (Sukuna) | −40 HP a todos los rivales |
| Gran Juego (Kenjaku) | −60 CE a todos los rivales |

### Choque de dominios

Si ambos jugadores declaran un dominio en el mismo instante se inicia una **secuencia de teclas bajo presión** (3 rondas):

- Cada jugador memoriza su propia secuencia secreta de dígitos (4 → 5 → 6 dígitos por ronda).
- Se introduce la secuencia sin verla (se oculta tras pulsar Enter).
- **Sukuna y Kenjaku** arrancan con +1 punto de ventaja por su densidad de dominio superior.
- **En caso de empate gana el defensor.**
- El perdedor sufre burnout y su dominio colapsa; el ganador activa el suyo.

---

## Tribunal Maldito (Hiromi Higuruma)

El dominio de Higuruma activa un **juicio automático** sin respuesta previa del rival.

### Fase de juicio

1. Se selecciona aleatoriamente un crimen de 9 posibles, clasificado en:
   - ⚪ **LEVE** (índice 0–2)
   - 🟡 **GRAVE** (índice 3–5)
   - 🔴 **FATAL** (índice 6–8)
2. El jugador acusado elige entre 3 argumentos de defensa; solo uno es correcto.
3. Si acierta → **INOCENTE**, sin consecuencias.
4. Si falla → **CULPABLE provisional**.
   - Crímenes GRAVES y FATALES permiten solicitar una **apelación** (segundo juicio).
   - Si también falla la apelación → sentencia aplicada.

### Sentencias

| Gravedad | Sentencia | Efecto |
|----------|-----------|--------|
| LEVE o GRAVE | Confiscación | Destruye la herramienta maldita del acusado (si la tiene), sella su técnica (burnout 2) o confisca toda su CE |
| FATAL | Pena de muerte | Higuruma obtiene la **Espada del Verdugo** de forma permanente |

### Espada del Verdugo

Arma otorgada por Judgeman al dictarse una sentencia de muerte. Higuruma la adquiere como habilidad adicional permanente en su menú.

- Cada vez que se usa: **30 % de probabilidad** de conectar.
- **Primer impacto**: −60 % del HP actual del objetivo.
- **Segundo impacto**: muerte instantánea.
- La espada persiste aunque el dominio haya expirado.

---

## Mecánicas especiales por personaje

### Mahoraga (Megumi Fushiguro)

Al usar la habilidad `MAHORAGA`, Megumi abandona el combate permanentemente y es reemplazado en el equipo por **Mahoraga (General Divino)** (800 HP / 500 CE). Mahoraga posee instakill sobre cualquier Maldición con su `Tajo de Exterminio`.

### Ángel — Jacob: Aniquilación

Contra Maldiciones: daño ×2 y esquiva anulada de forma automática.
`ESCALERA DE JACOB` es su técnica máxima con el mayor daño del juego (180 base + modificadores).

### Idle Death Gamble (Hakari)

Al activar el dominio hay un **33 %** de jackpot: Hakari obtiene **inmortalidad durante 4 turnos** y CE ilimitada (9999). En caso contrario, solo se consume el coste del dominio.

### Transformación de Naoya Zenin

Si Naoya muere por un **golpe físico puro** (sin CE, sin herramienta maldita), resucita inmediatamente como **Naoya Zenin (Maldición)** (550 HP / sin CE), una maldición especial con 5 habilidades propias y sin coste de energía.

Si muere por un ataque energético, no resucita.

### Personajes sin energía maldita

**Maki Zenin** y **Toji Fushiguro** tienen CE = 0. Para ellos:

- Todas sus habilidades tienen coste 0.
- No pueden recargar energía ni usar la Técnica Inversa por CE.
- Se curan con regeneración física extrema (+150 HP).
- Son inmunes al golpe seguro de los dominios.
- Sus ataques son siempre considerados físicos (relevante para la transformación de Naoya).

---

## Sistema de música

La clase abstracta `MusicaJJK` gestiona la selección y reproducción de pistas. La subclase concreta `MusicaConsola` anuncia en pantalla la canción seleccionada al inicio de cada combate. Las pistas disponibles son:

| # | Título | Artista | Contexto |
|---|--------|---------|----------|
| 1 | Kaikai Kitan | Eve | Opening 1 — Temporada 1 |
| 2 | SPECIALZ | King Gnu | Opening Arco de Shibuya |
| 3 | Ao no Sumika | Tatsuya Kitani | Opening Inventario Oculto |
| 4 | Lost in Paradise | ALI ft. AKLO | Ending 1 — Temporada 1 |
| 5 | AIZO | King GNU | Opening 5 — Temporada 3 |

---

## Añadir o modificar personajes

Todo el catálogo vive en `personajes.csv`. El formato por línea es:

```
nombre|TIPO|vida|energia|h0_nombre|h0_desc|h0_danio|h0_coste|h0_efecto|h0_fisico|...(×5 habilidades)
```

Campos: **34 en total**, separados por `|`. La primera línea es la cabecera y se ignora.

| Campo | Valores posibles |
|-------|-----------------|
| TIPO | `HECHICERO` o `MALDICION` |
| h_efecto | `NORMAL`, `ATURDIDO`, `DEBILITADO` o `POTENCIADO` |
| h_fisico | `true` o `false` |

> El separador es `|` (pipe) para evitar conflictos con comas y puntos y coma en las descripciones.

### Ejemplo de fila

```
Gojo Satoru|HECHICERO|600|450|Azul|Atrae|40|5|NORMAL|false|Rojo|Repele|60|5|NORMAL|false|VACÍO PÚRPURA|Borra materia|80|5|NORMAL|false|Destello Negro|Impacto físico letal|100|5|NORMAL|true|EXPANSIÓN: VACÍO INFINITO|Inmoviliza 2 turnos|120|15|NORMAL|false
```

---

## Notas de diseño (OOP)

| Elemento | Patrón / Razón |
|----------|----------------|
| `Personaje` abstracta | Evita instanciar entidades sin tipo concreto; centraliza stats y lógica de daño |
| `Hechicero` / `Maldicion` | Herencia concreta; `esMaldicion()` permite mecánicas diferenciadas (Jacob, etc.) |
| `Combatiente` (interfaz) | Contrato de `esMaldicion()` y `manifestarAura()` independiente de la jerarquía |
| `MusicaJJK` abstracta | Mantiene estado (pistas, selección) y permite añadir subclases con audio real sin tocar `JuegoJJK` |
| `SeleccionInvalidaException` | Excepción semántica propia para errores de selección de menú |
| `personajes.csv` | Separa datos de lógica; añadir personajes sin recompilar |
