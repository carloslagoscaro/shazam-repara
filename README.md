# Shazam Repara

App Android que usa **audio, vibraciones y cámara** para diagnosticar fallas mecánicas y eléctricas en electrodomésticos y del hogar. Identifica el repuesto exacto, avisa si es seguro seguir usándolo, indica costo estimado transparente y si requiere técnico especialista.

## Qué hace

- **Escaneo por audio**: Graba 10–15 segundos con el micrófono y analiza patrones de ruido (chirrido, zumbido, golpeteo, silbido, metalizado, ruido agudo) para asociarlos a síntomas conocidos.
- **Escaneo por vibraciones**: Usa el acelerómetro del teléfono mientras apoyas el equipo; detecta vibración, golpeteo, zumbido y ruidos metálicos.
- **Identificación visual (cámara)**: El usuario señala señales visibles (fuga de agua, cable quemado, óxido, pieza suelta, etc.) tocando los íconos en pantalla.
- **Diagnóstico**: Motor que combina las tres fuentes de señal y devuelve:
  - La falla más probable con probabilidad (%).
  - Nivel de seguridad: **Seguro / Precaución / Peligro**.
  - Costo estimado de repuesto y mano de obra (en pesos chilenos, referencias de mercado 2025‑2026).
  - Dificultad: **Simple / Intermedia / Solo especialista**.
  - Motivo técnico y pasos de verificación.
  - Consejos para evitar estafas (pedir presupuesto por escrito, ver la pieza, comparar al menos dos presupuestos, exigir garantía escrita).

## Flujo de uso

1. **Abre la app** y selecciona el tipo de equipo (Auto, Lavadora, Refrigerador, Aire, Microondas, Calefón, Lavavajillas, Aspiradora u Otro).
2. **Apoya el teléfono** sobre el equipo (o en la zona del motor/soporte) y pulsa **Grabar vibración** (10 seg aprox).
3. **Graba audio** apuntando el micrófono al ruido que hace el equipo (mismos 10 seg).
4. **Marca señales visuales** si ves fuga de agua, cable quemado, óxido, pieza suelta, etc.
5. **Resultados al instante**: la app muestra la falla principal, probabilidad, nivel de seguridad, costo estimado y si es una reparación simple o requiere técnico.
6. **Guarda en historial** (opcional) para consultar later o compartir con otro taller.

## Capturas de pantalla (en dispositivo)

| Pantalla | Descripción |
|---|---|
| **Inicio** | Lista de equipos para elegir. |
| **Escaneo** | Indicador de progreso mientras se graba audio y vibración. |
| **Resultado** | Falla principal, probabilidad, nivel de seguridad, costo estimado y dificultad. |
| **Historial** | Lista de diagnósticos previos (se guardan localmente en el teléfono). |

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
- Permisos: `INTERNET`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` (opcional, para futuras funciones), `ACCESS_COARSE_LOCATION` (opcional), sensores y cámara (uso opcional en diagnóstico).
- Sin dependencias de terceros más allá de AndroidX.
- La base de datos de fallas está empaquetada en la app; todo el procesamiento es **offline** y no requiere internet después de la instalación.

## Nota sobre este entorno

Este proyecto se generó en una máquina sin JDK, ni Android SDK ni shell, así que **aquí no se pudo compilar ni ejecutar**. El código está escrito contra las APIs estables de Compose Material 3 y AndroidX; la compilación debe hacerse con alguna de las tres opciones de arriba.

## Licencia

Proyecto de código abierto. Puedes usar, modificar y redistribuir el código bajo los términos que consideres.