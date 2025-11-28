# 🔄 AppHilos: Simulación de Procesamiento de Pedidos

## Integrantes

- Carrasco Choque, Arles Melvin
- Chancuaña Alvis, Klismann
- Hancco Soncco, Vladimir Jaward
- Hanccoccallo Achircana, Frank Duks
- Nina Suyo, Diego Claudio

## 📘 Resumen Ejecutivo

Este proyecto es una **aplicación móvil para Android**, desarrollada íntegramente en **Kotlin con Jetpack Compose**. Su propósito principal es **demostrar y simular un sistema de procesamiento de pedidos en segundo plano**, utilizando coroutines de Kotlin para gestionar tareas asíncronas sin bloquear la interfaz de usuario.

La aplicación presenta una lista de pedidos predefinidos y permite al usuario controlar la simulación a través de un panel de control con las siguientes acciones:

- **Iniciar:** Comienza el procesamiento de pedidos en estado "Pendiente".
- **Pausar:** Detiene temporalmente la simulación.
- **Reiniciar:** Restablece todos los pedidos a su estado inicial.

El estado de cada pedido se actualiza en tiempo real en la interfaz, cambiando de `PENDIENTE` a `EN PROCESO` y finalmente a `PROCESADO`, mostrando de manera clara el poder de la gestión de estado reactiva en Jetpack Compose.

---

## 🖥️ Componentes de la Interfaz Implementados

A continuación se detallan las principales pantallas y componentes de la aplicación:

### `OrderProcessorScreen`

- **Propósito:** Es la pantalla principal y única de la aplicación. Orquesta la visualización de todos los demás componentes.
- **Comportamiento Principal:** Muestra una lista de pedidos, un panel de control y un registro de logs. Recopila el estado del `OrderViewModel` y lo refleja en la UI, asegurando que la interfaz de usuario siempre muestre los datos más recientes.

### `ControlPanel` (Componente)

- **Propósito:** Proporcionar al usuario los controles para manejar la simulación.
- **Comportamiento Principal:** Contiene los botones "Iniciar", "Pausar" y "Reiniciar". Cada botón está vinculado a las funciones correspondientes en el `OrderViewModel` para controlar el flujo del procesamiento de pedidos. También muestra una barra de progreso que refleja el avance general de la simulación.

### `OrderCard` (Componente)

- **Propósito:** Mostrar la información de un único pedido de forma clara y visual.
- **Comportamiento Principal:** Es una tarjeta que presenta el ID del pedido, su descripción y su estado actual. El color del indicador de estado cambia dinámicamente (`PENDIENTE` en gris, `EN PROCESO` en azul, `PROCESADO` en verde) para ofrecer una retroalimentación visual inmediata.

---

## ⚙️ Instrucciones de Ejecución

Sigue los siguientes pasos para compilar y ejecutar el proyecto:

1.  **Clonar el repositorio:**

    ```bash
    git clone [URL_DEL_REPOSITORIO]
    ```

    o descargar el archivo ZIP del proyecto.

2.  **Abrir el proyecto:**
    - Descomprime el archivo (si lo descargaste en formato ZIP).
    - Abre **Android Studio** y selecciona la carpeta del proyecto (`appHilos`).

3.  **Ejecutar la aplicación (Modo Desarrollo):**
    - Asegúrate de tener un dispositivo Android conectado o un emulador configurado.
    - Haz clic en el botón **Run ▶️** en la barra de herramientas de Android Studio.

4.  **Generar un APK de Lanzamiento (Opcional):**
    - Para generar un APK no firmado, puedes ejecutar la siguiente tarea de Gradle desde la terminal de Android Studio:
      ```bash
      ./gradlew assembleRelease
      ```
    - El archivo generado se encontrará en la ruta:
      ```
      app/build/outputs/apk/release/app-release-unsigned.apk
      ```
    - **Nota:** Para instalar este APK en un dispositivo o publicarlo, necesitarás firmarlo con una clave de lanzamiento.

---

## 📊 Características Técnicas

- **Arquitectura:** MVVM (Model-View-ViewModel), separando la lógica de la interfaz de usuario.
- **Interfaz de Usuario:** 100% construida con **Jetpack Compose**, el moderno toolkit de UI declarativo de Android.
- **Gestión de Estado:** Uso de `StateFlow` y `MutableState` dentro del `OrderViewModel` para gestionar y exponer el estado de manera reactiva y segura para el ciclo de vida.
- **Asincronía:** Implementación de **Coroutines de Kotlin** para manejar el procesamiento en segundo plano, evitando bloquear el hilo principal y manteniendo una UI fluida.
- **Patrón de Diseño:** Componentes de UI reutilizables y sin estado (`OrderCard`, `ControlPanel`) que reciben datos y lambdas para comunicar eventos.

---

## 🔧 Funcionalidades Principales

- **Simulación de Procesamiento Asíncrono:** El núcleo de la aplicación simula el procesamiento de pedidos utilizando coroutines, actualizando el estado de cada uno secuencialmente.
- **Control Interactivo de la Simulación:** Permite al usuario iniciar, pausar y reiniciar el proceso, demostrando el control sobre las tareas en segundo plano.
- **Visualización del Estado en Tiempo Real:** La interfaz se actualiza automáticamente para reflejar el estado actual de cada pedido y el progreso general.
- **Registro de Eventos (Logging):** Muestra un log de eventos importantes, como el inicio, la pausa y la finalización de la simulación.

---

## 🗃️ Modelo de Datos

El modelo de datos de la aplicación es sencillo y se centra en representar un pedido y su ciclo de vida:

- **`Order`**: Representa un pedido con las siguientes propiedades:
  - `id` (Int): Identificador único.
  - `description` (String): Descripción del pedido.
  - `timestamp` (Long): Marca de tiempo de su creación.
  - `status` (OrderStatus): El estado actual del pedido.

- **`OrderStatus`**: Es una clase `enum` que define los posibles estados de un pedido:
  - `PENDING` (Pendiente)
  - `IN_PROGRESS` (En Proceso)
  - `PROCESSED` (Procesado)
  - `CANCELLED` (Cancelado - no utilizado en la simulación actual pero disponible).
