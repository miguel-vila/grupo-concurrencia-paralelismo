# ¿Cómo funciona un _pool_ de _threads_?

## Material

//@TODO

Tal vez:

* [Java theory and practice: Thread pools and work queues](http://www.ibm.com/developerworks/java/library/j-jtp0730/index.html)

## Enunciado

Tentativamente:

> Construir un servidor que reciba mensajes por un socket. Al ser un servidor, las peticiones deben poderse atender de manera simultánea. Para resolverlo, se debe implementar un pool de threads (no usar los dispuestos por la JVM) que permita atender de la manera más eficiente (https://en.wikipedia.org/wiki/Amdahl%27s_law) estos mensajes.

> Para simplificar el ejemplo, la atención de cada mensaje debe ser un Thread.sleep(2000).

> Pueden hacer pruebas de su implementación con gatling/jmeter/etc que simule diferentes curvas de concurrencia y así pueden evaluar su implementación.

> Hay varios patrones de implementación: reactor pattern, event loop, competing consumers, etc. Es cuestión de googlear :D

> Si tienen preguntas me avisan. Por ahí tengo una implementación de un pool de threads que hice hace ya varios años, creo que podemos comparar resultados.
