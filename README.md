# Reto Técnico: API de E-commerce y Tokenización
Este repositorio contiene la solución al reto técnico para la posición de Desarrollador Java. El proyecto consiste en el diseño, desarrollo y despliegue de una API RESTful que simula un proceso de tokenización de tarjetas, gestión de clientes y un flujo de compras en línea, construido con Java y Spring Boot.
# Requerimientos Funcionales
## Tokenización de Tarjetas de Crédito:
- [x] Crear componente que recibe datos de tarjeta y devuelve un token único.
- [x] API de tokenización autenticada por API Key o Secret Key.
- [x] Configurar probabilidad de rechazo para la creación de tokens.
## Ping API:
- [x] Implementar endpoint /ping que retorne pong con código HTTP 200.
## Gestión de Clientes:
- [x] Permitir el registro de clientes con datos básicos.
- [x] Validar que los datos del cliente sean correctos y únicos (email/teléfono).
## Búsqueda de Productos:
- [ ] Implementar funcionalidad para buscar productos.
- [ ] Almacenar las búsquedas de forma asíncrona.
- [ ] Restringir visualización de productos según stock configurable.
## Carrito de Compras:
- [ ] Implementar la funcionalidad para agregar productos al carrito.
## Gestión de Pedidos y Pagos:
- [ ] Registrar pedidos con detalles del cliente, tarjeta y dirección.
- [ ] Implementar lógica de aprobación/rechazo de pagos con probabilidad configurable.
- [ ] Reintentar pago N veces si es rechazado.
## Notificaciones por Correo:
- [ ] Enviar correos al cliente en caso de éxito o fallo del pago.
## Logs Centralizados:
- [ ] Registrar todos los eventos con un UUID único por transacción en la base de datos.
# Requerimientos No Funcionales y Criterios de Entrega
## Seguridad:
- [ ] Implementar autenticación mediante API Key / Secret Key.
- [ ] Encriptar datos sensibles (tarjetas de crédito) antes de almacenarlos.

## Escalabilidad y Desempeño:
- [ ] Diseñar la arquitectura para soportar concurrencia.
- [ ] Centralizar parámetros clave en un archivo de configuración externo.
## Pruebas:
- [ ] Proveer pruebas unitarias con al menos 80% de cobertura.
- [ ] Proveer una colección de Postman/Insomnia/Bruno para pruebas.
## Mantenibilidad:
- [ ] Diseñar un código modular y documentado.
## DevSecOps:
- [ ] Implementar flujos de trabajo para Integración Continua (CI).
- [ ] Implementar flujos de trabajo para Pruebas Continuas.
- [ ] Implementar flujos de trabajo para Despliegue Continuo (CD).
## Despliegue:
- [ ] El sistema debe ser desplegable usando Docker y Docker Compose.
- [ ] La API debe estar desplegada y accesible en línea en un proveedor de hosting.
## Documentación y Entrega:
- [x] Subir el código a un repositorio Git.
- [ ] Crear archivo README.md con toda la información requerida.
- [ ] Proveer un diagrama de arquitectura.
- [ ] Documentar el uso de IA (prompts, links a chats).
- [ ] Entregar la solución vía correo electrónico.
## Exposición Oral:
- [ ] Preparar una DEMO del sistema.
- [ ] Prepararse para la sesión de preguntas y respuestas técnicas.