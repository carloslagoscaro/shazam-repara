# Shazam Repara

Simulador de sonido de **motor de auto** con fallas inyectables y diagnóstico automático del sonido. Eliges un motor, lo enciendes, aceleras y activas fallas (correa, rodamiento, inyector, fuga de vacío); la app analiza el mismo sonido que estás oyendo y te dice qué falla es, con probabilidad, repuesto y costo estimado en pesos chilenos.

Todo el sonido se **sintetiza en el teléfono**: no hay archivos de audio, ni descargas, ni compras dentro de la app.

## Qué hace

- **Motores simulados**: 4 cilindros bencina, V8 americano, diésel 1.9 y deportivo turbo, cada uno con su ralentí, régimen máximo y timbre propio.
- **Pedal de acelerador**: mantén presionado para subir las RPM; el motor responde con inercia y muestra las revoluciones en pantalla.
- **Fallas simuladas**: correa de accesorios patinando (chirrido agudo), rodamiento de alternador (zumbido grave), inyector sucio (cascabeleo irregular) y fuga de vacío en la admisión (silbido).
- **Diagnóstico del sonido**: analiza los últimos 5 segundos con FFT (frecuencia dominante, bandas, picos tonales, transitorios) y consulta el catálogo de fallas del motor.
- **Resultado**: causa más probable y probabilidad, nivel de seguridad (**Seguro / Precaución / Peligro**), repuesto exacto, costo estimado de repuesto y mano de obra, motivo técnico, otras causas posibles y qué revisar.

## Flujo de uso

1. Elige el motor.
2. Pulsa **Encender motor** (suena en ralentí).
3. Mantén presionado el pedal para acelerar.
4. Marca una o más fallas para escuchar cómo cambia el sonido.
5. Pulsa **Diagnosticar este sonido** y revisa el resultado.

## Cómo obtener el APK

### Opción A — GitHub Actions (sin instalar nada)

1. Sube este proyecto a un repositorio de GitHub (puede ser privado).
2. Entra a la pestaña **Actions** → workflow **Compilar APK** → **Run workflow**.
3. Cuando termine, ve a **Releases** (columna derecha del repo): ahí está `APK build <N>` con una descarga directa del `.apk`.
4. Pasa el `.apk` al teléfono e instálalo (permitiendo "instalar apps de origen desconocido").

### Opción B — Android Studio

1. Instala Android Studio (incluye JDK y Android SDK).
2. **File → Open** y elige esta carpeta. Acepta cuando pida generar/sincronizar Gradle.
3. Menú **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

### Opción C — desde este repo (si ya está publicado)

Si el repo está en GitHub y el workflow ya corrió, la APK aparece como Release en la pestaña **Releases** sin necesidad de entrar a Actions.

## Requisitos técnicos

- `minSdk 26` (Android 8.0), `targetSdk 35`, Kotlin 2.0, Jetpack Compose (Material 3).
- Sin permisos especiales: la app solo reproduce audio.
- Sin dependencias de terceros más allá de AndroidX.
- La base de datos de fallas está empaquetada en la app; todo el procesamiento es **offline** y no requiere internet después de la instalación.

## Nota sobre este entorno

Este proyecto se generó en una máquina sin JDK, ni Android SDK ni shell, así que **aquí no se pudo compilar ni ejecutar**. El código está escrito contra las APIs estables de Compose Material 3 y AndroidX; la compilación debe hacerse con alguna de las opciones de arriba.

## Licencia

Proyecto de código abierto. Puedes usar, modificar y redistribuir el código bajo los términos que consideres.