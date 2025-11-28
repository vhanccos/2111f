# 🍕 AppHilos - Procesador de Pedidos

## Integrantes

- Carrasco Choque, Arles Melvin
- Chancuaña Alvis, Klismann
- Hancco Soncco, Vladimir Jaward
- Hanccoccallo Achircana, Frank Duks
- Nina Suyo, Diego Claudio

## 📘 Resumen Ejecutivo

Este proyecto es una **aplicación móvil Android**, desarrollada en **Kotlin con Jetpack Compose**, diseñada para simular un **sistema de procesamiento de pedidos en tiempo real**. El objetivo principal es servir como un ejemplo práctico y educativo sobre el manejo de concurrencia y operaciones asíncronas en el desarrollo moderno de Android.

La aplicación permite:

- **Añadir** nuevos pedidos a una cola.
- **Procesar** pedidos individualmente o todos a la vez.
- **Visualizar** el estado de cada pedido en tiempo real (Pendiente, Cocinando, Listo) con una barra de progreso.
- **Limitar** el número de pedidos que se "cocinan" simultáneamente para simular un entorno con recursos limitados.

El enfoque del proyecto no está en la persistencia de datos, sino en la **orquestación de tareas en segundo plano (worker threads)** para no bloquear el **hilo principal (UI thread)**, garantizando una interfaz de usuario fluida y receptiva en todo momento.

---

## 🌟 Arquitectura y Conceptos de Concurrencia

A continuación se detallan los conceptos técnicos clave que la aplicación utiliza y dónde se pueden encontrar.

### 1. Coroutines (Corrutinas)
Las corrutinas son la base de toda la lógica asíncrona en la aplicación. Permiten que las operaciones de larga duración (como "cocinar" un pedido) se ejecuten sin bloquear el hilo principal, manteniendo la UI fluida y receptiva.

### 2. Main Thread (UI Thread) vs. Worker Threads
- **Main Thread**: Es el único hilo que puede modificar la interfaz de usuario. En esta app, Jetpack Compose gestiona este hilo por nosotros. Todas las funciones `@Composable` y sus actualizaciones se ejecutan en el hilo principal.
- **Worker Threads**: Para evitar congelar la UI, cualquier tarea que tome tiempo se delega a hilos de trabajo en segundo plano. En nuestro caso, la "cocción" de los pedidos se realiza en un worker thread.

### 3. Dispatchers
Los `Dispatchers` son los que deciden en qué hilo o grupo de hilos se ejecutará una corrutina.
- **`Dispatchers.Default`**: En `OrderRepository.kt`, la línea `.flowOn(Dispatchers.Default)` es crucial. Mueve la ejecución de todo el `flow` (que simula la cocción) a un worker thread del pool `Default`, asegurando que el `delay` y la lógica de progreso no afecten al hilo principal.

### 4. `viewModelScope`
Es un `CoroutineScope` integrado en la clase `ViewModel`. Cualquier corrutina lanzada en este ámbito se cancela automáticamente si el `ViewModel` se destruye (por ejemplo, cuando el usuario sale de la pantalla). Esto previene fugas de memoria y trabajo innecesario.

- **Ubicación**: `OrderViewModel.kt`
- **Uso**: Todas las funciones públicas como `addOrder()`, `processOrder()`, y `processAllPendingOrders()` usan `viewModelScope.launch` para iniciar tareas asíncronas de forma segura.

### 5. `withContext`
Aunque este proyecto prefiere `flowOn` por ser más idiomático para `Flows`, `withContext` es una función fundamental para cambiar de hilo dentro de una corrutina. Sirve para ejecutar un bloque de código en un `Dispatcher` específico y luego regresar al contexto original.

### 6. `lifecycleScope` y Observación en Compose
En la capa de UI (Composable functions), necesitamos observar los datos del `ViewModel` de una manera que respete el ciclo de vida de la UI.
- **`collectAsState()`**: En `OrderProcessorScreen.kt`, usamos `val orders by viewModel.orders.collectAsState()`. Esta función de Jetpack Compose recolecta el `StateFlow` del `ViewModel` y automáticamente vuelve a dibujar la UI cuando los datos cambian.
- **`LaunchedEffect`**: Se utiliza para recolectar el `Channel` de eventos del `ViewModel` y mostrar un `Snackbar`. El `LaunchedEffect` cancela su corrutina automáticamente cuando el Composable abandona la pantalla.

### 7. WorkManager
`WorkManager` es una biblioteca para trabajo en segundo plano que necesita ejecución garantizada y diferible.
- **Estado en este proyecto**: La dependencia de `WorkManager` está incluida en el `build.gradle.kts`. Sin embargo, **no hay una implementación activa de un `Worker` en el código fuente actual**. Esto representa una oportunidad de mejora para procesar pedidos de forma persistente, incluso si el usuario cierra la aplicación.

---

## 🖥️ Descripción de Interfaces Implementadas

La aplicación consta de una pantalla principal que integra varios componentes reutilizables.

### `OrderProcessorScreen`

- **Propósito:** Es la pantalla única y principal de la aplicación. Orquesta todos los demás componentes.
- **Comportamiento Principal:**
    - Muestra un panel de control para añadir, procesar y limpiar pedidos.
    - Presenta una tarjeta con estadísticas en tiempo real (total, pendientes, cocinando, listos).
    - Muestra una lista de todos los pedidos (`OrderCard`) de forma reactiva.
    - Utiliza `viewModel.orders.collectAsState()` para observar cambios y redibujar la UI.
    - Utiliza `LaunchedEffect` para escuchar eventos del ViewModel y mostrar notificaciones (`Snackbar`).

### `ControlPanel` (Componente)

- **Propósito:** Agrupar los botones de acción principales.
- **Comportamiento:** Contiene los botones para "Añadir Pedido", "Procesar Todos" y "Limpiar Pedidos", delegando las acciones al `OrderViewModel`.

### `OrderCard` (Componente)

- **Propósito:** Mostrar la información de un único pedido.
- **Comportamiento:**
    - Muestra el nombre, estado y una barra de progreso (`LinearProgressIndicator`) que se actualiza en tiempo real.
    - Contiene un botón para procesar ese pedido específico.
    - El color y la información cambian según el `OrderStatus` del pedido.

---

## ⚙️ Instrucciones de Ejecución

Sigue los siguientes pasos para compilar o ejecutar el proyecto:

1. **Clonar el repositorio:**
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   ```
   o descargar el archivo ZIP desde GitHub.

2. **Abrir el proyecto:**
   - Descomprime el archivo (si descargaste el ZIP).
   - Abre **Android Studio** y selecciona la carpeta del proyecto.

3. **OPCION A: Ejecutar la aplicación en modo desarrollo:**
   - Conecta un dispositivo Android o utiliza un emulador.
   - Haz clic en **Run ▶️** dentro de Android Studio.

4. **OPCION B: Generar APK de lanzamiento:**
   ```bash
   ./gradlew assembleRelease
   ```
   El APK sin firmar se encontrará en `app/build/outputs/apk/release/app-release-unsigned.apk`. Para instalarlo, debe ser firmado primero.
   
   📘 [Guía oficial para firmar APKs](https://developer.android.com/studio/publish/app-signing)
