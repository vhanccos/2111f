# Guion de Demostración Técnica: AppHilos

Este documento sirve como un guion detallado para una presentación y demostración en vivo de los conceptos de concurrencia implementados en la aplicación `AppHilos`.

**Preparación Pre-Demo:**
1.  Abrir el proyecto en Android Studio.
2.  Abrir la ventana de **Logcat**.
3.  Crear filtros de Logcat para los `tags`: **`Order`** y **`BackupWorker`**.
4.  Tener el emulador o dispositivo físico visible y listo para la interacción.

---

### 1. Main Thread vs. Worker Threads (La UI Nunca Muere)

*   **Concepto a Probar:** El Hilo Principal (`Main Thread`) se dedica exclusivamente a la interfaz de usuario (UI). Cualquier trabajo pesado o de larga duración debe ser delegado a hilos secundarios (`Worker Threads`) para evitar que la aplicación se congele (ANR - Application Not Responding).

*   **Explicación Técnica:** En Android, todas las interacciones del usuario (toques, scrolls) y las actualizaciones de la pantalla ocurren en el `Main Thread`. Si ejecutamos una tarea que dura varios segundos en este hilo (como una llamada de red o un cálculo complejo), el hilo se bloquea. No puede procesar nuevos eventos de usuario ni dibujar nuevos frames, resultando en una app congelada. Las corrutinas son la herramienta moderna para gestionar esta delegación de trabajo de forma sencilla y segura.

*   **Prueba de Demostración:** Comprobar la fluidez de la UI durante una carga de trabajo pesada.

*   **Pasos de la Acción:**
    1.  Pulsa **"➕ Agregar Pedido"** 6 o 7 veces.
    2.  Pulsa **"▶️ Procesar Todos"**.
    3.  Mientras los pedidos están "Cocinando", **desliza la lista** de arriba hacia abajo vigorosamente.

*   **Evidencia Irrefutable (Visual):**
    *   El scroll es **perfectamente fluido**.
    *   Las animaciones de las barras de progreso se actualizan suavemente.

*   **Conclusión del Discurso:** "Como pueden ver, la app es 100% responsiva a pesar de estar procesando múltiples pedidos en paralelo. Esto es la prueba de que el hilo principal está libre, dedicado solo a la interfaz, mientras el trabajo pesado ocurre en hilos de fondo gracias a las corrutinas."

---

### 2. Coroutine Dispatchers (El Cerebro de la Delegación)

*   **Concepto a Probar:** Los `Dispatchers` determinan en qué hilo o grupo de hilos se ejecuta una corrutina. El uso de `flowOn` nos permite cambiar el contexto de ejecución de un `Flow` de manera idiomática.

*   **Explicación Técnica:**
    *   `Dispatchers.Main`: El único hilo de UI. Se usa para cualquier tarea que interactúe directamente con la interfaz, como actualizar un `TextView` o, en nuestro caso, es el contexto por defecto del `viewModelScope` desde donde se recolectan los resultados para la UI.
    *   `Dispatchers.IO`: Optimizado para operaciones de Entrada/Salida (I/O) que no consumen mucho CPU, como llamadas a una API, acceso a base de datos o lectura/escritura de archivos. Utiliza un pool de hilos compartido.
    *   `Dispatchers.Default`: Optimizado para trabajo intensivo de CPU, como ordenar una lista muy grande, realizar cálculos complejos o procesar imágenes. Utiliza un pool de hilos cuyo tamaño está limitado por el número de núcleos de CPU del dispositivo. **En nuestra simulación, usamos `Default` porque "cocinar" simula un trabajo computacionalmente activo.**

*   **Código Relevante:**
    *   `app/src/main/java/com/example/apphilos/repository/OrderRepository.kt`
    ```kotlin
    fun processOrder(order: Order): Flow<Order> = flow {
        // ... toda la lógica de simulación con 'delay' ...
    }.flowOn(Dispatchers.Default) // ← ¡ESTA ES LA CLAVE! (L57)
    ```

*   **Prueba de Demostración:** Seguir el "viaje" de un pedido a través de los hilos usando Logcat.

*   **Pasos de la Acción:**
    1.  Pulsa **"🗑️ Limpiar Todo"**.
    2.  En Logcat, activa el filtro **"Order"**.
    3.  Pulsa **"➕ Agregar Pedido"** una vez y luego pulsa su botón **"Procesar"**.

*   **Evidencia Irrefutable (Logcat):**
    *   El log `D/OrderViewModel: processOrder: Iniciando #...` aparece en el hilo **`main`**.
    *   Inmediatamente después, los logs del `Repository` (`D/OrderRepository: processOrder...`) aparecen en un hilo llamado **`DefaultDispatcher-worker-X`**.

*   **Conclusión del Discurso:** "Observen el Logcat. La acción se inicia en 'main', pero gracias a `.flowOn(Dispatchers.Default)`, todo el flujo de procesamiento se traslada a un 'worker thread'. Esta simple línea es el corazón de nuestra estrategia para mantener la app fluida."

---

### 3. `viewModelScope` (El Supervisor del Ciclo de Vida)

*   **Concepto a Probar:** Las corrutinas lanzadas en `viewModelScope` están atadas al ciclo de vida del `ViewModel`. Sobreviven a cambios de configuración de la UI y se cancelan automáticamente cuando el `ViewModel` se destruye, evitando memory leaks.

*   **Código Relevante:**
    *   `app/src/main/java/com/example/apphilos/viewmodel/OrderViewModel.kt`
    ```kotlin
    fun processOrder(order: Order) {
        viewModelScope.launch { // (L76)
            try {
                Log.d("OrderViewModel", "processOrder: Iniciando #${order.id}")

                repository.processOrder(order).collect { updatedOrder -> // (L80)
                    updateOrder(updatedOrder)
                }
                //...
            } //...
        }
    }
    ```

*   **Prueba de Demostración:** Rotar el dispositivo durante el procesamiento para forzar una recreación de la UI.

*   **Pasos de la Acción:**
    1.  Añade 3-4 pedidos y pulsa **"▶️ Procesar Todos"**.
    2.  Mientras se están "cocinando", **rota el emulador o dispositivo físico**.

*   **Evidencia Irrefutable (Visual):**
    *   La UI se redibuja, pero los pedidos **continúan su progreso exactamente donde estaban**. Las barras de progreso no se reinician.

*   **Conclusión del Discurso:** "Acabo de rotar la pantalla, lo que en Android destruye y recrea la interfaz. El proceso no se interrumpió porque la corrutina vive en el `viewModelScope`, que es independiente de la UI. Esto garantiza la continuidad del trabajo y previene errores."

---

### 4. `LaunchedEffect` (El Guardián del Ciclo de Vida en Compose)

*   **Concepto a Probar:** `LaunchedEffect` es un Composable que lanza una corrutina cuando entra en la composición y la cancela automáticamente cuando sale. Es la forma correcta en Jetpack Compose de ejecutar trabajos asíncronos que están atados al ciclo de vida de la UI, como observar un `Channel` o un `Flow`.

*   **Código Relevante:**
    *   `app/src/main/java/com/example/apphilos/ui/screens/OrderProcessorScreen.kt`
    ```kotlin
    LaunchedEffect(Unit) { // (L25)
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message) // (L27)
        }
    }
    ```

*   **Prueba de Demostración:** Mostrar que la recolección de eventos (Snackbars) está viva solo cuando la UI es visible.

*   **Pasos de la Acción:**
    1.  Pulsa **"➕ Agregar Pedido"**. Un `Snackbar` aparece confirmando la acción.
    2.  Minimiza la app (botón Home) y vuelve a abrirla. El `Snackbar` no vuelve a aparecer, demostrando que es un evento de una sola vez. `LaunchedEffect` se canceló y se relanzó, y está a la espera de nuevos eventos.

*   **Conclusión del Discurso:** "Usamos `LaunchedEffect` para escuchar eventos únicos del ViewModel, como estas notificaciones. Este scope asegura que solo escuchamos cuando la UI está en pantalla, previniendo errores y comportamientos inesperados si un evento llegara cuando la UI no está visible."

---

### 5. Concurrencia Estructurada (`async/await` y `Semaphore`)

*   **Concepto a Probar:** La app usa un `Semaphore` para limitar el acceso a un recurso (solo 3 "cocinas" a la vez) y `async/await` para lanzar múltiples tareas en paralelo y luego esperar a que todas finalicen.

*   **Código Relevante:**
    *   `app/src/main/java/com/example/apphilos/viewmodel/OrderViewModel.kt`
    ```kotlin
    val jobs = pending.map { order ->
        async { // (L101)
            repository.processOrder(order).collect { updatedOrder ->
                updateOrder(updatedOrder)
            }
        }
    }

    jobs.forEach { it.await() } // (L108)
    ```
    *   `app/src/main/java/com/example/apphilos/repository/OrderRepository.kt`
    ```kotlin
    private val kitchenSemaphore = Semaphore(3) // (L14)
    // ...
    kitchenSemaphore.acquire() // (L23)
    // ...
    kitchenSemaphore.release() // (L53)
    ```

*   **Prueba de Demostración:** Procesar más pedidos que las "cocinas" disponibles.

*   **Pasos de la Acción:**
    1.  Limpia los pedidos.
    2.  Añade **5 pedidos** y pulsa **"▶️ Procesar Todos"**.

*   **Evidencia Irrefutable (Visual):**
    *   El contador "Cocinando" en las estadísticas **nunca supera el número 3**.
    *   Se ve claramente cómo 3 pedidos se procesan y los otros 2 esperan su turno.

*   **Conclusión del Discurso:** "Lanzamos 5 tareas en paralelo con `async`, pero nuestro `Semaphore(3)` actúa como un portero, asegurando que solo 3 pasen a la vez. Luego, con `await`, nos aseguramos de que el mensaje 'Todos los pedidos procesados' solo se envíe cuando el último de los 5 haya terminado. Esto es concurrencia estructurada y controlada."

---

### 6. `WorkManager` (El Trabajador Inmortal)

*   **Concepto a Probar:** `WorkManager` ejecuta tareas de forma garantizada y persistente, incluso si la aplicación es cerrada por completo. Es la herramienta ideal para trabajo que **debe** completarse.

*   **Código Relevante:**
    *   `app/src/main/java/com/example/apphilos/workers/BackupWorker.kt`
    ```kotlin
    class BackupWorker(appContext: Context, workerParams: WorkerParameters):
        CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result { // (L19)
            //...
            delay(5000) // (L26)
            makeStatusNotification("Respaldo completado exitosamente", applicationContext) // (L29)
            return Result.success()
        }
    }
    ```
    *   `app/src/main/java/com/example/apphilos/viewmodel/OrderViewModel.kt`
    ```kotlin
    fun scheduleBackup() {
        viewModelScope.launch {
            val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>().build() // (L157)
            workManager.enqueue(backupRequest) // (L158)
            //...
        }
    }
    ```

*   **Prueba de Demostración:** Programar una tarea y cerrar la aplicación forzosamente.

*   **Pasos de la Acción:**
    1.  En Logcat, activa el filtro **"BackupWorker"**.
    2.  Pulsa el botón **"💾 Respaldar (WM)"**.
    3.  Verás un `Snackbar` confirmando la programación.
    4.  Inmediatamente, ve a la pantalla de apps recientes y **cierra la aplicación por completo** (swipe up).

*   **Evidencia Irrefutable (Visual y Logcat):**
    *   **Logcat:** Verás el log `D/BackupWorker: Iniciando respaldo...` y luego el proceso de la app terminará.
    *   **Notificación del Sistema:** Pasados ~5 segundos, y con la app cerrada, **aparecerá una notificación del sistema** con el mensaje: **"Respaldo completado exitosamente"**.

*   **Conclusión del Discurso:** "Acabamos de matar la aplicación. Sin embargo, la tarea se completó y recibimos la notificación. Esto es `WorkManager`: una vez que le das una tarea, el sistema operativo garantiza que se ejecutará, pase lo que pase con la app. Es la máxima garantía para trabajo en segundo plano."